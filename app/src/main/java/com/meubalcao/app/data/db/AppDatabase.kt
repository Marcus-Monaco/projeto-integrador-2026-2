package com.meubalcao.app.data.db

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.io.File
import java.io.InputStream
import java.io.OutputStream

/**
 * Acesso ao banco SQLite local "meubalcao.db" (padrão singleton).
 *
 * - Na primeira abertura executa o script assets/schema.sql (cópia empacotada de database/schema.sql).
 * - Ativa as chaves estrangeiras a cada abertura (sem isso o SQLite as ignora).
 * - Controla a versão do esquema para futuras migrações (onUpgrade).
 */
class AppDatabase private constructor(private val context: Context) :
    SQLiteOpenHelper(context, NOME_ARQUIVO, null, VERSAO) {

    companion object {
        const val NOME_ARQUIVO = "meubalcao.db"
        const val VERSAO = 1

        @Volatile
        private var instancia: AppDatabase? = null

        fun get(context: Context): AppDatabase =
            instancia ?: synchronized(this) {
                instancia ?: AppDatabase(context.applicationContext).also { instancia = it }
            }

        /** Usado pelos testes automatizados para começar cada teste com um banco novo. */
        fun reiniciar() = synchronized(this) { instancia?.close(); instancia = null }

        /** Ordem de exclusão respeitando as chaves estrangeiras (filhas antes das mães). */
        val TABELAS = listOf(
            "envio_campanha", "campanha", "item_venda", "venda",
            "produto", "categoria", "cliente", "meta_mensal", "perfil_negocio",
        )
    }

    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        db.setForeignKeyConstraintsEnabled(true)
        // Diário em arquivo único: o backup é uma cópia fiel de meubalcao.db
        db.disableWriteAheadLogging()
    }

    override fun onCreate(db: SQLiteDatabase) {
        executarScript(db, "schema.sql")
    }

    override fun onUpgrade(db: SQLiteDatabase, antiga: Int, nova: Int) {
        // Exemplo de migração futura:
        // if (antiga < 2) db.execSQL("ALTER TABLE produto ADD COLUMN codigo_barras TEXT")
    }

    /** Executa um script .sql dos assets, comando a comando, dentro de uma transação. */
    fun executarScript(db: SQLiteDatabase, asset: String) {
        val texto = context.assets.open(asset).bufferedReader(Charsets.UTF_8).use { it.readText() }
        val comandos = SqlScript.dividir(texto).filterNot { it.uppercase().startsWith("PRAGMA") }
        db.beginTransaction()
        try {
            comandos.forEach { db.execSQL(it) }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    // ------------------------------------------------------------------ utilidades

    fun <T> consultar(sql: String, vararg args: Any?, mapear: (Cursor) -> T): List<T> {
        val cursor = readableDatabase.rawQuery(sql, args.map { it?.toString() }.toTypedArray())
        return cursor.use { c -> buildList { while (c.moveToNext()) add(mapear(c)) } }
    }

    fun <T> consultarUm(sql: String, vararg args: Any?, mapear: (Cursor) -> T): T? =
        consultar(sql, *args, mapear = mapear).firstOrNull()

    fun <T> transacao(bloco: (SQLiteDatabase) -> T): T {
        val db = writableDatabase
        db.beginTransaction()
        try {
            val r = bloco(db)
            db.setTransactionSuccessful()
            return r
        } finally {
            db.endTransaction()
        }
    }

    // ------------------------------------------------------------------ dados de exemplo e limpeza (RF27)

    fun apagarTudo() = transacao { db ->
        TABELAS.forEach { db.delete(it, null, null) }
        db.delete("sqlite_sequence", null, null)
    }

    fun carregarExemplo() = transacao { db ->
        TABELAS.forEach { db.delete(it, null, null) }
        db.delete("sqlite_sequence", null, null)
        val texto = context.assets.open("seed.sql").bufferedReader(Charsets.UTF_8).use { it.readText() }
        SqlScript.dividir(texto)
            .filterNot { it.uppercase().startsWith("PRAGMA") }
            .forEach { db.execSQL(it) }
    }

    // ------------------------------------------------------------------ backup e restauração (RF26)

    fun arquivo(): File = context.getDatabasePath(NOME_ARQUIVO)

    /** Fecha o banco e copia o arquivo para [destino]. */
    @Synchronized
    fun exportar(destino: OutputStream) {
        close()
        arquivo().inputStream().use { it.copyTo(destino) }
    }

    /**
     * Confere o cabeçalho "SQLite format 3", substitui o arquivo atual e reabre a conexão.
     * Se o arquivo não for um banco válido do MeuBalcão, nada é alterado.
     */
    @Synchronized
    fun restaurar(origem: InputStream) {
        val temp = File(context.cacheDir, "restaurar.db")
        origem.use { entrada -> temp.outputStream().use { entrada.copyTo(it) } }
        val cabecalho = temp.inputStream().use { s -> ByteArray(16).also { s.read(it) } }
        require(String(cabecalho, Charsets.US_ASCII).startsWith("SQLite format 3")) {
            "O arquivo escolhido não é um backup do MeuBalcão."
        }
        SQLiteDatabase.openDatabase(temp.path, null, SQLiteDatabase.OPEN_READONLY).use { db ->
            db.rawQuery("SELECT COUNT(*) FROM sqlite_master WHERE name IN ('produto','venda','cliente')", null)
                .use { c -> c.moveToFirst(); require(c.getInt(0) == 3) { "O arquivo não contém as tabelas do MeuBalcão." } }
        }
        close()
        val atual = arquivo()
        File(atual.path + "-journal").delete()
        temp.copyTo(atual, overwrite = true)
        temp.delete()
        writableDatabase // reabre
    }
}

// ---------------------------------------------------------------------- extensões de Cursor

fun Cursor.str(col: String): String? = getColumnIndexOrThrow(col).let { if (isNull(it)) null else getString(it) }
fun Cursor.txt(col: String): String = str(col) ?: ""
fun Cursor.long(col: String): Long = getLong(getColumnIndexOrThrow(col))
fun Cursor.longOrNull(col: String): Long? = getColumnIndexOrThrow(col).let { if (isNull(it)) null else getLong(it) }
fun Cursor.int(col: String): Int = getInt(getColumnIndexOrThrow(col))
fun Cursor.dbl(col: String): Double = getDouble(getColumnIndexOrThrow(col))
fun Cursor.bool(col: String): Boolean = getInt(getColumnIndexOrThrow(col)) == 1

fun valores(vararg pares: Pair<String, Any?>): ContentValues = ContentValues().apply {
    pares.forEach { (k, v) ->
        when (v) {
            null -> putNull(k)
            is String -> put(k, v)
            is Int -> put(k, v)
            is Long -> put(k, v)
            is Double -> put(k, v)
            is Boolean -> put(k, if (v) 1 else 0)
            else -> put(k, v.toString())
        }
    }
}

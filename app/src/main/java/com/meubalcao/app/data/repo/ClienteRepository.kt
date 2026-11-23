package com.meubalcao.app.data.repo

import android.database.Cursor
import com.meubalcao.app.data.db.AppDatabase
import com.meubalcao.app.data.db.bool
import com.meubalcao.app.data.db.dbl
import com.meubalcao.app.data.db.int
import com.meubalcao.app.data.db.long
import com.meubalcao.app.data.db.str
import com.meubalcao.app.data.db.txt
import com.meubalcao.app.data.db.valores
import com.meubalcao.app.data.model.Cliente
import com.meubalcao.app.data.model.ClienteResumo
import com.meubalcao.app.data.model.Origem
import com.meubalcao.app.data.model.Segmento

/** Tabela cliente e segmentação automática (RF13 a RF16, RN08). */
class ClienteRepository(private val db: AppDatabase) {

    /**
     * Resumo de cada cliente calculado a partir das vendas CONCLUÍDAS
     * (vendas canceladas não contam para os segmentos, RN05).
     */
    private val resumoSql = """
        SELECT c.*,
               COUNT(v.id)                              AS compras,
               COALESCE(SUM(v.valor_total), 0)          AS total_gasto,
               MAX(v.data_hora)                         AS ultima_compra,
               CAST(julianday(date('now','localtime')) - julianday(date(MAX(v.data_hora))) AS INTEGER) AS dias_sem_comprar,
               (c.data_nascimento IS NOT NULL AND
                strftime('%m', c.data_nascimento) = strftime('%m','now','localtime')) AS aniversariante,
               (date(c.criado_em) >= date('now','localtime','-30 days')) AS novo
        FROM cliente c
        LEFT JOIN venda v ON v.cliente_id = c.id AND v.status = 'CONCLUIDA'
    """

    /** Regras de segmentação RN08 aplicadas sobre o resumo. */
    private fun having(s: Segmento) = when (s) {
        Segmento.TODOS -> ""
        Segmento.RECORRENTES -> "HAVING compras >= 3"
        Segmento.INATIVOS -> "HAVING compras > 0 AND dias_sem_comprar > 30"
        Segmento.NOVOS -> "HAVING novo = 1"
        Segmento.ANIVERSARIANTES -> "HAVING aniversariante = 1"
    }

    private fun mapearCliente(c: Cursor) = Cliente(
        id = c.long("id"),
        nome = c.txt("nome"),
        telefone = c.str("telefone"),
        email = c.str("email"),
        dataNascimento = c.str("data_nascimento"),
        cidade = c.str("cidade"),
        origem = runCatching { Origem.valueOf(c.txt("origem")) }.getOrDefault(Origem.OUTRO),
        aceitaPromocoes = c.bool("aceita_promocoes"),
        observacoes = c.str("observacoes"),
        criadoEm = c.txt("criado_em"),
    )

    private fun mapearResumo(c: Cursor) = ClienteResumo(
        cliente = mapearCliente(c),
        compras = c.int("compras"),
        totalGasto = c.dbl("total_gasto"),
        ultimaCompra = c.str("ultima_compra"),
        diasSemComprar = c.getColumnIndexOrThrow("dias_sem_comprar").let { if (c.isNull(it)) null else c.getInt(it) },
        aniversariante = c.bool("aniversariante"),
    )

    fun listar(busca: String = "", segmento: Segmento = Segmento.TODOS): List<ClienteResumo> {
        val termo = "%${busca.trim()}%"
        return db.consultar(
            """$resumoSql WHERE c.nome LIKE ? OR COALESCE(c.telefone,'') LIKE ?
               GROUP BY c.id ${having(segmento)}
               ORDER BY c.nome COLLATE NOCASE""",
            termo, termo, mapear = ::mapearResumo,
        )
    }

    fun resumo(id: Long): ClienteResumo? =
        db.consultarUm("$resumoSql WHERE c.id = ? GROUP BY c.id", id, mapear = ::mapearResumo)

    /** Público de uma campanha: segmento + somente quem aceita promoções (RN09). */
    fun publico(segmento: Segmento): List<ClienteResumo> = db.consultar(
        """$resumoSql WHERE c.aceita_promocoes = 1 AND c.telefone IS NOT NULL
           GROUP BY c.id ${having(segmento)} ORDER BY c.nome COLLATE NOCASE""",
        mapear = ::mapearResumo,
    )

    fun contagemPorSegmento(): Map<Segmento, Int> = Segmento.entries.associateWith { s ->
        db.consultarUm("SELECT COUNT(*) FROM ($resumoSql GROUP BY c.id ${having(s)})") { it.getInt(0) } ?: 0
    }

    /** Salva o cliente. O telefone é UNIQUE (RN11): duplicado gera SQLiteConstraintException. */
    fun salvar(cl: Cliente): Long {
        val v = valores(
            "nome" to cl.nome.trim(),
            "telefone" to cl.telefone?.trim()?.ifEmpty { null },
            "email" to cl.email?.trim()?.ifEmpty { null },
            "data_nascimento" to cl.dataNascimento?.ifEmpty { null },
            "cidade" to cl.cidade?.trim()?.ifEmpty { null },
            "origem" to cl.origem.name,
            "aceita_promocoes" to cl.aceitaPromocoes,
            "observacoes" to cl.observacoes?.trim()?.ifEmpty { null },
        )
        val w = db.writableDatabase
        val id = if (cl.id == 0L) w.insertOrThrow("cliente", null, v)
        else { w.update("cliente", v, "id = ?", arrayOf(cl.id.toString())); cl.id }
        Repositorios.notificarMudanca()
        return id
    }

    /** Lista simples para escolher o cliente na nova venda. */
    fun todos(): List<Cliente> = db.consultar("SELECT * FROM cliente ORDER BY nome COLLATE NOCASE", mapear = ::mapearCliente)
}

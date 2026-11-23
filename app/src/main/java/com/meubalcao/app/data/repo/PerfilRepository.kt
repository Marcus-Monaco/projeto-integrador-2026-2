package com.meubalcao.app.data.repo

import android.database.sqlite.SQLiteDatabase
import com.meubalcao.app.data.db.AppDatabase
import com.meubalcao.app.data.db.str
import com.meubalcao.app.data.db.txt
import com.meubalcao.app.data.db.valores
import com.meubalcao.app.data.model.PerfilNegocio

/** Tabelas perfil_negocio (RF01) e meta_mensal (RF22). */
class PerfilRepository(private val db: AppDatabase) {

    fun obter(): PerfilNegocio? = db.consultarUm("SELECT * FROM perfil_negocio WHERE id = 1") { c ->
        PerfilNegocio(
            nomeFantasia = c.txt("nome_fantasia"),
            slogan = c.str("slogan"),
            descricao = c.str("descricao"),
            whatsapp = c.str("whatsapp"),
            instagram = c.str("instagram"),
            endereco = c.str("endereco"),
            horarioFuncionamento = c.str("horario_funcionamento"),
            logoPath = c.str("logo_path"),
            corTema = c.str("cor_tema") ?: "#0F766E",
        )
    }

    /** Existe um único perfil (id = 1, RN10): insere ou substitui. */
    fun salvar(p: PerfilNegocio) {
        db.writableDatabase.insertWithOnConflict(
            "perfil_negocio", null,
            valores(
                "id" to 1,
                "nome_fantasia" to p.nomeFantasia.trim(),
                "slogan" to p.slogan?.trim()?.ifEmpty { null },
                "descricao" to p.descricao?.trim()?.ifEmpty { null },
                "whatsapp" to p.whatsapp?.trim()?.ifEmpty { null },
                "instagram" to p.instagram?.trim()?.ifEmpty { null },
                "endereco" to p.endereco?.trim()?.ifEmpty { null },
                "horario_funcionamento" to p.horarioFuncionamento?.trim()?.ifEmpty { null },
                "logo_path" to p.logoPath,
                "cor_tema" to p.corTema,
            ),
            SQLiteDatabase.CONFLICT_REPLACE,
        )
        Repositorios.notificarMudanca()
    }

    // ---------------------------------------------------------------- meta mensal (uma por mês, RN10)

    fun meta(anoMes: String): Double? =
        db.consultarUm("SELECT valor_meta FROM meta_mensal WHERE ano_mes = ?", anoMes) { it.getDouble(0) }

    fun salvarMeta(anoMes: String, valor: Double) {
        // UPDATE e, se não havia meta no mês, INSERT (compatível com o SQLite do Android 8)
        val w = db.writableDatabase
        val alteradas = w.update("meta_mensal", valores("valor_meta" to valor), "ano_mes = ?", arrayOf(anoMes))
        if (alteradas == 0) w.insertOrThrow("meta_mensal", null, valores("ano_mes" to anoMes, "valor_meta" to valor))
        Repositorios.notificarMudanca()
    }
}

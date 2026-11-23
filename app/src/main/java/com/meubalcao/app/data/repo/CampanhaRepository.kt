package com.meubalcao.app.data.repo

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import com.meubalcao.app.data.db.AppDatabase
import com.meubalcao.app.data.db.int
import com.meubalcao.app.data.db.long
import com.meubalcao.app.data.db.longOrNull
import com.meubalcao.app.data.db.str
import com.meubalcao.app.data.db.txt
import com.meubalcao.app.data.db.valores
import com.meubalcao.app.data.model.Campanha
import com.meubalcao.app.data.model.CanalCampanha
import com.meubalcao.app.data.model.Segmento
import com.meubalcao.app.data.model.StatusCampanha

/** Tabelas campanha e envio_campanha (RF17 a RF20). */
class CampanhaRepository(private val db: AppDatabase) {

    /** Inclui o resultado: clientes alcançados e quantos compraram depois do envio (RF20). */
    private val selectCampanha = """
        SELECT ca.*, p.nome AS produto_nome,
               (SELECT COUNT(*) FROM envio_campanha e WHERE e.campanha_id = ca.id) AS alcancados,
               (SELECT COUNT(DISTINCT e.cliente_id) FROM envio_campanha e
                  JOIN venda v ON v.cliente_id = e.cliente_id AND v.status = 'CONCLUIDA'
                              AND v.data_hora >= e.enviado_em
                 WHERE e.campanha_id = ca.id) AS compraram
        FROM campanha ca LEFT JOIN produto p ON p.id = ca.produto_id
    """

    private fun mapear(c: Cursor) = Campanha(
        id = c.long("id"),
        titulo = c.txt("titulo"),
        mensagem = c.txt("mensagem"),
        produtoId = c.longOrNull("produto_id"),
        produtoNome = c.str("produto_nome"),
        segmento = Segmento.valueOf(c.txt("segmento")),
        canal = CanalCampanha.valueOf(c.txt("canal")),
        dataInicio = c.str("data_inicio"),
        dataFim = c.str("data_fim"),
        status = StatusCampanha.valueOf(c.txt("status")),
        alcancados = c.int("alcancados"),
        compraramDepois = c.int("compraram"),
    )

    fun listar(status: StatusCampanha? = null): List<Campanha> =
        if (status == null) db.consultar("$selectCampanha ORDER BY ca.criada_em DESC, ca.id DESC", mapear = ::mapear)
        else db.consultar("$selectCampanha WHERE ca.status = ? ORDER BY ca.criada_em DESC, ca.id DESC", status.name, mapear = ::mapear)

    fun campanha(id: Long): Campanha? = db.consultarUm("$selectCampanha WHERE ca.id = ?", id, mapear = ::mapear)

    fun salvar(ca: Campanha): Long {
        val v = valores(
            "titulo" to ca.titulo.trim(),
            "mensagem" to ca.mensagem.trim(),
            "produto_id" to ca.produtoId,
            "segmento" to ca.segmento.name,
            "canal" to ca.canal.name,
            "data_inicio" to ca.dataInicio,
            "data_fim" to ca.dataFim,
            "status" to ca.status.name,
        )
        val w = db.writableDatabase
        val id = if (ca.id == 0L) w.insertOrThrow("campanha", null, v)
        else { w.update("campanha", v, "id = ?", arrayOf(ca.id.toString())); ca.id }
        Repositorios.notificarMudanca()
        return id
    }

    fun alterarStatus(id: Long, status: StatusCampanha) {
        db.writableDatabase.update("campanha", valores("status" to status.name), "id = ?", arrayOf(id.toString()))
        Repositorios.notificarMudanca()
    }

    /** Clientes que já receberam a campanha. */
    fun enviados(campanhaId: Long): Set<Long> =
        db.consultar("SELECT cliente_id FROM envio_campanha WHERE campanha_id = ?", campanhaId) { it.getLong(0) }.toSet()

    /** Registra o envio pelo WhatsApp (RF19). Reenvio atualiza a data. */
    fun registrarEnvio(campanhaId: Long, clienteId: Long) {
        db.writableDatabase.insertWithOnConflict(
            "envio_campanha", null,
            valores("campanha_id" to campanhaId, "cliente_id" to clienteId),
            SQLiteDatabase.CONFLICT_REPLACE,
        )
        Repositorios.notificarMudanca()
    }
}

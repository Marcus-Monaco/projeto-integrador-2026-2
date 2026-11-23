package com.meubalcao.app.data.repo

import android.database.Cursor
import com.meubalcao.app.data.db.AppDatabase
import com.meubalcao.app.data.db.dbl
import com.meubalcao.app.data.db.int
import com.meubalcao.app.data.db.long
import com.meubalcao.app.data.db.longOrNull
import com.meubalcao.app.data.db.str
import com.meubalcao.app.data.db.txt
import com.meubalcao.app.data.db.valores
import com.meubalcao.app.data.model.Canal
import com.meubalcao.app.data.model.FormaPagamento
import com.meubalcao.app.data.model.ItemVenda
import com.meubalcao.app.data.model.StatusVenda
import com.meubalcao.app.data.model.Venda

/** Erro de regra de negócio exibido ao usuário. */
class RegraNegocioException(msg: String) : Exception(msg)

/** Tabelas venda e item_venda (RF10 a RF12, RN01 a RN05). */
class VendaRepository(private val db: AppDatabase) {

    /**
     * Registra a venda em uma única transação (RNF07): cabeçalho, itens e baixa de estoque.
     * Se qualquer passo falhar, nada é gravado.
     */
    fun registrar(venda: Venda, itens: List<ItemVenda>): Long {
        if (itens.isEmpty()) throw RegraNegocioException("Adicione pelo menos um item à venda.")       // RN04

        val id = db.transacao { w ->
            // RN02: preço e custo vêm do cadastro atual do produto (não da tela)
            val precos = itens.associate { item ->
                item.produtoId to (db.consultarUm(
                    "SELECT preco, custo, estoque_atual, nome FROM produto WHERE id = ?", item.produtoId,
                ) { c -> Quad(c.dbl("preco"), c.dbl("custo"), c.int("estoque_atual"), c.txt("nome")) }
                    ?: throw RegraNegocioException("Produto não encontrado."))
            }
            val soma = itens.sumOf { precos.getValue(it.produtoId).a * it.quantidade }
            if (venda.desconto < 0 || venda.desconto > soma)
                throw RegraNegocioException("O desconto não pode ser maior que a soma dos itens.")       // RN03

            val vendaId = w.insertOrThrow(
                "venda", null,
                valores(
                    "cliente_id" to venda.clienteId,
                    "canal" to venda.canal.name,
                    "forma_pagamento" to venda.formaPagamento.name,
                    "desconto" to venda.desconto,
                    "valor_total" to Math.round((soma - venda.desconto) * 100) / 100.0,
                    "observacao" to venda.observacao?.trim()?.ifEmpty { null },
                ),
            )
            for (item in itens) {
                val (preco, custo, estoque, nome) = precos.getValue(item.produtoId)
                if (item.quantidade > estoque)                                                           // RN01
                    throw RegraNegocioException("Estoque insuficiente de \"$nome\" (disponível: $estoque).")
                w.insertOrThrow(
                    "item_venda", null,
                    valores(
                        "venda_id" to vendaId,
                        "produto_id" to item.produtoId,
                        "quantidade" to item.quantidade,
                        "preco_unitario" to preco,
                        "custo_unitario" to custo,
                        "subtotal" to preco * item.quantidade,
                    ),
                )
                // Baixa automática; o CHECK (estoque_atual >= 0) do banco é a última barreira
                w.execSQL(
                    "UPDATE produto SET estoque_atual = estoque_atual - ? WHERE id = ?",
                    arrayOf<Any>(item.quantidade, item.produtoId),
                )
            }
            vendaId
        }
        Repositorios.notificarMudanca()
        return id
    }

    /** Cancela a venda e devolve os itens ao estoque (RF11, RN05). */
    fun cancelar(vendaId: Long) {
        db.transacao { w ->
            val alteradas = w.compileStatement(
                "UPDATE venda SET status = 'CANCELADA' WHERE id = ? AND status = 'CONCLUIDA'",
            ).apply { bindLong(1, vendaId) }.executeUpdateDelete()
            if (alteradas == 1) {
                w.execSQL(
                    """UPDATE produto SET estoque_atual = estoque_atual +
                         (SELECT SUM(quantidade) FROM item_venda WHERE venda_id = ? AND produto_id = produto.id)
                       WHERE id IN (SELECT produto_id FROM item_venda WHERE venda_id = ?)""",
                    arrayOf(vendaId, vendaId),
                )
            }
        }
        Repositorios.notificarMudanca()
    }

    private val selectVenda = """
        SELECT v.*, c.nome AS cliente_nome,
               (SELECT COALESCE(SUM(quantidade),0) FROM item_venda WHERE venda_id = v.id) AS qtd_itens
        FROM venda v LEFT JOIN cliente c ON c.id = v.cliente_id
    """

    private fun mapear(c: Cursor) = Venda(
        id = c.long("id"),
        clienteId = c.longOrNull("cliente_id"),
        clienteNome = c.str("cliente_nome"),
        dataHora = c.txt("data_hora"),
        canal = runCatching { Canal.valueOf(c.txt("canal")) }.getOrDefault(Canal.OUTRO),
        formaPagamento = runCatching { FormaPagamento.valueOf(c.txt("forma_pagamento")) }.getOrDefault(FormaPagamento.OUTRO),
        desconto = c.dbl("desconto"),
        valorTotal = c.dbl("valor_total"),
        status = StatusVenda.valueOf(c.txt("status")),
        observacao = c.str("observacao"),
        qtdItens = c.int("qtd_itens"),
    )

    /** Histórico com filtro por situação (RF12). status = null → todas. */
    fun historico(status: StatusVenda? = null, clienteId: Long? = null): List<Venda> {
        val filtros = mutableListOf<String>()
        val args = mutableListOf<Any?>()
        if (status != null) { filtros += "v.status = ?"; args += status.name }
        if (clienteId != null) { filtros += "v.cliente_id = ?"; args += clienteId }
        val where = if (filtros.isEmpty()) "" else "WHERE " + filtros.joinToString(" AND ")
        return db.consultar("$selectVenda $where ORDER BY v.data_hora DESC LIMIT 500", *args.toTypedArray(), mapear = ::mapear)
    }

    fun venda(id: Long): Venda? = db.consultarUm("$selectVenda WHERE v.id = ?", id, mapear = ::mapear)

    fun itens(vendaId: Long): List<ItemVenda> = db.consultar(
        """SELECT iv.*, p.nome AS produto_nome FROM item_venda iv
           JOIN produto p ON p.id = iv.produto_id WHERE iv.venda_id = ? ORDER BY iv.id""",
        vendaId,
    ) { c ->
        ItemVenda(
            id = c.long("id"),
            produtoId = c.long("produto_id"),
            produtoNome = c.txt("produto_nome"),
            quantidade = c.int("quantidade"),
            precoUnitario = c.dbl("preco_unitario"),
            custoUnitario = c.dbl("custo_unitario"),
        )
    }

    private data class Quad(val a: Double, val b: Double, val c: Int, val d: String)
}

package com.meubalcao.app.data.repo

import com.meubalcao.app.data.db.AppDatabase
import com.meubalcao.app.data.db.dbl
import com.meubalcao.app.data.db.int
import com.meubalcao.app.data.db.txt
import com.meubalcao.app.data.model.Indicadores
import com.meubalcao.app.data.model.Linha
import com.meubalcao.app.data.model.MetaMes
import java.time.LocalDate

/**
 * Consultas analíticas do painel e dos relatórios (RF21 a RF25).
 * São as mesmas consultas de database/consultas.sql, parametrizadas pelo período.
 * [dias] = 1 (hoje), 7, 30 ou 90. Somente vendas CONCLUÍDAS entram nos números (RN05).
 */
class RelatorioRepository(private val db: AppDatabase) {

    private fun desde(dias: Int) = "date('now','localtime','-${dias - 1} days')"

    fun indicadores(dias: Int): Indicadores {
        val base = db.consultarUm(
            """SELECT COUNT(*) AS vendas, COALESCE(SUM(valor_total),0) AS faturamento,
                      COALESCE(AVG(valor_total),0) AS ticket
               FROM venda WHERE status = 'CONCLUIDA' AND date(data_hora) >= ${desde(dias)}""",
        ) { c -> Triple(c.int("vendas"), c.dbl("faturamento"), c.dbl("ticket")) }!!
        // RN07: lucro bruto = valor total das vendas concluídas - custo dos itens vendidos
        val custo = db.consultarUm(
            """SELECT COALESCE(SUM(iv.custo_unitario * iv.quantidade),0)
               FROM item_venda iv JOIN venda v ON v.id = iv.venda_id
               WHERE v.status = 'CONCLUIDA' AND date(v.data_hora) >= ${desde(dias)}""",
        ) { it.getDouble(0) }!!
        return Indicadores(base.first, base.second, base.third, base.second - custo)
    }

    /** Faturamento por dia, preenchendo com zero os dias sem venda (gráficos T02/T11). */
    fun faturamentoPorDia(dias: Int): List<Linha> {
        val mapa = db.consultar(
            """SELECT date(data_hora) AS dia, COUNT(*) AS vendas, SUM(valor_total) AS total
               FROM venda WHERE status = 'CONCLUIDA' AND date(data_hora) >= ${desde(dias)}
               GROUP BY date(data_hora)""",
        ) { c -> c.txt("dia") to Linha(c.txt("dia"), c.int("vendas"), c.dbl("total")) }.toMap()
        val hoje = LocalDate.now()
        return (dias - 1 downTo 0).map { d ->
            val dia = hoje.minusDays(d.toLong()).toString()
            mapa[dia] ?: Linha(dia, 0, 0.0)
        }
    }

    fun maisVendidos(dias: Int, limite: Int = 5): List<Linha> = db.consultar(
        """SELECT p.nome, SUM(iv.quantidade) AS unidades, SUM(iv.subtotal) AS receita
           FROM item_venda iv JOIN venda v ON v.id = iv.venda_id JOIN produto p ON p.id = iv.produto_id
           WHERE v.status = 'CONCLUIDA' AND date(v.data_hora) >= ${desde(dias)}
           GROUP BY p.id ORDER BY unidades DESC, receita DESC LIMIT $limite""",
    ) { c -> Linha(c.txt("nome"), c.int("unidades"), c.dbl("receita")) }

    fun melhoresClientes(dias: Int, limite: Int = 5): List<Linha> = db.consultar(
        """SELECT c.nome, COUNT(v.id) AS compras, SUM(v.valor_total) AS total
           FROM venda v JOIN cliente c ON c.id = v.cliente_id
           WHERE v.status = 'CONCLUIDA' AND date(v.data_hora) >= ${desde(dias)}
           GROUP BY c.id ORDER BY total DESC LIMIT $limite""",
    ) { c -> Linha(c.txt("nome"), c.int("compras"), c.dbl("total")) }

    fun porCanal(dias: Int): List<Linha> = db.consultar(
        """SELECT canal, COUNT(*) AS vendas, SUM(valor_total) AS total FROM venda
           WHERE status = 'CONCLUIDA' AND date(data_hora) >= ${desde(dias)}
           GROUP BY canal ORDER BY total DESC""",
    ) { c -> Linha(c.txt("canal"), c.int("vendas"), c.dbl("total")) }

    fun porPagamento(dias: Int): List<Linha> = db.consultar(
        """SELECT forma_pagamento, COUNT(*) AS vendas, SUM(valor_total) AS total FROM venda
           WHERE status = 'CONCLUIDA' AND date(data_hora) >= ${desde(dias)}
           GROUP BY forma_pagamento ORDER BY vendas DESC""",
    ) { c -> Linha(c.txt("forma_pagamento"), c.int("vendas"), c.dbl("total")) }

    /** Progresso da meta do mês atual (RF22). */
    fun metaDoMes(): MetaMes? = db.consultarUm(
        """SELECT m.ano_mes, m.valor_meta,
                  COALESCE((SELECT SUM(v.valor_total) FROM venda v
                            WHERE v.status = 'CONCLUIDA' AND strftime('%Y-%m', v.data_hora) = m.ano_mes),0) AS realizado
           FROM meta_mensal m WHERE m.ano_mes = strftime('%Y-%m','now','localtime')""",
    ) { c -> MetaMes(c.txt("ano_mes"), c.dbl("valor_meta"), c.dbl("realizado")) }

    /** Linhas do CSV exportado (RF25): uma linha por item vendido no período. */
    fun linhasCsv(dias: Int): List<List<String>> = db.consultar(
        """SELECT v.id, v.data_hora, COALESCE(c.nome,'') AS cliente, v.canal, v.forma_pagamento, v.status,
                  p.nome AS produto, iv.quantidade, iv.preco_unitario, iv.subtotal, v.desconto, v.valor_total
           FROM venda v JOIN item_venda iv ON iv.venda_id = v.id JOIN produto p ON p.id = iv.produto_id
           LEFT JOIN cliente c ON c.id = v.cliente_id
           WHERE date(v.data_hora) >= ${desde(dias)} ORDER BY v.data_hora, v.id""",
    ) { c -> (0 until c.columnCount).map { c.getString(it) ?: "" } }
}

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
import com.meubalcao.app.data.model.Categoria
import com.meubalcao.app.data.model.Linha
import com.meubalcao.app.data.model.Produto

/** Tabelas categoria e produto (RF05 a RF09). */
class CatalogoRepository(private val db: AppDatabase) {

    // ---------------------------------------------------------------- categorias

    fun categorias(somenteAtivas: Boolean = false): List<Categoria> = db.consultar(
        """SELECT c.*, (SELECT COUNT(*) FROM produto p WHERE p.categoria_id = c.id) AS qtd
           FROM categoria c ${if (somenteAtivas) "WHERE c.ativa = 1" else ""}
           ORDER BY c.ativa DESC, c.nome COLLATE NOCASE""",
    ) { c -> Categoria(c.long("id"), c.txt("nome"), c.str("descricao"), c.bool("ativa"), c.int("qtd")) }

    fun salvarCategoria(cat: Categoria): Long {
        val v = valores("nome" to cat.nome.trim(), "descricao" to cat.descricao?.trim()?.ifEmpty { null }, "ativa" to cat.ativa)
        val w = db.writableDatabase
        val id = if (cat.id == 0L) w.insertOrThrow("categoria", null, v)
        else { w.update("categoria", v, "id = ?", arrayOf(cat.id.toString())); cat.id }
        Repositorios.notificarMudanca()
        return id
    }

    // ---------------------------------------------------------------- produtos

    private fun mapear(c: Cursor) = Produto(
        id = c.long("id"),
        categoriaId = c.long("categoria_id"),
        categoriaNome = c.txt("categoria_nome"),
        nome = c.txt("nome"),
        descricao = c.str("descricao"),
        preco = c.dbl("preco"),
        custo = c.dbl("custo"),
        estoqueAtual = c.int("estoque_atual"),
        estoqueMinimo = c.int("estoque_minimo"),
        imagemPath = c.str("imagem_path"),
        destaque = c.bool("destaque"),
        ativo = c.bool("ativo"),
    )

    private val selectProduto =
        "SELECT p.*, c.nome AS categoria_nome FROM produto p JOIN categoria c ON c.id = p.categoria_id"

    /** Busca por nome e filtro por categoria (RF07). */
    fun produtos(busca: String = "", categoriaId: Long? = null, incluirInativos: Boolean = true): List<Produto> {
        val filtros = mutableListOf("p.nome LIKE ?")
        val args = mutableListOf<Any?>("%${busca.trim()}%")
        if (categoriaId != null) { filtros += "p.categoria_id = ?"; args += categoriaId }
        if (!incluirInativos) filtros += "p.ativo = 1"
        return db.consultar(
            "$selectProduto WHERE ${filtros.joinToString(" AND ")} ORDER BY p.ativo DESC, p.destaque DESC, p.nome COLLATE NOCASE",
            *args.toTypedArray(), mapear = ::mapear,
        )
    }

    fun produto(id: Long): Produto? = db.consultarUm("$selectProduto WHERE p.id = ?", id, mapear = ::mapear)

    fun destaques(): List<Produto> =
        db.consultar("$selectProduto WHERE p.ativo = 1 ORDER BY p.destaque DESC, p.nome LIMIT 8", mapear = ::mapear)

    /** Alerta de estoque baixo (RF09). */
    fun estoqueBaixo(): List<Produto> = db.consultar(
        "$selectProduto WHERE p.ativo = 1 AND p.estoque_atual <= p.estoque_minimo ORDER BY (p.estoque_atual - p.estoque_minimo)",
        mapear = ::mapear,
    )

    fun salvarProduto(p: Produto): Long {
        val v = valores(
            "categoria_id" to p.categoriaId,
            "nome" to p.nome.trim(),
            "descricao" to p.descricao?.trim()?.ifEmpty { null },
            "preco" to p.preco,
            "custo" to p.custo,
            "estoque_atual" to p.estoqueAtual,
            "estoque_minimo" to p.estoqueMinimo,
            "imagem_path" to p.imagemPath,
            "destaque" to p.destaque,
            "ativo" to p.ativo,
        )
        val w = db.writableDatabase
        val id = if (p.id == 0L) w.insertOrThrow("produto", null, v)
        else { w.update("produto", v, "id = ?", arrayOf(p.id.toString())); p.id }
        Repositorios.notificarMudanca()
        return id
    }

    /** Unidades e receita do produto nos últimos 30 dias (tela T05). */
    fun desempenho30Dias(produtoId: Long): Linha = db.consultarUm(
        """SELECT COALESCE(SUM(iv.quantidade),0) AS unidades, COALESCE(SUM(iv.subtotal),0) AS receita
           FROM item_venda iv JOIN venda v ON v.id = iv.venda_id
           WHERE iv.produto_id = ? AND v.status = 'CONCLUIDA'
             AND date(v.data_hora) >= date('now','localtime','-29 days')""",
        produtoId,
    ) { c -> Linha("30 dias", c.int("unidades"), c.dbl("receita")) }!!

    /** Unidades vendidas por dia nos últimos 7 dias (gráfico da tela T05). */
    fun unidadesPorDia(produtoId: Long): Map<String, Double> = db.consultar(
        """SELECT date(v.data_hora) AS dia, SUM(iv.quantidade) AS unidades
           FROM item_venda iv JOIN venda v ON v.id = iv.venda_id
           WHERE iv.produto_id = ? AND v.status = 'CONCLUIDA'
             AND date(v.data_hora) >= date('now','localtime','-6 days')
           GROUP BY date(v.data_hora)""",
        produtoId,
    ) { c -> c.txt("dia") to c.dbl("unidades") }.toMap()
}

package com.meubalcao.app

import android.database.sqlite.SQLiteConstraintException
import androidx.test.core.app.ApplicationProvider
import com.meubalcao.app.data.db.AppDatabase
import com.meubalcao.app.data.db.SqlScript
import com.meubalcao.app.data.model.Canal
import com.meubalcao.app.data.model.Categoria
import com.meubalcao.app.data.model.Cliente
import com.meubalcao.app.data.model.FormaPagamento
import com.meubalcao.app.data.model.ItemVenda
import com.meubalcao.app.data.model.Produto
import com.meubalcao.app.data.model.Segmento
import com.meubalcao.app.data.model.StatusVenda
import com.meubalcao.app.data.model.Venda
import com.meubalcao.app.data.repo.RegraNegocioException
import com.meubalcao.app.data.repo.Repositorios
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

/**
 * Testes do banco local executados no computador com o SQLite real do Android (Robolectric).
 * Cobrem os casos planejados em docs/10-banco-local.md, item 10.8.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class BancoDeDadosTest {

    private lateinit var db: AppDatabase

    @Before
    fun abrir() {
        AppDatabase.reiniciar()
        Repositorios.iniciar(ApplicationProvider.getApplicationContext(), forcar = true)
        db = Repositorios.db
    }

    @After
    fun fechar() = AppDatabase.reiniciar()

    private fun contar(sql: String, vararg args: Any?) = db.consultarUm(sql, *args) { it.getInt(0) }!!
    private fun estoque(id: Long) = Repositorios.catalogo.produto(id)!!.estoqueAtual

    private fun produtoTeste(estoque: Int = 10): Long {
        val cat = Repositorios.catalogo.salvarCategoria(Categoria(nome = "Bolos"))
        return Repositorios.catalogo.salvarProduto(Produto(categoriaId = cat, nome = "Bolo", preco = 40.0, custo = 15.0, estoqueAtual = estoque, estoqueMinimo = 2))
    }

    @Test
    fun `esquema cria as 9 tabelas com chaves estrangeiras ativas`() {
        val tabelas = db.consultar("SELECT name FROM sqlite_master WHERE type='table' AND name NOT LIKE 'sqlite_%' AND name NOT LIKE 'android_%'") { it.getString(0) }
        assertEquals(AppDatabase.TABELAS.sorted(), tabelas.sorted())
        assertEquals(1, contar("PRAGMA foreign_keys"))
    }

    @Test
    fun `dados de exemplo carregam do seed sql`() {
        db.carregarExemplo()
        assertEquals(1, contar("SELECT COUNT(*) FROM perfil_negocio"))
        assertEquals(4, contar("SELECT COUNT(*) FROM categoria"))
        assertEquals(12, contar("SELECT COUNT(*) FROM produto"))
        assertEquals(10, contar("SELECT COUNT(*) FROM cliente"))
        assertTrue(contar("SELECT COUNT(*) FROM venda") >= 100)
        assertEquals(3, contar("SELECT COUNT(*) FROM campanha"))
        assertNotNull(Repositorios.relatorios.metaDoMes())
    }

    @Test
    fun `venda baixa o estoque e copia preco e custo`() {
        val p = produtoTeste(10)
        val id = Repositorios.vendas.registrar(
            Venda(canal = Canal.WHATSAPP, formaPagamento = FormaPagamento.PIX, desconto = 5.0),
            listOf(ItemVenda(produtoId = p, quantidade = 3, precoUnitario = 0.0)),
        )
        assertEquals(7, estoque(p))
        val v = Repositorios.vendas.venda(id)!!
        assertEquals(115.0, v.valorTotal, 0.001) // 3 × 40 − 5
        val item = Repositorios.vendas.itens(id).single()
        assertEquals(40.0, item.precoUnitario, 0.001)
        assertEquals(15.0, item.custoUnitario, 0.001)
    }

    @Test
    fun `estoque insuficiente desfaz toda a venda`() {
        val p = produtoTeste(2)
        assertThrows(RegraNegocioException::class.java) {
            Repositorios.vendas.registrar(Venda(), listOf(ItemVenda(produtoId = p, quantidade = 5, precoUnitario = 40.0)))
        }
        assertEquals(2, estoque(p))
        assertEquals(0, contar("SELECT COUNT(*) FROM venda"))
    }

    @Test
    fun `check do banco impede estoque negativo`() {
        val p = produtoTeste(1)
        assertThrows(SQLiteConstraintException::class.java) {
            db.writableDatabase.execSQL("UPDATE produto SET estoque_atual = estoque_atual - 5 WHERE id = $p")
        }
    }

    @Test
    fun `desconto maior que os itens e venda sem itens sao recusados`() {
        val p = produtoTeste()
        assertThrows(RegraNegocioException::class.java) {
            Repositorios.vendas.registrar(Venda(desconto = 100.0), listOf(ItemVenda(produtoId = p, quantidade = 1, precoUnitario = 40.0)))
        }
        assertThrows(RegraNegocioException::class.java) { Repositorios.vendas.registrar(Venda(), emptyList()) }
    }

    @Test
    fun `cancelar devolve ao estoque e tira dos relatorios`() {
        val p = produtoTeste(10)
        val id = Repositorios.vendas.registrar(Venda(), listOf(ItemVenda(produtoId = p, quantidade = 4, precoUnitario = 40.0)))
        assertEquals(1, Repositorios.relatorios.indicadores(1).vendas)
        Repositorios.vendas.cancelar(id)
        Repositorios.vendas.cancelar(id) // cancelar de novo não devolve duas vezes
        assertEquals(10, estoque(p))
        assertEquals(StatusVenda.CANCELADA, Repositorios.vendas.venda(id)!!.status)
        assertEquals(0, Repositorios.relatorios.indicadores(1).vendas)
    }

    @Test
    fun `telefone de cliente nao se repete`() {
        Repositorios.clientes.salvar(Cliente(nome = "Ana", telefone = "(54) 99999-0001"))
        assertThrows(SQLiteConstraintException::class.java) {
            Repositorios.clientes.salvar(Cliente(nome = "Outra Ana", telefone = "(54) 99999-0001"))
        }
    }

    @Test
    fun `chave estrangeira inexistente e bloqueada`() {
        assertThrows(SQLiteConstraintException::class.java) {
            Repositorios.catalogo.salvarProduto(Produto(categoriaId = 999, nome = "X", preco = 1.0))
        }
    }

    @Test
    fun `segmentos de clientes seguem as regras RN08`() {
        db.carregarExemplo()
        val todos = Repositorios.clientes.listar()
        val recorrentes = Repositorios.clientes.listar(segmento = Segmento.RECORRENTES)
        assertEquals(10, todos.size)
        assertTrue(recorrentes.isNotEmpty())
        assertTrue(recorrentes.all { it.compras >= 3 })
        assertTrue(Repositorios.clientes.listar(segmento = Segmento.INATIVOS).all { (it.diasSemComprar ?: 0) > 30 })
        assertTrue(Repositorios.clientes.listar(segmento = Segmento.ANIVERSARIANTES).any { it.cliente.nome.startsWith("Mariana") })
        // RN09: o público de campanha só tem quem aceita promoções
        assertTrue(Repositorios.clientes.publico(Segmento.TODOS).all { it.cliente.aceitaPromocoes })
        assertEquals(9, Repositorios.clientes.publico(Segmento.TODOS).size)
    }

    @Test
    fun `consultas do painel batem com os dados`() {
        db.carregarExemplo()
        val r = Repositorios.relatorios
        val ind = r.indicadores(30)
        assertTrue(ind.vendas > 0)
        assertEquals(ind.faturamento / ind.vendas, ind.ticketMedio, 0.01)
        assertTrue(ind.lucroBruto in 0.0..ind.faturamento)
        assertEquals(7, r.faturamentoPorDia(7).size)
        assertTrue(r.maisVendidos(30).size <= 5)
        assertTrue(Repositorios.catalogo.estoqueBaixo().any { it.nome == "Torta de limão" })
        assertTrue(Repositorios.campanhas.listar().first { it.titulo == "Semana do Brownie" }.alcancados == 4)
    }

    @Test
    fun `backup e restauracao preservam os dados`() {
        db.carregarExemplo()
        val copia = ByteArrayOutputStream().also { db.exportar(it) }.toByteArray()
        db.apagarTudo()
        assertEquals(0, contar("SELECT COUNT(*) FROM produto"))
        db.restaurar(ByteArrayInputStream(copia))
        assertEquals(12, contar("SELECT COUNT(*) FROM produto"))
        assertThrows(IllegalArgumentException::class.java) { db.restaurar(ByteArrayInputStream("nao e banco".toByteArray())) }
        assertEquals(12, contar("SELECT COUNT(*) FROM produto"))
    }

    @Test
    fun `divisor de script respeita textos com ponto e virgula`() {
        val cmds = SqlScript.dividir("-- comentario\nINSERT INTO t VALUES ('a; b -- c');\nSELECT 1;")
        assertEquals(listOf("INSERT INTO t VALUES ('a; b -- c')", "SELECT 1"), cmds)
    }
}

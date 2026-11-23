package com.meubalcao.app

import com.meubalcao.app.ui.screens.integrantes
import com.meubalcao.app.ui.screens.personalizar
import com.meubalcao.app.util.Compartilhar
import com.meubalcao.app.util.Fmt
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FormatosTest {
    @Test
    fun `le valores digitados em reais`() {
        assertEquals(12.5, Fmt.lerValor("12,50")!!, 0.001)
        assertEquals(12.5, Fmt.lerValor("12.50")!!, 0.001)
        assertEquals(1234.5, Fmt.lerValor("1.234,50")!!, 0.001)
        assertNull(Fmt.lerValor("abc"))
    }

    @Test
    fun `converte datas entre o formato brasileiro e o do banco`() {
        assertEquals("1992-03-14", Fmt.dataParaBanco("14/03/1992"))
        assertNull(Fmt.dataParaBanco("31/02/2020"))
        assertEquals("14/03/1992", Fmt.dataDoBanco("1992-03-14"))
    }

    @Test
    fun `telefone recebe codigo do Brasil para o WhatsApp`() {
        assertEquals("5554999990001", Compartilhar.telefoneInternacional("(54) 99999-0001"))
    }

    @Test
    fun `mensagem de campanha e personalizada`() {
        val m = personalizar("Oi, {nome}! {produto} na {negocio}", "Mariana Souza", "Doce Ponto", "Brownie")
        assertEquals("Oi, Mariana! Brownie na Doce Ponto", m)
    }

    @Test
    fun `tela Sobre lista os quatro integrantes com nome completo`() {
        assertEquals(4, integrantes.size)
        assertTrue(integrantes.all { it.nome.split(" ").size >= 3 })
    }
}

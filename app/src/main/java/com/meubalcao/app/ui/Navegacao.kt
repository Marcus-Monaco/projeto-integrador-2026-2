package com.meubalcao.app.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.meubalcao.app.ui.screens.AberturaScreen
import com.meubalcao.app.ui.screens.CampanhaEnvioScreen
import com.meubalcao.app.ui.screens.CampanhaFormScreen
import com.meubalcao.app.ui.screens.CampanhasScreen
import com.meubalcao.app.ui.screens.CatalogoScreen
import com.meubalcao.app.ui.screens.CategoriasScreen
import com.meubalcao.app.ui.screens.ClienteDetalheScreen
import com.meubalcao.app.ui.screens.ClienteFormScreen
import com.meubalcao.app.ui.screens.ClientesScreen
import com.meubalcao.app.ui.screens.ConfiguracoesScreen
import com.meubalcao.app.ui.screens.HistoricoVendasScreen
import com.meubalcao.app.ui.screens.MaisScreen
import com.meubalcao.app.ui.screens.MeuNegocioScreen
import com.meubalcao.app.ui.screens.NovaVendaScreen
import com.meubalcao.app.ui.screens.PainelScreen
import com.meubalcao.app.ui.screens.ProdutoDetalheScreen
import com.meubalcao.app.ui.screens.ProdutoFormScreen
import com.meubalcao.app.ui.screens.RelatoriosScreen
import com.meubalcao.app.ui.screens.SobreScreen
import com.meubalcao.app.ui.screens.VendaDetalheScreen
import com.meubalcao.app.ui.theme.Cores

/** Rotas das telas (T01 a T17 dos wireframes + telas auxiliares de cadastro). */
object Rotas {
    const val ABERTURA = "abertura"                  // T01
    const val PAINEL = "painel"                      // T02
    const val CATALOGO = "catalogo"                  // T03
    const val PRODUTO_FORM = "produto_form?id={id}"  // T04
    const val PRODUTO = "produto/{id}"               // T05
    const val NOVA_VENDA = "nova_venda?clienteId={clienteId}" // T06
    const val VENDAS = "vendas"                      // T07
    const val VENDA = "venda/{id}"                   // T08
    const val CLIENTES = "clientes"                  // T09
    const val CLIENTE = "cliente/{id}"               // T10
    const val CLIENTE_FORM = "cliente_form?id={id}"
    const val RELATORIOS = "relatorios"              // T11
    const val CAMPANHAS = "campanhas"                // T12
    const val CAMPANHA_FORM = "campanha_form?id={id}&clienteId={clienteId}" // T13
    const val CAMPANHA_ENVIO = "campanha_envio/{id}"
    const val NEGOCIO = "negocio"                    // T14
    const val MAIS = "mais"                          // T15
    const val CONFIG = "config"                      // T16
    const val SOBRE = "sobre"                        // T17
    const val CATEGORIAS = "categorias"

    fun produtoForm(id: Long = 0) = "produto_form?id=$id"
    fun produto(id: Long) = "produto/$id"
    fun novaVenda(clienteId: Long = 0) = "nova_venda?clienteId=$clienteId"
    fun venda(id: Long) = "venda/$id"
    fun cliente(id: Long) = "cliente/$id"
    fun clienteForm(id: Long = 0) = "cliente_form?id=$id"
    fun campanhaForm(id: Long = 0, clienteId: Long = 0) = "campanha_form?id=$id&clienteId=$clienteId"
    fun campanhaEnvio(id: Long) = "campanha_envio/$id"
}

private data class Aba(val rota: String, val rotulo: String, val icone: androidx.compose.ui.graphics.vector.ImageVector)

private val abas = listOf(
    Aba(Rotas.PAINEL, "Início", Icons.Default.Home),
    Aba(Rotas.CATALOGO, "Catálogo", Icons.Default.Storefront),
    Aba(Rotas.NOVA_VENDA, "Vender", Icons.Default.Add),
    Aba(Rotas.CLIENTES, "Clientes", Icons.Default.People),
    Aba(Rotas.MAIS, "Mais", Icons.Default.Menu),
)

@Composable
fun MeuBalcaoApp() {
    val nav = rememberNavController()
    val entrada by nav.currentBackStackEntryAsState()
    val rotaAtual = entrada?.destination?.route
    val mostrarBarra = rotaAtual in setOf(Rotas.PAINEL, Rotas.CATALOGO, Rotas.CLIENTES, Rotas.MAIS)

    Scaffold(
        containerColor = Cores.Fundo,
        bottomBar = { if (mostrarBarra) BarraInferior(nav, rotaAtual) },
    ) { pad ->
        NavHost(
            nav, startDestination = Rotas.ABERTURA,
            modifier = Modifier.padding(pad).consumeWindowInsets(pad).imePadding(),
        ) {
            composable(Rotas.ABERTURA) {
                AberturaScreen { nav.navigate(Rotas.PAINEL) { popUpTo(Rotas.ABERTURA) { inclusive = true } } }
            }
            composable(Rotas.PAINEL) { PainelScreen(nav) }
            composable(Rotas.CATALOGO) { CatalogoScreen(nav) }
            composable(Rotas.PRODUTO_FORM, listOf(argLong("id"))) { ProdutoFormScreen(nav, it.arguments!!.getLong("id")) }
            composable(Rotas.PRODUTO, listOf(argLong("id"))) { ProdutoDetalheScreen(nav, it.arguments!!.getLong("id")) }
            composable(Rotas.NOVA_VENDA, listOf(argLong("clienteId"))) { NovaVendaScreen(nav, it.arguments!!.getLong("clienteId")) }
            composable(Rotas.VENDAS) { HistoricoVendasScreen(nav) }
            composable(Rotas.VENDA, listOf(argLong("id"))) { VendaDetalheScreen(nav, it.arguments!!.getLong("id")) }
            composable(Rotas.CLIENTES) { ClientesScreen(nav) }
            composable(Rotas.CLIENTE, listOf(argLong("id"))) { ClienteDetalheScreen(nav, it.arguments!!.getLong("id")) }
            composable(Rotas.CLIENTE_FORM, listOf(argLong("id"))) { ClienteFormScreen(nav, it.arguments!!.getLong("id")) }
            composable(Rotas.RELATORIOS) { RelatoriosScreen(nav) }
            composable(Rotas.CAMPANHAS) { CampanhasScreen(nav) }
            composable(Rotas.CAMPANHA_FORM, listOf(argLong("id"), argLong("clienteId"))) {
                CampanhaFormScreen(nav, it.arguments!!.getLong("id"), it.arguments!!.getLong("clienteId"))
            }
            composable(Rotas.CAMPANHA_ENVIO, listOf(argLong("id"))) { CampanhaEnvioScreen(nav, it.arguments!!.getLong("id")) }
            composable(Rotas.NEGOCIO) { MeuNegocioScreen(nav) }
            composable(Rotas.MAIS) { MaisScreen(nav) }
            composable(Rotas.CONFIG) { ConfiguracoesScreen(nav) }
            composable(Rotas.SOBRE) { SobreScreen(nav) }
            composable(Rotas.CATEGORIAS) { CategoriasScreen(nav) }
        }
    }
}

private fun argLong(nome: String) = navArgument(nome) { type = NavType.LongType; defaultValue = 0L }

@Composable
private fun BarraInferior(nav: NavHostController, rotaAtual: String?) {
    NavigationBar(containerColor = Color.White, tonalElevation = 0.dp) {
        abas.forEach { aba ->
            if (aba.rota == Rotas.NOVA_VENDA) {
                // Botão central "Vender" em destaque: a venda fica sempre a um toque
                Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    FloatingActionButton(
                        onClick = { nav.navigate(Rotas.novaVenda()) },
                        shape = CircleShape, containerColor = Cores.Primaria, contentColor = Color.White,
                        modifier = Modifier.size(56.dp),
                    ) { Icon(Icons.Default.Add, "Vender") }
                }
            } else {
                NavigationBarItem(
                    selected = rotaAtual == aba.rota,
                    onClick = {
                        nav.navigate(aba.rota) {
                            popUpTo(Rotas.PAINEL) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = { Icon(aba.icone, aba.rotulo) },
                    label = { Text(aba.rotulo) },
                    colors = NavigationBarItemDefaults.colors(indicatorColor = Cores.PrimariaClara, selectedIconColor = Cores.Primaria, selectedTextColor = Cores.Primaria),
                )
            }
        }
    }
}

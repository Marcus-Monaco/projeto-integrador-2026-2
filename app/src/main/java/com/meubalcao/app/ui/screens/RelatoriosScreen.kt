package com.meubalcao.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.meubalcao.app.data.model.Canal
import com.meubalcao.app.data.model.FormaPagamento
import com.meubalcao.app.data.repo.Repositorios
import com.meubalcao.app.ui.components.BarrasHorizontais
import com.meubalcao.app.ui.components.Cartao
import com.meubalcao.app.ui.components.Filtros
import com.meubalcao.app.ui.components.GraficoColunas
import com.meubalcao.app.ui.components.Indicador
import com.meubalcao.app.ui.components.Tela
import com.meubalcao.app.ui.components.carregar
import com.meubalcao.app.ui.components.gravar
import com.meubalcao.app.ui.theme.Cores
import com.meubalcao.app.util.Compartilhar
import com.meubalcao.app.util.Csv
import com.meubalcao.app.util.Fmt

private val periodos = listOf(7 to "7 dias", 30 to "30 dias", 90 to "90 dias")
private val abas = listOf("Vendas", "Produtos", "Clientes", "Canais")

/** T11 · Relatórios por período, com gráficos, rankings e exportação CSV (RF23 a RF25). */
@Composable
fun RelatoriosScreen(nav: NavController) {
    val context = LocalContext.current
    val escopo = rememberCoroutineScope()
    var dias by rememberSaveable { mutableIntStateOf(30) }
    var aba by rememberSaveable { mutableStateOf(abas[0]) }
    val r = Repositorios.relatorios
    val d = carregar(dias) {
        RelDados(r.indicadores(dias), r.faturamentoPorDia(dias), r.maisVendidos(dias, 10), r.melhoresClientes(dias, 10), r.porCanal(dias), r.porPagamento(dias))
    }

    Tela("Relatórios", voltar = { nav.popBackStack() }, acoes = {
        IconButton(onClick = {
            escopo.gravar(context, depois = { f -> Compartilhar.arquivoGerado(context, f as java.io.File, "text/csv", "Relatório de vendas - últimos $dias dias") }) {
                val arquivo = Compartilhar.arquivoTemporario(context, "vendas-${dias}dias-${Fmt.anoMesAtual()}.csv")
                Csv.escrever(arquivo, Csv.CABECALHO_VENDAS, r.linhasCsv(dias))
                arquivo
            }
        }) { Icon(Icons.Default.FileDownload, "Exportar CSV") }
    }) { pad ->
        val dados = d ?: return@Tela
        LazyColumn(Modifier.fillMaxSize().padding(pad), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item { Filtros(periodos, periodos.first { it.first == dias }, { it.second }, { dias = it.first }) }
            item { Filtros(abas, aba, { it }, { aba = it }) }
            when (aba) {
                "Vendas" -> {
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Indicador("Faturamento", Fmt.moeda(dados.ind.faturamento), Modifier.weight(1f))
                            Indicador("Vendas", dados.ind.vendas.toString(), Modifier.weight(1f))
                        }
                    }
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Indicador("Ticket médio", Fmt.moeda(dados.ind.ticketMedio), Modifier.weight(1f))
                            Indicador("Lucro bruto", Fmt.moeda(dados.ind.lucroBruto), Modifier.weight(1f))
                        }
                    }
                    item {
                        Cartao {
                            Text("Faturamento por dia", fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(12.dp))
                            GraficoColunas(dados.porDia.map { Fmt.diaCurto(it.rotulo) to it.valor }, altura = 160, destacarUltimo = false)
                        }
                    }
                    item {
                        val melhor = dados.porDia.maxByOrNull { it.valor }
                        val pag = dados.pagamento.firstOrNull()
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Indicador("Melhor dia", melhor?.takeIf { it.valor > 0 }?.let { "${Fmt.diaCurto(it.rotulo)} · R$ ${Fmt.numero(it.valor, 0)}" } ?: "-", Modifier.weight(1f))
                            Indicador("Pagamento + usado", pag?.let { rotuloPagamento(it.rotulo) } ?: "-", Modifier.weight(1f))
                        }
                    }
                    if (dados.pagamento.isNotEmpty()) item {
                        Cartao {
                            Text("Formas de pagamento", fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(12.dp))
                            BarrasHorizontais(dados.pagamento.map { Triple(rotuloPagamento(it.rotulo), it.quantidade.toDouble(), "${it.quantidade} venda(s)") }, colorido = true)
                        }
                    }
                }
                "Produtos" -> item {
                    Cartao {
                        Text("Produtos mais vendidos", fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(12.dp))
                        if (dados.produtos.isEmpty()) Text("Nenhuma venda no período.", color = Cores.TextoApoio)
                        BarrasHorizontais(dados.produtos.map { Triple(it.rotulo, it.quantidade.toDouble(), "${it.quantidade} un. · ${Fmt.moeda(it.valor)}") })
                    }
                }
                "Clientes" -> item {
                    Cartao {
                        Text("Melhores clientes", fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(12.dp))
                        if (dados.clientes.isEmpty()) Text("Nenhuma venda com cliente identificado no período.", color = Cores.TextoApoio)
                        BarrasHorizontais(dados.clientes.map { Triple(it.rotulo, it.valor, "${Fmt.moeda(it.valor)} · ${it.quantidade}x") })
                    }
                }
                else -> item {
                    Cartao {
                        Text("Faturamento por canal de venda", fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(12.dp))
                        if (dados.canais.isEmpty()) Text("Nenhuma venda no período.", color = Cores.TextoApoio)
                        val total = dados.canais.sumOf { it.valor }.coerceAtLeast(0.01)
                        BarrasHorizontais(
                            dados.canais.map {
                                Triple(rotuloCanal(it.rotulo), it.valor, "${Fmt.moeda(it.valor)} (${Fmt.numero(it.valor / total * 100, 0)}%)")
                            },
                            colorido = true,
                        )
                    }
                }
            }
            item {
                Text(
                    "Somente vendas concluídas entram nos números. Use o ícone de download para exportar as vendas do período em CSV.",
                    color = Cores.TextoApoio, style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

private fun rotuloCanal(s: String) = runCatching { Canal.valueOf(s).rotulo }.getOrDefault(s)
private fun rotuloPagamento(s: String) = runCatching { FormaPagamento.valueOf(s).rotulo }.getOrDefault(s)

private data class RelDados(
    val ind: com.meubalcao.app.data.model.Indicadores,
    val porDia: List<com.meubalcao.app.data.model.Linha>,
    val produtos: List<com.meubalcao.app.data.model.Linha>,
    val clientes: List<com.meubalcao.app.data.model.Linha>,
    val canais: List<com.meubalcao.app.data.model.Linha>,
    val pagamento: List<com.meubalcao.app.data.model.Linha>,
)

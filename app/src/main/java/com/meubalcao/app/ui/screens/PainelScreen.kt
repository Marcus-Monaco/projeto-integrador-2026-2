package com.meubalcao.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.meubalcao.app.data.repo.Repositorios
import com.meubalcao.app.ui.Rotas
import com.meubalcao.app.ui.components.BarraProgresso
import com.meubalcao.app.ui.components.BarrasHorizontais
import com.meubalcao.app.ui.components.Cartao
import com.meubalcao.app.ui.components.Filtros
import com.meubalcao.app.ui.components.GraficoColunas
import com.meubalcao.app.ui.components.Indicador
import com.meubalcao.app.ui.components.Secao
import com.meubalcao.app.ui.components.Tela
import com.meubalcao.app.ui.components.Vazio
import com.meubalcao.app.ui.components.carregar
import com.meubalcao.app.ui.components.gravar
import com.meubalcao.app.ui.theme.Cores
import com.meubalcao.app.util.Fmt

/** Períodos do painel: hoje, 7 e 30 dias. */
val periodosPainel = listOf(1 to "Hoje", 7 to "7 dias", 30 to "30 dias")

/** T02 · Painel inicial (RF09, RF21 a RF24). */
@Composable
fun PainelScreen(nav: NavController) {
    var dias by rememberSaveable { mutableIntStateOf(30) }
    val r = Repositorios.relatorios
    val perfil = carregar { Repositorios.perfil.obter() }
    val dados = carregar(dias) {
        PainelDados(
            r.indicadores(dias), r.faturamentoPorDia(7), r.maisVendidos(dias),
            Repositorios.catalogo.estoqueBaixo(), r.metaDoMes(),
            Repositorios.db.consultarUm("SELECT COUNT(*) FROM produto") { it.getInt(0) } ?: 0,
        )
    }
    val context = LocalContext.current
    val escopo = rememberCoroutineScope()

    Tela(titulo = perfil?.nomeFantasia?.ifBlank { null } ?: "MeuBalcão") { pad ->
        if (dados == null) return@Tela
        LazyColumn(
            Modifier.fillMaxSize().padding(pad),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (dados.totalProdutos == 0) {
                item {
                    Cartao {
                        Vazio(
                            Icons.Default.Storefront, "Bem-vindo ao MeuBalcão!",
                            "Cadastre seu negócio e seus produtos para começar a vender. Ou carregue os dados de exemplo para conhecer o app.",
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Button(onClick = { nav.navigate(Rotas.NEGOCIO) }) { Text("Configurar meu negócio") }
                                TextButton(onClick = {
                                    escopo.gravar(context, "Dados de exemplo carregados.") {
                                        Repositorios.db.carregarExemplo(); Repositorios.notificarMudanca()
                                    }
                                }) { Text("Carregar dados de exemplo") }
                            }
                        }
                    }
                }
            }
            item { Filtros(periodosPainel, periodosPainel.first { it.first == dias }, { it.second }, { dias = it.first }) }

            if (dados.estoqueBaixo.isNotEmpty()) item {
                Cartao(onClick = { nav.navigate(Rotas.CATALOGO) }, cor = Cores.AlertaFundo) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Warning, null, tint = Cores.Alerta)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text("${dados.estoqueBaixo.size} produto(s) com estoque baixo", fontWeight = FontWeight.Bold, color = Cores.Alerta)
                            Text(dados.estoqueBaixo.take(3).joinToString { "${it.nome} (${it.estoqueAtual})" }, style = MaterialTheme.typography.bodySmall, maxLines = 2)
                        }
                        Icon(Icons.Default.ChevronRight, null, tint = Cores.Alerta)
                    }
                }
            }

            item {
                val i = dados.indicadores
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Indicador("Faturamento", Fmt.moeda(i.faturamento), Modifier.weight(1f), Icons.Default.AttachMoney)
                        Indicador("Vendas", i.vendas.toString(), Modifier.weight(1f), Icons.Default.Receipt)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Indicador("Ticket médio", Fmt.moeda(i.ticketMedio), Modifier.weight(1f), Icons.AutoMirrored.Filled.TrendingUp)
                        Indicador("Lucro bruto", Fmt.moeda(i.lucroBruto), Modifier.weight(1f), Icons.Default.Savings)
                    }
                }
            }

            item {
                Cartao(onClick = { nav.navigate(Rotas.CONFIG) }) {
                    val m = dados.meta
                    Text("Meta de ${Fmt.mesAtualExtenso()}", fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    if (m == null) {
                        Text("Nenhuma meta definida. Toque para definir a meta do mês.", color = Cores.TextoApoio)
                    } else {
                        Row {
                            Text(Fmt.moeda(m.realizado), fontWeight = FontWeight.Bold, color = Cores.Primaria, modifier = Modifier.weight(1f))
                            Text("de ${Fmt.moeda(m.valorMeta)}", color = Cores.TextoApoio)
                        }
                        Spacer(Modifier.height(8.dp))
                        BarraProgresso(m.percentual, if (m.percentual >= 100) Cores.Sucesso else Cores.Primaria)
                        Spacer(Modifier.height(4.dp))
                        Text("${Fmt.numero(m.percentual)}% da meta", style = MaterialTheme.typography.bodySmall, color = Cores.TextoApoio)
                    }
                }
            }

            item {
                Cartao {
                    Text("Faturamento dos últimos 7 dias", fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(12.dp))
                    GraficoColunas(
                        dados.porDia.map { Fmt.diaSemana(it.rotulo) to it.valor },
                        rotuloValor = { v -> Fmt.numero(v, 0) },
                    )
                }
            }

            item {
                Cartao {
                    Secao("Mais vendidos") {
                        TextButton(onClick = { nav.navigate(Rotas.RELATORIOS) }) { Text("Relatórios") }
                    }
                    if (dados.ranking.isEmpty()) Text("Nenhuma venda no período.", color = Cores.TextoApoio)
                    else BarrasHorizontais(dados.ranking.map { Triple(it.rotulo, it.quantidade.toDouble(), "${it.quantidade} un.") })
                }
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(onClick = { nav.navigate(Rotas.VENDAS) }, modifier = Modifier.weight(1f).height(48.dp)) {
                        Icon(Icons.Default.Receipt, null, Modifier.size(18.dp)); Spacer(Modifier.width(6.dp)); Text("Vendas")
                    }
                    OutlinedButton(onClick = { nav.navigate(Rotas.produtoForm()) }, modifier = Modifier.weight(1f).height(48.dp)) {
                        Icon(Icons.Default.Inventory2, null, Modifier.size(18.dp)); Spacer(Modifier.width(6.dp)); Text("Produto")
                    }
                }
            }
        }
    }
}

private data class PainelDados(
    val indicadores: com.meubalcao.app.data.model.Indicadores,
    val porDia: List<com.meubalcao.app.data.model.Linha>,
    val ranking: List<com.meubalcao.app.data.model.Linha>,
    val estoqueBaixo: List<com.meubalcao.app.data.model.Produto>,
    val meta: com.meubalcao.app.data.model.MetaMes?,
    val totalProdutos: Int,
)

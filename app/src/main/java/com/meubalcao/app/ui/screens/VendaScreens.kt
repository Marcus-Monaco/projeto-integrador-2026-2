package com.meubalcao.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.meubalcao.app.data.model.Canal
import com.meubalcao.app.data.model.Cliente
import com.meubalcao.app.data.model.FormaPagamento
import com.meubalcao.app.data.model.ItemVenda
import com.meubalcao.app.data.model.StatusVenda
import com.meubalcao.app.data.model.Venda
import com.meubalcao.app.data.repo.Repositorios
import com.meubalcao.app.ui.Rotas
import com.meubalcao.app.ui.components.Campo
import com.meubalcao.app.ui.components.Cartao
import com.meubalcao.app.ui.components.Confirmar
import com.meubalcao.app.ui.components.Filtros
import com.meubalcao.app.ui.components.LinhaValor
import com.meubalcao.app.ui.components.Opcoes
import com.meubalcao.app.ui.components.Secao
import com.meubalcao.app.ui.components.Selecao
import com.meubalcao.app.ui.components.Selo
import com.meubalcao.app.ui.components.Tela
import com.meubalcao.app.ui.components.Vazio
import com.meubalcao.app.ui.components.carregar
import com.meubalcao.app.ui.components.gravar
import com.meubalcao.app.ui.theme.Cores
import com.meubalcao.app.util.Compartilhar
import com.meubalcao.app.util.Fmt

/** T06 · Nova venda: itens, cliente opcional, canal, pagamento e desconto (RF10, RN01 a RN04). */
@Composable
fun NovaVendaScreen(nav: NavController, clienteInicial: Long) {
    val context = LocalContext.current
    val escopo = rememberCoroutineScope()
    var busca by rememberSaveable { mutableStateOf("") }
    val produtos = carregar(busca) { Repositorios.catalogo.produtos(busca, incluirInativos = false) } ?: emptyList()
    val clientes = carregar { Repositorios.clientes.todos() } ?: emptyList()

    val carrinho = remember { mutableStateMapOf<Long, Int>() }   // produtoId → quantidade
    var clienteId by rememberSaveable { mutableStateOf(clienteInicial.takeIf { it > 0 }) }
    var canal by rememberSaveable { mutableStateOf(Canal.BALCAO) }
    var pagamento by rememberSaveable { mutableStateOf(FormaPagamento.PIX) }
    var desconto by rememberSaveable { mutableStateOf("") }
    var salvando by remember { mutableStateOf(false) }

    // Guarda os produtos já adicionados para manter preço/nome mesmo se a busca mudar
    val conhecidos = remember { mutableStateMapOf<Long, com.meubalcao.app.data.model.Produto>() }
    LaunchedEffect(produtos) { produtos.forEach { conhecidos[it.id] = it } }

    val itens = carrinho.filterValues { it > 0 }.mapNotNull { (id, q) -> conhecidos[id]?.let { it to q } }
    val subtotal = itens.sumOf { (p, q) -> p.preco * q }
    val descontoNum = Fmt.lerValor(desconto) ?: 0.0
    val total = (subtotal - descontoNum).coerceAtLeast(0.0)
    val erroDesconto = if (descontoNum > subtotal && subtotal > 0) "Maior que a soma dos itens" else null

    Tela(
        "Nova venda", voltar = { nav.popBackStack() },
        rodape = {
            Surface(shadowElevation = 8.dp, color = Color.White) {
                Column(Modifier.padding(16.dp)) {
                    LinhaValor("Subtotal (${itens.sumOf { it.second }} itens)", Fmt.moeda(subtotal))
                    if (descontoNum > 0) LinhaValor("Desconto", "- ${Fmt.moeda(descontoNum)}", cor = Cores.Perigo)
                    LinhaValor("Total", Fmt.moeda(total), destaque = true, cor = Cores.Primaria)
                    Spacer(Modifier.height(8.dp))
                    Button(
                        enabled = itens.isNotEmpty() && erroDesconto == null && !salvando,
                        onClick = {
                            salvando = true
                            escopo.gravar(context, "Venda registrada!", depois = { id ->
                                nav.navigate(Rotas.venda(id as Long)) { popUpTo(Rotas.NOVA_VENDA) { inclusive = true } }
                            }) {
                                try {
                                    Repositorios.vendas.registrar(
                                        Venda(clienteId = clienteId, canal = canal, formaPagamento = pagamento, desconto = descontoNum),
                                        itens.map { (p, q) -> ItemVenda(produtoId = p.id, quantidade = q, precoUnitario = p.preco) },
                                    )
                                } finally { salvando = false }
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                    ) { Text("Finalizar venda · ${Fmt.moeda(total)}") }
                }
            }
        },
    ) { pad ->
        LazyColumn(Modifier.fillMaxSize().padding(pad), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                Selecao(
                    "Cliente (opcional)", listOf<Cliente?>(null) + clientes, clientes.firstOrNull { it.id == clienteId },
                    { it?.let { c -> c.nome + (c.telefone?.let { t -> " · $t" } ?: "") } ?: "Sem cliente (venda de balcão)" },
                    { clienteId = it?.id }, vazio = "Sem cliente (venda de balcão)",
                )
            }
            if (itens.isNotEmpty()) {
                item { Secao("Itens da venda") }
                items(itens, key = { "c" + it.first.id }) { (p, q) ->
                    Cartao {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(p.nome, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text("${Fmt.moeda(p.preco)} × $q = ${Fmt.moeda(p.preco * q)}", style = MaterialTheme.typography.bodySmall, color = Cores.TextoApoio)
                            }
                            IconButton(onClick = { carrinho[p.id] = q - 1 }) { Icon(Icons.Default.RemoveCircleOutline, "Menos", tint = Cores.Primaria) }
                            Text("$q", fontWeight = FontWeight.Bold)
                            IconButton(
                                onClick = { carrinho[p.id] = q + 1 }, enabled = q < p.estoqueAtual,
                            ) { Icon(Icons.Default.AddCircle, "Mais", tint = if (q < p.estoqueAtual) Cores.Primaria else Cores.Borda) }
                        }
                    }
                }
            }
            item { Secao("Adicionar produtos") }
            item {
                OutlinedTextField(
                    busca, { busca = it }, placeholder = { Text("Buscar produto") }, leadingIcon = { Icon(Icons.Default.Search, null) },
                    singleLine = true, shape = RoundedCornerShape(50), modifier = Modifier.fillMaxWidth(),
                )
            }
            if (produtos.isEmpty()) item { Vazio(Icons.Default.Inventory2, "Nenhum produto ativo", "Cadastre produtos no Catálogo para vender.") }
            items(produtos, key = { "p" + it.id }) { p ->
                val q = carrinho[p.id] ?: 0
                val semEstoque = p.estoqueAtual - q <= 0
                Row(
                    Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(12.dp)).padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(p.nome, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(
                            "${Fmt.moeda(p.preco)} · estoque ${p.estoqueAtual}", style = MaterialTheme.typography.bodySmall,
                            color = if (p.estoqueAtual == 0) Cores.Perigo else Cores.TextoApoio,
                        )
                    }
                    IconButton(onClick = { carrinho[p.id] = q + 1 }, enabled = !semEstoque) {
                        Icon(Icons.Default.AddCircle, "Adicionar", tint = if (semEstoque) Cores.Borda else Cores.Primaria, modifier = Modifier.size(32.dp))
                    }
                }
            }
            item { Secao("Canal de venda") }
            item { Opcoes(Canal.entries, canal, { it.rotulo }, { canal = it }) }
            item { Secao("Forma de pagamento") }
            item { Opcoes(FormaPagamento.entries, pagamento, { it.rotulo }, { pagamento = it }) }
            item { Campo("Desconto", desconto, { desconto = it }, teclado = KeyboardType.Decimal, erro = erroDesconto, prefixo = "R$ ") }
        }
    }
}

/** T07 · Histórico de vendas agrupado por dia, com filtro por situação (RF12). */
@Composable
fun HistoricoVendasScreen(nav: NavController) {
    var filtro by rememberSaveable { mutableStateOf<StatusVenda?>(null) }
    val vendas = carregar(filtro) { Repositorios.vendas.historico(filtro) }
    Tela("Histórico de vendas", voltar = { nav.popBackStack() }) { pad ->
        Column(Modifier.fillMaxSize().padding(pad)) {
            Filtros(
                listOf(null, StatusVenda.CONCLUIDA, StatusVenda.CANCELADA), filtro,
                { when (it) { null -> "Todas"; StatusVenda.CONCLUIDA -> "Concluídas"; StatusVenda.CANCELADA -> "Canceladas" } },
                { filtro = it }, Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
            )
            if (vendas?.isEmpty() == true) Vazio(Icons.Default.Receipt, "Nenhuma venda encontrada", "Use o botão Vender para registrar a primeira venda.")
            LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                vendas.orEmpty().groupBy { it.dataHora.take(10) }.forEach { (dia, lista) ->
                    item(key = "d$dia") {
                        Row(Modifier.padding(top = 8.dp)) {
                            Text(Fmt.diaRelativo(dia), fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                            Text(Fmt.moeda(lista.filter { it.status == StatusVenda.CONCLUIDA }.sumOf { it.valorTotal }), color = Cores.TextoApoio)
                        }
                    }
                    items(lista, key = { it.id }) { v -> LinhaVenda(v) { nav.navigate(Rotas.venda(v.id)) } }
                }
            }
        }
    }
}

@Composable
fun LinhaVenda(v: Venda, mostrarData: Boolean = false, aoClicar: () -> Unit) {
    val cancelada = v.status == StatusVenda.CANCELADA
    Cartao(onClick = aoClicar) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(v.clienteNome ?: "Venda de balcão", fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    "${if (mostrarData) Fmt.dataHora(v.dataHora) else Fmt.hora(v.dataHora)} · ${v.canal.rotulo} · ${v.formaPagamento.rotulo} · ${v.qtdItens} it.",
                    style = MaterialTheme.typography.bodySmall, color = Cores.TextoApoio, maxLines = 1,
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    Fmt.moeda(v.valorTotal), fontWeight = FontWeight.Bold,
                    color = if (cancelada) Cores.TextoApoio else Cores.Texto,
                    textDecoration = if (cancelada) TextDecoration.LineThrough else null,
                )
                if (cancelada) Selo("Cancelada", Cores.Perigo, Cores.PerigoFundo)
            }
        }
    }
}

/** T08 · Detalhe da venda e cancelamento com devolução ao estoque (RF11, RN05). */
@Composable
fun VendaDetalheScreen(nav: NavController, id: Long) {
    val context = LocalContext.current
    val escopo = rememberCoroutineScope()
    val dados = carregar(id) { Repositorios.vendas.venda(id) to Repositorios.vendas.itens(id) }
    var confirmar by remember { mutableStateOf(false) }

    Tela("Venda #$id", voltar = { nav.popBackStack() }) { pad ->
        val v = dados?.first ?: return@Tela
        val itens = dados.second
        val cancelada = v.status == StatusVenda.CANCELADA
        Column(Modifier.fillMaxSize().padding(pad).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Cartao(cor = if (cancelada) Cores.PerigoFundo else Cores.PrimariaClara) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(if (cancelada) Icons.Default.Block else Icons.Default.CheckCircle, null, tint = if (cancelada) Cores.Perigo else Cores.Primaria)
                    Spacer(Modifier.width(8.dp))
                    Text(if (cancelada) "Venda cancelada" else "Venda concluída", fontWeight = FontWeight.Bold, color = if (cancelada) Cores.Perigo else Cores.Primaria)
                }
            }
            Cartao {
                LinhaValor("Data", Fmt.dataHora(v.dataHora))
                LinhaValor("Cliente", v.clienteNome ?: "Sem cliente")
                LinhaValor("Canal", v.canal.rotulo)
                LinhaValor("Pagamento", v.formaPagamento.rotulo)
            }
            Cartao {
                Text("Itens", fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                itens.forEach { i ->
                    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Text("${i.quantidade}×", color = Cores.TextoApoio, modifier = Modifier.width(36.dp))
                        Column(Modifier.weight(1f)) {
                            Text(i.produtoNome)
                            Text(Fmt.moeda(i.precoUnitario) + " cada", style = MaterialTheme.typography.bodySmall, color = Cores.TextoApoio)
                        }
                        Text(Fmt.moeda(i.subtotal), fontWeight = FontWeight.Medium)
                    }
                }
                HorizontalDivider(Modifier.padding(vertical = 8.dp), color = Cores.Borda)
                LinhaValor("Subtotal", Fmt.moeda(itens.sumOf { it.subtotal }))
                LinhaValor("Desconto", "- " + Fmt.moeda(v.desconto))
                LinhaValor("Total", Fmt.moeda(v.valorTotal), destaque = true, cor = if (cancelada) Cores.TextoApoio else Cores.Primaria)
            }
            val texto = buildString {
                append("Comprovante de venda #${v.id}\n${Fmt.dataHora(v.dataHora)}\n\n")
                itens.forEach { append("${it.quantidade}x ${it.produtoNome} — ${Fmt.moeda(it.subtotal)}\n") }
                if (v.desconto > 0) append("Desconto: -${Fmt.moeda(v.desconto)}\n")
                append("*Total: ${Fmt.moeda(v.valorTotal)}* (${v.formaPagamento.rotulo})\n\nObrigado pela preferência!")
            }
            OutlinedButton(onClick = { Compartilhar.texto(context, texto, "Enviar comprovante") }, modifier = Modifier.fillMaxWidth().height(48.dp)) {
                Icon(Icons.Default.Share, null); Spacer(Modifier.width(8.dp)); Text("Enviar comprovante")
            }
            if (!cancelada) {
                Text(
                    "Ao cancelar, os itens voltam para o estoque e a venda deixa de contar nos relatórios.",
                    style = MaterialTheme.typography.bodySmall, color = Cores.TextoApoio,
                )
                Button(
                    onClick = { confirmar = true }, modifier = Modifier.fillMaxWidth().height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Cores.Perigo),
                ) { Text("Cancelar venda") }
            }
        }
    }
    if (confirmar) Confirmar(
        "Cancelar a venda?", "Os itens serão devolvidos ao estoque. Esta ação não pode ser desfeita.", "Cancelar venda", perigo = true,
        aoConfirmar = { escopo.gravar(context, "Venda cancelada.") { Repositorios.vendas.cancelar(id) } },
        aoFechar = { confirmar = false },
    )
}

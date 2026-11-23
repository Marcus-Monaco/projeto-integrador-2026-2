package com.meubalcao.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.meubalcao.app.data.model.Cliente
import com.meubalcao.app.data.model.ClienteResumo
import com.meubalcao.app.data.model.Origem
import com.meubalcao.app.data.model.Segmento
import com.meubalcao.app.data.repo.Repositorios
import com.meubalcao.app.ui.Rotas
import com.meubalcao.app.ui.components.Avatar
import com.meubalcao.app.ui.components.Campo
import com.meubalcao.app.ui.components.Cartao
import com.meubalcao.app.ui.components.Chave
import com.meubalcao.app.ui.components.Filtros
import com.meubalcao.app.ui.components.Indicador
import com.meubalcao.app.ui.components.LinhaValor
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

/** T09 · Clientes com busca e segmentação automática (RF13, RF14, RN08). */
@Composable
fun ClientesScreen(nav: NavController) {
    var busca by rememberSaveable { mutableStateOf("") }
    var segmento by rememberSaveable { mutableStateOf(Segmento.TODOS) }
    val clientes = carregar(busca, segmento) { Repositorios.clientes.listar(busca, segmento) }
    val contagem = carregar { Repositorios.clientes.contagemPorSegmento() } ?: emptyMap()

    Tela("Clientes", botao = {
        ExtendedFloatingActionButton(
            onClick = { nav.navigate(Rotas.clienteForm()) }, icon = { Icon(Icons.Default.PersonAdd, null) }, text = { Text("Novo cliente") },
            containerColor = Cores.Primaria, contentColor = Color.White,
        )
    }) { pad ->
        Column(Modifier.fillMaxSize().padding(pad)) {
            OutlinedTextField(
                busca, { busca = it }, placeholder = { Text("Buscar por nome ou telefone") }, leadingIcon = { Icon(Icons.Default.Search, null) },
                singleLine = true, shape = RoundedCornerShape(50), modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            )
            Filtros(Segmento.entries, segmento, { s -> "${s.rotulo} (${contagem[s] ?: 0})" }, { segmento = it }, Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
            if (clientes?.isEmpty() == true) Vazio(
                Icons.Default.People, "Nenhum cliente aqui",
                if (segmento == Segmento.TODOS && busca.isBlank()) "Cadastre seus clientes para acompanhar compras e enviar promoções." else "Nenhum cliente neste filtro.",
            )
            LazyColumn(contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 88.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(clientes.orEmpty(), key = { it.cliente.id }) { r -> LinhaCliente(r) { nav.navigate(Rotas.cliente(r.cliente.id)) } }
            }
        }
    }
}

@Composable
private fun LinhaCliente(r: ClienteResumo, aoClicar: () -> Unit) {
    Cartao(onClick = aoClicar) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Avatar(r.cliente.nome)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(r.cliente.nome, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f, fill = false))
                    if (r.aniversariante) { Spacer(Modifier.width(6.dp)); Selo("🎂 Aniversário", Cores.Primaria, Cores.PrimariaClara) }
                    if (r.inativo) { Spacer(Modifier.width(6.dp)); Selo("Inativo") }
                }
                Text(
                    "${r.compras} compra(s) · " + (r.ultimaCompra?.let { "última em ${Fmt.data(it)}" } ?: "sem compras"),
                    style = MaterialTheme.typography.bodySmall, color = Cores.TextoApoio,
                )
            }
            Text(Fmt.moeda(r.totalGasto), fontWeight = FontWeight.Bold)
        }
    }
}

/** T10 · Detalhe do cliente: contato, indicadores e histórico de compras (RF15, RF16). */
@Composable
fun ClienteDetalheScreen(nav: NavController, id: Long) {
    val context = LocalContext.current
    val dados = carregar(id) { Repositorios.clientes.resumo(id) to Repositorios.vendas.historico(clienteId = id) }
    val nomeNegocio = carregar { Repositorios.perfil.obter()?.nomeFantasia } ?: ""

    Tela(dados?.first?.cliente?.nome ?: "Cliente", voltar = { nav.popBackStack() }, acoes = {
        IconButton(onClick = { nav.navigate(Rotas.clienteForm(id)) }) { Icon(Icons.Default.Edit, "Editar") }
    }) { pad ->
        val r = dados?.first ?: return@Tela
        val c = r.cliente
        LazyColumn(Modifier.fillMaxSize().padding(pad), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                Cartao {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Avatar(c.nome, 56.dp)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(c.nome, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            Text(listOfNotNull(c.telefone, c.cidade).joinToString(" · "), color = Cores.TextoApoio, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Selo("Origem: ${c.origem.rotulo}", Cores.TextoApoio, Cores.Borda)
                        if (c.aceitaPromocoes) Selo("Aceita promoções", Cores.Sucesso, Cores.PrimariaClara) else Selo("Não aceita promoções", Cores.TextoApoio, Cores.Borda)
                    }
                    if (r.aniversariante) { Spacer(Modifier.height(6.dp)); Selo("🎂 Faz aniversário este mês (${Fmt.dataDoBanco(c.dataNascimento).take(5)})", Cores.Primaria, Cores.PrimariaClara) }
                    if (r.inativo) { Spacer(Modifier.height(6.dp)); Selo("Sem comprar há ${r.diasSemComprar} dias") }
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Indicador("Compras", r.compras.toString(), Modifier.weight(1f))
                    Indicador("Total gasto", Fmt.moeda(r.totalGasto), Modifier.weight(1.3f))
                    Indicador("Ticket", Fmt.moeda(r.ticketMedio), Modifier.weight(1.1f))
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { Compartilhar.whatsapp(context, c.telefone, "Olá, ${c.nome.substringBefore(' ')}! Aqui é da $nomeNegocio. ") },
                        enabled = !c.telefone.isNullOrBlank(), modifier = Modifier.weight(1f).height(48.dp),
                    ) { Icon(Icons.AutoMirrored.Filled.Chat, null); Spacer(Modifier.width(4.dp)); Text("WhatsApp") }
                    OutlinedButton(
                        onClick = { c.telefone?.let { Compartilhar.ligar(context, it) } },
                        enabled = !c.telefone.isNullOrBlank(), modifier = Modifier.weight(1f).height(48.dp),
                    ) { Icon(Icons.Default.Phone, null); Spacer(Modifier.width(4.dp)); Text("Ligar") }
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { nav.navigate(Rotas.novaVenda(id)) }, modifier = Modifier.weight(1f).height(48.dp)) {
                        Icon(Icons.Default.ShoppingCart, null); Spacer(Modifier.width(4.dp)); Text("Vender")
                    }
                    OutlinedButton(
                        onClick = { nav.navigate(Rotas.campanhaForm(clienteId = id)) }, enabled = c.aceitaPromocoes,
                        modifier = Modifier.weight(1f).height(48.dp),
                    ) { Icon(Icons.Default.Campaign, null); Spacer(Modifier.width(4.dp)); Text("Promoção") }
                }
            }
            if (c.email != null || c.observacoes != null || c.dataNascimento != null) item {
                Cartao {
                    c.email?.let { LinhaValor("E-mail", it) }
                    c.dataNascimento?.let { LinhaValor("Nascimento", Fmt.dataDoBanco(it)) }
                    LinhaValor("Cliente desde", Fmt.data(c.criadoEm))
                    c.observacoes?.let { Spacer(Modifier.height(4.dp)); Text(it, color = Cores.TextoApoio) }
                }
            }
            item { Secao("Histórico de compras") }
            if (dados.second.isEmpty()) item { Text("Nenhuma compra registrada.", color = Cores.TextoApoio) }
            items(dados.second, key = { it.id }) { v -> LinhaVenda(v, mostrarData = true) { nav.navigate(Rotas.venda(v.id)) } }
        }
    }
}

/** Cadastro/edição de cliente (RF13, RN11). */
@Composable
fun ClienteFormScreen(nav: NavController, id: Long) {
    val context = LocalContext.current
    val escopo = rememberCoroutineScope()
    val original = carregar(id) { if (id > 0) Repositorios.clientes.resumo(id)?.cliente else null }

    var iniciado by rememberSaveable { mutableStateOf(id == 0L) }
    var nome by rememberSaveable { mutableStateOf("") }
    var telefone by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var nascimento by rememberSaveable { mutableStateOf("") }
    var cidade by rememberSaveable { mutableStateOf("") }
    var origem by rememberSaveable { mutableStateOf(Origem.BALCAO) }
    var aceita by rememberSaveable { mutableStateOf(true) }
    var obs by rememberSaveable { mutableStateOf("") }
    var tentou by remember { mutableStateOf(false) }

    LaunchedEffect(original) {
        val c = original ?: return@LaunchedEffect
        if (iniciado) return@LaunchedEffect
        nome = c.nome; telefone = c.telefone.orEmpty(); email = c.email.orEmpty()
        nascimento = Fmt.dataDoBanco(c.dataNascimento); cidade = c.cidade.orEmpty()
        origem = c.origem; aceita = c.aceitaPromocoes; obs = c.observacoes.orEmpty()
        iniciado = true
    }

    val nascBanco = Fmt.dataParaBanco(nascimento)
    val erroNasc = if (nascimento.isNotBlank() && nascBanco == null) "Use o formato dd/mm/aaaa" else null

    Tela(if (id == 0L) "Novo cliente" else "Editar cliente", voltar = { nav.popBackStack() }) { pad ->
        Column(Modifier.fillMaxSize().padding(pad).verticalScroll(rememberScrollState()).padding(16.dp)) {
            Campo("Nome completo *", nome, { nome = it }, erro = if (tentou && nome.isBlank()) "Informe o nome" else null)
            Campo("Telefone / WhatsApp", telefone, { telefone = it }, teclado = KeyboardType.Phone, dica = "Ex.: (54) 99999-0000")
            Campo("E-mail", email, { email = it }, teclado = KeyboardType.Email)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Campo("Nascimento", nascimento, { nascimento = it }, Modifier.weight(1f), KeyboardType.Number, erro = erroNasc, dica = "dd/mm/aaaa")
                Campo("Cidade", cidade, { cidade = it }, Modifier.weight(1f))
            }
            Selecao("Como conheceu o negócio", Origem.entries, origem, { it.rotulo }, { origem = it })
            Chave("Aceita receber promoções", "Somente estes clientes entram nas campanhas", aceita) { aceita = it }
            Campo("Observações", obs, { obs = it }, linhas = 3)
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = {
                    tentou = true
                    if (nome.isBlank() || erroNasc != null) return@Button
                    escopo.gravar(context, "Cliente salvo.", depois = { nav.popBackStack() }) {
                        Repositorios.clientes.salvar(
                            Cliente(
                                id = id, nome = nome, telefone = telefone, email = email, dataNascimento = nascBanco,
                                cidade = cidade, origem = origem, aceitaPromocoes = aceita, observacoes = obs,
                            ),
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
            ) { Text("Salvar cliente") }
        }
    }
}

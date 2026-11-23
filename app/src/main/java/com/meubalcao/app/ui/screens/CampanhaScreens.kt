package com.meubalcao.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.meubalcao.app.data.model.Campanha
import com.meubalcao.app.data.model.CanalCampanha
import com.meubalcao.app.data.model.Produto
import com.meubalcao.app.data.model.Segmento
import com.meubalcao.app.data.model.StatusCampanha
import com.meubalcao.app.data.repo.Repositorios
import com.meubalcao.app.ui.Rotas
import com.meubalcao.app.ui.components.Avatar
import com.meubalcao.app.ui.components.Campo
import com.meubalcao.app.ui.components.Cartao
import com.meubalcao.app.ui.components.Filtros
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

/** Modelos de mensagem prontos (RF17). {nome} é trocado pelo primeiro nome do cliente. */
private val modelos = listOf(
    "Novidade" to "Oi, {nome}! Temos novidade na {negocio}: {produto} saindo agora. Quer que eu separe o seu?",
    "Saudade" to "Oi, {nome}! Faz um tempinho que você não aparece na {negocio}. Que tal passar aqui esta semana? Estamos com {produto} fresquinho!",
    "Aniversário" to "Feliz aniversário, {nome}! 🎉 A {negocio} preparou um mimo especial para você este mês. Venha buscar!",
    "Promoção" to "Oi, {nome}! Só esta semana: {produto} com condição especial na {negocio}. Peça pelo WhatsApp!",
)

fun personalizar(mensagem: String, nomeCliente: String, negocio: String, produto: String?): String =
    mensagem.replace("{nome}", nomeCliente.substringBefore(' '))
        .replace("{negocio}", negocio.ifBlank { "nossa loja" })
        .replace("{produto}", produto ?: "nossos produtos")

/** T12 · Campanhas: situação, alcance e resultado (RF17, RF19, RF20). */
@Composable
fun CampanhasScreen(nav: NavController) {
    var filtro by rememberSaveable { mutableStateOf<StatusCampanha?>(null) }
    val campanhas = carregar(filtro) { Repositorios.campanhas.listar(filtro) }

    Tela("Campanhas", voltar = { nav.popBackStack() }, botao = {
        ExtendedFloatingActionButton(
            onClick = { nav.navigate(Rotas.campanhaForm()) }, icon = { Icon(Icons.Default.Add, null) }, text = { Text("Nova campanha") },
            containerColor = Cores.Primaria, contentColor = Color.White,
        )
    }) { pad ->
        Column(Modifier.fillMaxSize().padding(pad)) {
            Filtros(listOf<StatusCampanha?>(null) + StatusCampanha.entries, filtro, { it?.rotulo ?: "Todas" }, { filtro = it }, Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
            if (campanhas?.isEmpty() == true) Vazio(Icons.Default.Campaign, "Nenhuma campanha", "Crie uma campanha para trazer seus clientes de volta.")
            LazyColumn(contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 88.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(campanhas.orEmpty(), key = { it.id }) { c ->
                    Cartao(onClick = { nav.navigate(Rotas.campanhaForm(c.id)) }) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(c.titulo, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                            when (c.status) {
                                StatusCampanha.ATIVA -> Selo("Ativa", Cores.Sucesso, Cores.PrimariaClara)
                                StatusCampanha.RASCUNHO -> Selo("Rascunho", Cores.TextoApoio, Cores.Borda)
                                StatusCampanha.ENCERRADA -> Selo("Encerrada", Cores.Alerta, Cores.AlertaFundo)
                            }
                        }
                        Text(
                            "Público: ${c.segmento.rotulo} · ${c.canal.rotulo}" + (c.produtoNome?.let { " · $it" } ?: ""),
                            style = MaterialTheme.typography.bodySmall, color = Cores.TextoApoio,
                        )
                        if (c.dataInicio != null || c.dataFim != null) Text(
                            "Período: ${Fmt.dataDoBanco(c.dataInicio).ifEmpty { "—" }} a ${Fmt.dataDoBanco(c.dataFim).ifEmpty { "—" }}",
                            style = MaterialTheme.typography.bodySmall, color = Cores.TextoApoio,
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            if (c.alcancados == 0) "Ainda não enviada"
                            else "${c.alcancados} cliente(s) alcançado(s) · ${c.compraramDepois} comprou(aram) depois",
                            fontWeight = FontWeight.Medium, color = if (c.compraramDepois > 0) Cores.Primaria else Cores.Texto,
                        )
                        if (c.status != StatusCampanha.ENCERRADA) {
                            Spacer(Modifier.height(8.dp))
                            Button(onClick = { nav.navigate(Rotas.campanhaEnvio(c.id)) }, modifier = Modifier.fillMaxWidth().height(44.dp)) {
                                Icon(Icons.AutoMirrored.Filled.Send, null); Spacer(Modifier.width(8.dp)); Text("Enviar para o público")
                            }
                        }
                    }
                }
            }
        }
    }
}

/** T13 · Nova/editar campanha com público, modelos e prévia (RF17, RF18, RN09). */
@Composable
fun CampanhaFormScreen(nav: NavController, id: Long, clienteId: Long) {
    val context = LocalContext.current
    val escopo = rememberCoroutineScope()
    val original = carregar(id) { if (id > 0) Repositorios.campanhas.campanha(id) else null }
    val produtos = carregar { Repositorios.catalogo.produtos(incluirInativos = false) } ?: emptyList()
    val negocio = carregar { Repositorios.perfil.obter()?.nomeFantasia } ?: ""
    val clienteAlvo = carregar(clienteId) { if (clienteId > 0) Repositorios.clientes.resumo(clienteId) else null }

    var iniciado by rememberSaveable { mutableStateOf(id == 0L) }
    var titulo by rememberSaveable { mutableStateOf("") }
    var mensagem by rememberSaveable { mutableStateOf(modelos[0].second) }
    var produtoId by rememberSaveable { mutableStateOf<Long?>(null) }
    var segmento by rememberSaveable { mutableStateOf(Segmento.TODOS) }
    var canal by rememberSaveable { mutableStateOf(CanalCampanha.WHATSAPP) }
    var inicio by rememberSaveable { mutableStateOf("") }
    var fim by rememberSaveable { mutableStateOf("") }
    var status by rememberSaveable { mutableStateOf(StatusCampanha.RASCUNHO) }
    var tentou by remember { mutableStateOf(false) }

    LaunchedEffect(original) {
        val c = original ?: return@LaunchedEffect
        if (iniciado) return@LaunchedEffect
        titulo = c.titulo; mensagem = c.mensagem; produtoId = c.produtoId; segmento = c.segmento; canal = c.canal
        inicio = Fmt.dataDoBanco(c.dataInicio); fim = Fmt.dataDoBanco(c.dataFim); status = c.status
        iniciado = true
    }
    // Atalho vindo do cliente (T10 → Promoção): segmento sugerido conforme o perfil do cliente
    LaunchedEffect(clienteAlvo) {
        val r = clienteAlvo ?: return@LaunchedEffect
        if (titulo.isNotBlank()) return@LaunchedEffect
        segmento = when { r.aniversariante -> Segmento.ANIVERSARIANTES; r.inativo -> Segmento.INATIVOS; r.compras >= 3 -> Segmento.RECORRENTES; else -> Segmento.TODOS }
        titulo = "Promoção para ${r.cliente.nome.substringBefore(' ')}"
        mensagem = when (segmento) { Segmento.ANIVERSARIANTES -> modelos[2].second; Segmento.INATIVOS -> modelos[1].second; else -> modelos[3].second }
    }

    val publico = carregar(segmento) { Repositorios.clientes.publico(segmento).size } ?: 0
    val produtoNome = produtos.firstOrNull { it.id == produtoId }?.nome
    val inicioBanco = Fmt.dataParaBanco(inicio)
    val fimBanco = Fmt.dataParaBanco(fim)
    val erroInicio = if (inicio.isNotBlank() && inicioBanco == null) "dd/mm/aaaa" else null
    val erroFim = when {
        fim.isNotBlank() && fimBanco == null -> "dd/mm/aaaa"
        inicioBanco != null && fimBanco != null && fimBanco < inicioBanco -> "Antes do início"
        else -> null
    }

    fun salvar(novoStatus: StatusCampanha, depois: (Long) -> Unit) {
        tentou = true
        if (titulo.isBlank() || mensagem.isBlank() || erroInicio != null || erroFim != null) return
        escopo.gravar(context, "Campanha salva.", depois = { depois(it as Long) }) {
            Repositorios.campanhas.salvar(
                Campanha(
                    id = id, titulo = titulo, mensagem = mensagem, produtoId = produtoId, segmento = segmento, canal = canal,
                    dataInicio = inicioBanco, dataFim = fimBanco, status = novoStatus,
                ),
            )
        }
    }

    Tela(if (id == 0L) "Nova campanha" else "Editar campanha", voltar = { nav.popBackStack() }) { pad ->
        Column(Modifier.fillMaxSize().padding(pad).verticalScroll(rememberScrollState()).padding(16.dp)) {
            Campo("Título *", titulo, { titulo = it }, erro = if (tentou && titulo.isBlank()) "Informe o título" else null)
            Selecao("Produto em destaque", listOf<Produto?>(null) + produtos, produtos.firstOrNull { it.id == produtoId }, { it?.nome ?: "Nenhum" }, { produtoId = it?.id }, vazio = "Nenhum")
            Secao("Público")
            Opcoes(Segmento.entries, segmento, { it.rotulo }, { segmento = it })
            Text("$publico cliente(s) que aceitam promoções neste público", style = MaterialTheme.typography.bodySmall, color = Cores.TextoApoio)
            Secao("Canal")
            Opcoes(CanalCampanha.entries, canal, { it.rotulo }, { canal = it })
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Campo("Início", inicio, { inicio = it }, Modifier.weight(1f), KeyboardType.Number, erro = erroInicio, dica = if (erroInicio == null) "dd/mm/aaaa" else null)
                Campo("Fim", fim, { fim = it }, Modifier.weight(1f), KeyboardType.Number, erro = erroFim, dica = if (erroFim == null) "dd/mm/aaaa" else null)
            }
            Secao("Mensagem")
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                modelos.forEach { (nome, texto) -> AssistChip(onClick = { mensagem = texto }, label = { Text(nome) }) }
            }
            Campo("Texto da mensagem *", mensagem, { mensagem = it }, linhas = 4, dica = "Use {nome}, {produto} e {negocio} para personalizar")
            Secao("Prévia no WhatsApp")
            Column(Modifier.fillMaxWidth().background(Color(0xFFECE5DD), RoundedCornerShape(12.dp)).padding(12.dp)) {
                Text(
                    personalizar(mensagem, clienteAlvo?.cliente?.nome ?: "Mariana", negocio, produtoNome),
                    modifier = Modifier.background(Color(0xFFDCF8C6), RoundedCornerShape(10.dp)).padding(10.dp),
                )
            }
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = { salvar(StatusCampanha.ATIVA) { novoId -> nav.navigate(Rotas.campanhaEnvio(novoId)) { popUpTo(Rotas.CAMPANHAS) } } },
                modifier = Modifier.fillMaxWidth().height(52.dp),
            ) { Icon(Icons.AutoMirrored.Filled.Send, null); Spacer(Modifier.width(8.dp)); Text("Ativar e enviar") }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(top = 8.dp)) {
                OutlinedButton(onClick = { salvar(if (status == StatusCampanha.ATIVA) StatusCampanha.ATIVA else StatusCampanha.RASCUNHO) { nav.popBackStack() } }, modifier = Modifier.weight(1f).height(48.dp)) {
                    Text(if (status == StatusCampanha.ATIVA) "Salvar" else "Salvar rascunho")
                }
                OutlinedButton(
                    onClick = { Compartilhar.texto(context, personalizar(mensagem, "cliente", negocio, produtoNome).replace("Oi, cliente!", "Oi!")) },
                    modifier = Modifier.weight(1f).height(48.dp),
                ) { Icon(Icons.Default.Share, null); Spacer(Modifier.width(6.dp)); Text("Compartilhar") }
            }
            if (id > 0 && status != StatusCampanha.ENCERRADA) TextButton(
                onClick = { salvar(StatusCampanha.ENCERRADA) { nav.popBackStack() } }, modifier = Modifier.fillMaxWidth(),
            ) { Text("Encerrar campanha", color = Cores.Alerta) }
        }
    }
}

/** Envio da campanha: um toque por cliente abre o WhatsApp e registra o envio (RF19). */
@Composable
fun CampanhaEnvioScreen(nav: NavController, id: Long) {
    val context = LocalContext.current
    val escopo = rememberCoroutineScope()
    val dados = carregar(id) {
        val c = Repositorios.campanhas.campanha(id)
        Triple(c, c?.let { Repositorios.clientes.publico(it.segmento) }.orEmpty(), Repositorios.campanhas.enviados(id))
    }
    val negocio = carregar { Repositorios.perfil.obter()?.nomeFantasia } ?: ""

    Tela("Enviar campanha", voltar = { nav.popBackStack() }) { pad ->
        val (c, publico, enviados) = dados ?: return@Tela
        if (c == null) return@Tela
        LazyColumn(Modifier.fillMaxSize().padding(pad), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            item {
                Cartao(cor = Cores.PrimariaClara) {
                    Text(c.titulo, fontWeight = FontWeight.Bold)
                    Text("Público: ${c.segmento.rotulo} · ${enviados.size} de ${publico.size} enviados", color = Cores.TextoApoio, style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.height(4.dp))
                    Text("Toque em Enviar: o WhatsApp abre com a mensagem personalizada. Depois volte ao app para o próximo cliente.", style = MaterialTheme.typography.bodySmall)
                }
            }
            if (publico.isEmpty()) item { Vazio(Icons.Default.Campaign, "Ninguém neste público", "Somente clientes com telefone que aceitam promoções podem receber campanhas.") }
            items(publico, key = { it.cliente.id }) { r ->
                val enviado = r.cliente.id in enviados
                Cartao {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Avatar(r.cliente.nome, 40.dp)
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(r.cliente.nome, fontWeight = FontWeight.SemiBold)
                            Text(r.cliente.telefone.orEmpty(), style = MaterialTheme.typography.bodySmall, color = Cores.TextoApoio)
                        }
                        if (enviado) Icon(Icons.Default.CheckCircle, "Enviado", tint = Cores.Sucesso)
                        Spacer(Modifier.width(4.dp))
                        TextButton(onClick = {
                            val texto = personalizar(c.mensagem, r.cliente.nome, negocio, c.produtoNome)
                            escopo.gravar(context, depois = { Compartilhar.whatsapp(context, r.cliente.telefone, texto) }) {
                                Repositorios.campanhas.registrarEnvio(c.id, r.cliente.id)
                                if (c.status == StatusCampanha.RASCUNHO) Repositorios.campanhas.alterarStatus(c.id, StatusCampanha.ATIVA)
                            }
                        }) { Text(if (enviado) "Reenviar" else "Enviar") }
                    }
                }
            }
        }
    }
}

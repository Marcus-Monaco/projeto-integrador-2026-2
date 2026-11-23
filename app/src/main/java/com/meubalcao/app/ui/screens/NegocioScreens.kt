package com.meubalcao.app.ui.screens

import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.meubalcao.app.data.model.PerfilNegocio
import com.meubalcao.app.data.repo.Repositorios
import com.meubalcao.app.ui.Rotas
import com.meubalcao.app.ui.components.Campo
import com.meubalcao.app.ui.components.Cartao
import com.meubalcao.app.ui.components.Foto
import com.meubalcao.app.ui.components.Secao
import com.meubalcao.app.ui.components.Tela
import com.meubalcao.app.ui.components.carregar
import com.meubalcao.app.ui.components.gravar
import com.meubalcao.app.ui.theme.Cores
import com.meubalcao.app.util.Compartilhar
import com.meubalcao.app.util.Imagens
import com.meubalcao.app.util.Vitrine

private val coresVitrine = listOf("#0F766E", "#1D4ED8", "#7C3AED", "#BE185D", "#B45309", "#15803D", "#0F172A")

/** T14 · Meu negócio: identidade digital, cartão com QR Code e vitrine (RF01 a RF03). */
@Composable
fun MeuNegocioScreen(nav: NavController) {
    val context = LocalContext.current
    val escopo = rememberCoroutineScope()
    val original = carregar { Repositorios.perfil.obter() }
    var iniciado by rememberSaveable { mutableStateOf(false) }
    var nome by rememberSaveable { mutableStateOf("") }
    var slogan by rememberSaveable { mutableStateOf("") }
    var descricao by rememberSaveable { mutableStateOf("") }
    var whatsapp by rememberSaveable { mutableStateOf("") }
    var instagram by rememberSaveable { mutableStateOf("") }
    var endereco by rememberSaveable { mutableStateOf("") }
    var horario by rememberSaveable { mutableStateOf("") }
    var logo by rememberSaveable { mutableStateOf<String?>(null) }
    var cor by rememberSaveable { mutableStateOf("#0F766E") }
    var tentou by remember { mutableStateOf(false) }

    LaunchedEffect(original) {
        val p = original ?: return@LaunchedEffect
        if (iniciado) return@LaunchedEffect
        nome = p.nomeFantasia; slogan = p.slogan.orEmpty(); descricao = p.descricao.orEmpty()
        whatsapp = p.whatsapp.orEmpty(); instagram = p.instagram.orEmpty(); endereco = p.endereco.orEmpty()
        horario = p.horarioFuncionamento.orEmpty(); logo = p.logoPath; cor = p.corTema
        iniciado = true
    }
    val seletor = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) escopo.gravar(context, depois = { logo = it as String? }) { Imagens.salvarFoto(context, uri, "logo") }
    }
    val perfil = PerfilNegocio(nome, slogan.ifBlank { null }, descricao.ifBlank { null }, whatsapp.ifBlank { null }, instagram.ifBlank { null }, endereco.ifBlank { null }, horario.ifBlank { null }, logo, cor)
    val corCompose = Color(android.graphics.Color.parseColor(cor))
    val qr = remember(perfil.whatsapp, perfil.instagram, perfil.nomeFantasia) { Imagens.qrCode(Vitrine.conteudoQr(perfil).ifBlank { "MeuBalcão" }, 400) }

    Tela("Meu negócio", voltar = { nav.popBackStack() }) { pad ->
        Column(Modifier.fillMaxSize().padding(pad).verticalScroll(rememberScrollState()).padding(16.dp)) {
            // Prévia do cartão digital
            Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(corCompose).padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Foto(logo, Modifier.size(64.dp).clickable { seletor.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }, Icons.Default.AddAPhoto)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(nome.ifBlank { "Nome do negócio" }, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        if (slogan.isNotBlank()) Text(slogan, color = Color.White.copy(alpha = 0.85f), fontSize = 13.sp)
                    }
                    Image(qr.asImageBitmap(), "QR Code", Modifier.size(72.dp).clip(RoundedCornerShape(8.dp)))
                }
                if (whatsapp.isNotBlank() || instagram.isNotBlank()) {
                    Spacer(Modifier.height(10.dp))
                    Text(listOf(whatsapp, instagram).filter { it.isNotBlank() }.joinToString("  ·  "), color = Color.White, fontSize = 13.sp)
                }
            }
            Spacer(Modifier.height(8.dp))
            Text("Toque no logotipo para escolher uma imagem. O QR Code abre o seu WhatsApp.", style = MaterialTheme.typography.bodySmall, color = Cores.TextoApoio)

            Secao("Dados do negócio")
            Campo("Nome do negócio *", nome, { nome = it }, erro = if (tentou && nome.isBlank()) "Informe o nome" else null)
            Campo("Slogan", slogan, { slogan = it })
            Campo("Descrição", descricao, { descricao = it }, linhas = 2)
            Campo("WhatsApp", whatsapp, { whatsapp = it }, teclado = KeyboardType.Phone, dica = "Ex.: (54) 99999-0000")
            Campo("Instagram", instagram, { instagram = it }, dica = "Ex.: @meunegocio")
            Campo("Endereço", endereco, { endereco = it })
            Campo("Horário de funcionamento", horario, { horario = it }, dica = "Ex.: Seg a Sáb, 8h às 19h")

            Secao("Cor da vitrine")
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                coresVitrine.forEach { hex ->
                    val c = Color(android.graphics.Color.parseColor(hex))
                    Box(
                        Modifier.size(36.dp).clip(CircleShape).background(c)
                            .border(if (hex == cor) 3.dp else 0.dp, Cores.Texto.copy(alpha = 0.4f), CircleShape)
                            .clickable { cor = hex },
                        contentAlignment = Alignment.Center,
                    ) { if (hex == cor) Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(18.dp)) }
                }
            }
            Spacer(Modifier.height(20.dp))
            Button(onClick = {
                tentou = true
                if (nome.isBlank()) return@Button
                escopo.gravar(context, "Dados do negócio salvos.") { Repositorios.perfil.salvar(perfil) }
            }, modifier = Modifier.fillMaxWidth().height(52.dp)) { Text("Salvar") }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = {
                tentou = true
                if (nome.isBlank()) return@OutlinedButton
                escopo.gravar(context, depois = { res ->
                    val (bmp, txt) = res as Pair<*, *>
                    Compartilhar.imagem(context, bmp as Bitmap, "vitrine.png", txt as String)
                }) {
                    Repositorios.perfil.salvar(perfil)
                    val produtos = Repositorios.catalogo.destaques()
                    Vitrine.gerar(perfil, produtos) to Vitrine.textoCatalogo(perfil, produtos)
                }
            }, modifier = Modifier.fillMaxWidth().height(52.dp)) {
                Icon(Icons.Default.Share, null); Spacer(Modifier.width(8.dp)); Text("Compartilhar vitrine digital")
            }
        }
    }
}

/** T15 · Menu Mais. */
@Composable
fun MaisScreen(nav: NavController) {
    val perfil = carregar { Repositorios.perfil.obter() }
    Tela("Mais") { pad ->
        Column(Modifier.fillMaxSize().padding(pad).verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Cartao(onClick = { nav.navigate(Rotas.NEGOCIO) }, cor = Cores.PrimariaClara) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Foto(perfil?.logoPath, Modifier.size(52.dp), Icons.Default.Storefront)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(perfil?.nomeFantasia ?: "Configure seu negócio", fontWeight = FontWeight.Bold)
                        Text(perfil?.slogan ?: "Nome, contatos, logo e vitrine digital", style = MaterialTheme.typography.bodySmall, color = Cores.TextoApoio)
                    }
                    Icon(Icons.Default.ChevronRight, null)
                }
            }
            ItemMenu(Icons.Default.Storefront, "Meu negócio", "Perfil, QR Code e vitrine") { nav.navigate(Rotas.NEGOCIO) }
            ItemMenu(Icons.Default.Receipt, "Histórico de vendas", "Consultar e cancelar vendas") { nav.navigate(Rotas.VENDAS) }
            ItemMenu(Icons.Default.Campaign, "Campanhas", "Promoções pelo WhatsApp") { nav.navigate(Rotas.CAMPANHAS) }
            ItemMenu(Icons.Default.Analytics, "Relatórios", "Gráficos, rankings e CSV") { nav.navigate(Rotas.RELATORIOS) }
            ItemMenu(Icons.Default.Category, "Categorias", "Organizar o catálogo") { nav.navigate(Rotas.CATEGORIAS) }
            ItemMenu(Icons.Default.Settings, "Configurações e metas", "Meta do mês, backup e dados") { nav.navigate(Rotas.CONFIG) }
            ItemMenu(Icons.Default.Info, "Sobre o app", "Versão e integrantes do grupo") { nav.navigate(Rotas.SOBRE) }
        }
    }
}

@Composable
private fun ItemMenu(icone: ImageVector, titulo: String, subtitulo: String, aoClicar: () -> Unit) {
    Cartao(onClick = aoClicar) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(40.dp).clip(RoundedCornerShape(10.dp)).background(Cores.PrimariaClara), contentAlignment = Alignment.Center) {
                Icon(icone, null, tint = Cores.Primaria)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(titulo, fontWeight = FontWeight.SemiBold)
                Text(subtitulo, style = MaterialTheme.typography.bodySmall, color = Cores.TextoApoio)
            }
            Icon(Icons.Default.ChevronRight, null, tint = Cores.TextoApoio)
        }
    }
}

package com.meubalcao.app.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.Scaffold
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.meubalcao.app.ui.theme.Cores
import com.meubalcao.app.util.Fmt
import com.meubalcao.app.util.Imagens

@Composable
fun Topo(titulo: String, voltar: (() -> Unit)? = null, acoes: @Composable RowScope.() -> Unit = {}) {
    TopAppBar(
        title = { Text(titulo, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        navigationIcon = {
            if (voltar != null) IconButton(onClick = voltar) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Voltar") }
        },
        actions = acoes,
        windowInsets = WindowInsets(0.dp),
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Cores.Fundo),
    )
}

/**
 * Estrutura padrão de uma tela: barra superior, conteúdo e botão flutuante opcional.
 * As margens do sistema (barra de status/navegação) já são tratadas pelo Scaffold principal.
 */
@Composable
fun Tela(
    titulo: String,
    voltar: (() -> Unit)? = null,
    acoes: @Composable RowScope.() -> Unit = {},
    botao: @Composable () -> Unit = {},
    rodape: @Composable () -> Unit = {},
    conteudo: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        topBar = { Topo(titulo, voltar, acoes) },
        floatingActionButton = botao,
        bottomBar = rodape,
        containerColor = Cores.Fundo,
        contentWindowInsets = WindowInsets(0.dp),
        content = conteudo,
    )
}

@Composable
fun Cartao(modifier: Modifier = Modifier, onClick: (() -> Unit)? = null, cor: Color = Color.White, conteudo: @Composable ColumnScope.() -> Unit) {
    val m = modifier.fillMaxWidth()
    val cores = CardDefaults.cardColors(containerColor = cor)
    val borda = androidx.compose.foundation.BorderStroke(1.dp, Cores.Borda)
    if (onClick != null) Card(onClick = onClick, modifier = m, colors = cores, border = borda, shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.padding(16.dp), content = conteudo)
    } else Card(modifier = m, colors = cores, border = borda, shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.padding(16.dp), content = conteudo)
    }
}

@Composable
fun Secao(titulo: String, modifier: Modifier = Modifier, acao: (@Composable () -> Unit)? = null) {
    Row(modifier.fillMaxWidth().padding(top = 8.dp, bottom = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(titulo, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
        acao?.invoke()
    }
}

@Composable
fun Indicador(rotulo: String, valor: String, modifier: Modifier = Modifier, icone: ImageVector? = null) {
    Card(
        modifier = modifier, shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Cores.Borda),
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (icone != null) { Icon(icone, null, tint = Cores.Primaria, modifier = Modifier.size(18.dp)); Spacer(Modifier.width(6.dp)) }
                Text(rotulo, style = MaterialTheme.typography.labelMedium, color = Cores.TextoApoio)
            }
            Spacer(Modifier.height(6.dp))
            Text(valor, fontSize = 20.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
fun Selo(texto: String, cor: Color = Cores.Alerta, fundo: Color = Cores.AlertaFundo) {
    Text(
        texto, color = cor, fontSize = 11.sp, fontWeight = FontWeight.SemiBold,
        modifier = Modifier.clip(RoundedCornerShape(50)).background(fundo).padding(horizontal = 8.dp, vertical = 2.dp),
    )
}

@Composable
fun Avatar(nome: String, tamanho: Dp = 44.dp, cor: Color = Cores.PrimariaClara) {
    Box(Modifier.size(tamanho).clip(CircleShape).background(cor), contentAlignment = Alignment.Center) {
        Text(Fmt.iniciais(nome), fontWeight = FontWeight.Bold, color = Cores.Primaria, fontSize = (tamanho.value / 2.8).sp)
    }
}

/** Foto salva no aparelho ou um ícone substituto. */
@Composable
fun Foto(caminho: String?, modifier: Modifier, icone: ImageVector) {
    val bmp = remember(caminho) { Imagens.carregar(caminho) }
    Box(modifier.clip(RoundedCornerShape(12.dp)).background(Cores.PrimariaClara), contentAlignment = Alignment.Center) {
        if (bmp != null) Image(bmp.asImageBitmap(), null, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        else Icon(icone, null, tint = Cores.Primaria, modifier = Modifier.size(32.dp))
    }
}

/** Linha de filtros (chips) com rolagem horizontal. */
@Composable
fun <T> Filtros(opcoes: List<T>, selecionado: T, rotulo: (T) -> String, aoSelecionar: (T) -> Unit, modifier: Modifier = Modifier) {
    Row(modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        opcoes.forEach { o ->
            FilterChip(
                selected = o == selecionado, onClick = { aoSelecionar(o) }, label = { Text(rotulo(o)) },
                leadingIcon = if (o == selecionado) ({ Icon(Icons.Default.Check, null, Modifier.size(16.dp)) }) else null,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Cores.PrimariaClara, selectedLabelColor = Cores.Primaria,
                    selectedLeadingIconColor = Cores.Primaria,
                ),
            )
        }
    }
}

/** Mesma ideia, em várias linhas (para formulários). */
@Composable
fun <T> Opcoes(opcoes: List<T>, selecionado: T, rotulo: (T) -> String, aoSelecionar: (T) -> Unit) {
    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        opcoes.forEach { o ->
            FilterChip(
                selected = o == selecionado, onClick = { aoSelecionar(o) }, label = { Text(rotulo(o)) },
                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Cores.PrimariaClara, selectedLabelColor = Cores.Primaria),
            )
        }
    }
}

@Composable
fun Campo(
    rotulo: String, valor: String, aoMudar: (String) -> Unit, modifier: Modifier = Modifier,
    teclado: KeyboardType = KeyboardType.Text, linhas: Int = 1, erro: String? = null, dica: String? = null,
    prefixo: String? = null,
) {
    OutlinedTextField(
        value = valor, onValueChange = aoMudar, label = { Text(rotulo) },
        modifier = modifier.fillMaxWidth().padding(vertical = 4.dp),
        keyboardOptions = KeyboardOptions(keyboardType = teclado),
        singleLine = linhas == 1, minLines = linhas,
        isError = erro != null,
        supportingText = (erro ?: dica)?.let { { Text(it) } },
        prefix = prefixo?.let { { Text(it) } },
        shape = RoundedCornerShape(12.dp),
    )
}

/** Campo de seleção (lista suspensa) somente leitura. */
@Composable
fun <T> Selecao(rotulo: String, opcoes: List<T>, selecionado: T?, texto: (T) -> String, aoSelecionar: (T) -> Unit, modifier: Modifier = Modifier, vazio: String = "Selecione") {
    var aberto by remember { mutableStateOf(false) }
    Box(modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        OutlinedTextField(
            value = selecionado?.let(texto) ?: vazio, onValueChange = {}, readOnly = true, label = { Text(rotulo) },
            trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) },
            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
        )
        Box(Modifier.matchParentSize().clip(RoundedCornerShape(12.dp)).clickable { aberto = true })
        DropdownMenu(expanded = aberto, onDismissRequest = { aberto = false }) {
            opcoes.forEach { o ->
                DropdownMenuItem(text = { Text(texto(o)) }, onClick = { aoSelecionar(o); aberto = false })
            }
        }
    }
}

@Composable
fun Chave(rotulo: String, descricao: String?, marcado: Boolean, aoMudar: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().clickable { aoMudar(!marcado) }.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(rotulo, fontWeight = FontWeight.Medium)
            descricao?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = Cores.TextoApoio) }
        }
        Switch(checked = marcado, onCheckedChange = aoMudar)
    }
}

@Composable
fun Vazio(icone: ImageVector, titulo: String, texto: String, acao: (@Composable () -> Unit)? = null) {
    Column(Modifier.fillMaxWidth().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.size(72.dp).clip(CircleShape).background(Cores.PrimariaClara), contentAlignment = Alignment.Center) {
            Icon(icone, null, tint = Cores.Primaria, modifier = Modifier.size(36.dp))
        }
        Spacer(Modifier.height(12.dp))
        Text(titulo, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        Text(texto, color = Cores.TextoApoio, textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyMedium)
        if (acao != null) { Spacer(Modifier.height(12.dp)); acao() }
    }
}

@Composable
fun Carregando() {
    Box(Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
}

@Composable
fun Confirmar(titulo: String, texto: String, botao: String, perigo: Boolean = false, aoConfirmar: () -> Unit, aoFechar: () -> Unit) {
    AlertDialog(
        onDismissRequest = aoFechar,
        title = { Text(titulo) },
        text = { Text(texto) },
        confirmButton = {
            TextButton(onClick = { aoConfirmar(); aoFechar() }) {
                Text(botao, color = if (perigo) Cores.Perigo else Cores.Primaria, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = { TextButton(onClick = aoFechar) { Text("Voltar") } },
    )
}

@Composable
fun LinhaValor(rotulo: String, valor: String, destaque: Boolean = false, cor: Color = Color.Unspecified) {
    Row(Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
        Text(rotulo, color = if (destaque) Cores.Texto else Cores.TextoApoio, fontWeight = if (destaque) FontWeight.Bold else FontWeight.Normal, modifier = Modifier.weight(1f))
        Text(valor, fontWeight = if (destaque) FontWeight.Bold else FontWeight.Medium, color = cor, fontSize = if (destaque) 18.sp else 14.sp)
    }
}

@Composable
fun Borda(modifier: Modifier = Modifier, conteudo: @Composable ColumnScope.() -> Unit) {
    Column(modifier.fillMaxWidth().border(1.dp, Cores.Borda, RoundedCornerShape(12.dp)).padding(12.dp), content = conteudo)
}

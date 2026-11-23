package com.meubalcao.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/** Padrões de interface definidos nos wireframes (docs/06-wireframes.md, item 6.3). */
object Cores {
    val Primaria = Color(0xFF0F766E)        // verde-azulado: ações, itens ativos, gráficos
    val PrimariaClara = Color(0xFFCCFBF1)
    val Alerta = Color(0xFFB45309)          // âmbar: estoque baixo, inativos
    val AlertaFundo = Color(0xFFFEF3C7)
    val Perigo = Color(0xFFB91C1C)          // vermelho: só ações destrutivas
    val PerigoFundo = Color(0xFFFEE2E2)
    val Texto = Color(0xFF0F172A)
    val TextoApoio = Color(0xFF64748B)
    val Fundo = Color(0xFFF8FAFC)
    val Borda = Color(0xFFE2E8F0)
    val Sucesso = Color(0xFF15803D)
    val Grafico = listOf(Color(0xFF0F766E), Color(0xFF0EA5E9), Color(0xFFF59E0B), Color(0xFF8B5CF6), Color(0xFFEC4899))
}

private val esquema = lightColorScheme(
    primary = Cores.Primaria,
    onPrimary = Color.White,
    primaryContainer = Cores.PrimariaClara,
    onPrimaryContainer = Color(0xFF134E4A),
    secondary = Color(0xFF334155),
    secondaryContainer = Color(0xFFE2E8F0),
    onSecondaryContainer = Cores.Texto,
    background = Cores.Fundo,
    onBackground = Cores.Texto,
    surface = Color.White,
    onSurface = Cores.Texto,
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = Cores.TextoApoio,
    surfaceContainer = Color.White,
    surfaceContainerLow = Color.White,
    surfaceContainerHigh = Color(0xFFF1F5F9),
    outline = Color(0xFFCBD5E1),
    outlineVariant = Cores.Borda,
    error = Cores.Perigo,
)

@Composable
fun MeuBalcaoTema(conteudo: @Composable () -> Unit) {
    MaterialTheme(colorScheme = esquema, content = conteudo)
}

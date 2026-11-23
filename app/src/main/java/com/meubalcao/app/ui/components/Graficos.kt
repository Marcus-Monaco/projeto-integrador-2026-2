package com.meubalcao.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.meubalcao.app.ui.theme.Cores

/**
 * Gráficos desenhados diretamente com o Canvas do Compose (sem biblioteca externa).
 */

/** Gráfico de colunas: faturamento por dia (RF23). */
@Composable
fun GraficoColunas(
    dados: List<Pair<String, Double>>,
    modifier: Modifier = Modifier,
    altura: Int = 140,
    destacarUltimo: Boolean = true,
    rotuloValor: ((Double) -> String)? = null,
) {
    val max = (dados.maxOfOrNull { it.second } ?: 0.0).coerceAtLeast(1.0)
    val mostrarRotulos = dados.size <= 10
    Column(modifier.fillMaxWidth()) {
        if (rotuloValor != null && mostrarRotulos) {
            Row(Modifier.fillMaxWidth()) {
                dados.forEach { (_, v) ->
                    Text(
                        if (v > 0) rotuloValor(v) else "", modifier = Modifier.weight(1f), fontSize = 9.sp,
                        textAlign = TextAlign.Center, color = Cores.TextoApoio, maxLines = 1,
                    )
                }
            }
        }
        Canvas(Modifier.fillMaxWidth().height(altura.dp).padding(top = 4.dp)) {
            val passo = size.width / dados.size.coerceAtLeast(1)
            val largura = passo * if (dados.size > 14) 0.7f else 0.55f
            dados.forEachIndexed { i, (_, v) ->
                val h = (v / max * size.height).toFloat().coerceAtLeast(if (v > 0) 4f else 2f)
                val cor = if (destacarUltimo && i == dados.lastIndex) Cores.Primaria else Cores.Primaria.copy(alpha = 0.45f)
                drawRoundRect(
                    color = if (v > 0) cor else Cores.Borda,
                    topLeft = Offset(i * passo + (passo - largura) / 2, size.height - h),
                    size = Size(largura, h), cornerRadius = CornerRadius(6f, 6f),
                )
            }
        }
        if (mostrarRotulos) {
            Row(Modifier.fillMaxWidth().padding(top = 4.dp)) {
                dados.forEach { (r, _) ->
                    Text(r, modifier = Modifier.weight(1f), fontSize = 11.sp, textAlign = TextAlign.Center, color = Cores.TextoApoio, maxLines = 1)
                }
            }
        } else if (dados.isNotEmpty()) {
            Row(Modifier.fillMaxWidth().padding(top = 4.dp)) {
                Text(dados.first().first, fontSize = 11.sp, color = Cores.TextoApoio, modifier = Modifier.weight(1f))
                Text(dados.last().first, fontSize = 11.sp, color = Cores.TextoApoio)
            }
        }
    }
}

/** Barras horizontais com rótulo e valor: ranking e faturamento por canal (RF23, RF24). */
@Composable
fun BarrasHorizontais(dados: List<Triple<String, Double, String>>, modifier: Modifier = Modifier, colorido: Boolean = false) {
    val max = (dados.maxOfOrNull { it.second } ?: 0.0).coerceAtLeast(1.0)
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        dados.forEachIndexed { i, (rotulo, valor, texto) ->
            val cor = if (colorido) Cores.Grafico[i % Cores.Grafico.size] else Cores.Primaria
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(rotulo, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Spacer(Modifier.width(8.dp))
                    Text(texto, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                }
                Spacer(Modifier.height(4.dp))
                Box(Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(50)).background(Cores.Borda)) {
                    Box(Modifier.fillMaxWidth((valor / max).toFloat().coerceIn(0.02f, 1f)).fillMaxHeight().clip(RoundedCornerShape(50)).background(cor))
                }
            }
        }
    }
}

/** Barra de progresso da meta do mês (RF22). */
@Composable
fun BarraProgresso(percentual: Double, cor: Color = Cores.Primaria) {
    Box(Modifier.fillMaxWidth().height(12.dp).clip(RoundedCornerShape(50)).background(Cores.Borda)) {
        Box(Modifier.fillMaxWidth((percentual / 100).toFloat().coerceIn(0f, 1f)).fillMaxHeight().clip(RoundedCornerShape(50)).background(cor))
    }
}

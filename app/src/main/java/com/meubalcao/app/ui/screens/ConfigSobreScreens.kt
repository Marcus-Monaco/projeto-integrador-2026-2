package com.meubalcao.app.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Dataset
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.meubalcao.app.BuildConfig
import com.meubalcao.app.data.repo.Repositorios
import com.meubalcao.app.ui.components.Avatar
import com.meubalcao.app.ui.components.Campo
import com.meubalcao.app.ui.components.Cartao
import com.meubalcao.app.ui.components.Confirmar
import com.meubalcao.app.ui.components.LinhaValor
import com.meubalcao.app.ui.components.Secao
import com.meubalcao.app.ui.components.Tela
import com.meubalcao.app.ui.components.carregar
import com.meubalcao.app.ui.components.gravar
import com.meubalcao.app.ui.theme.Cores
import com.meubalcao.app.util.Fmt
import java.time.LocalDate

/** T16 · Configurações: meta do mês, backup/restauração e dados de exemplo (RF22, RF26, RF27). */
@Composable
fun ConfiguracoesScreen(nav: NavController) {
    val context = LocalContext.current
    val escopo = rememberCoroutineScope()
    val anoMes = Fmt.anoMesAtual()
    val metaAtual = carregar { Repositorios.perfil.meta(anoMes) }
    var meta by remember { mutableStateOf("") }
    var confirmar by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(metaAtual) { if (meta.isEmpty() && metaAtual != null) meta = Fmt.valorEditavel(metaAtual) }

    // Backup: o usuário escolhe onde salvar o arquivo (Downloads, Drive...)
    val salvarBackup = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/octet-stream")) { uri ->
        if (uri != null) escopo.gravar(context, "Backup salvo com sucesso.") {
            context.contentResolver.openOutputStream(uri)?.use { Repositorios.db.exportar(it) }
        }
    }
    val abrirBackup = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) escopo.gravar(context, "Backup restaurado.") {
            context.contentResolver.openInputStream(uri)?.let { Repositorios.db.restaurar(it) }
            Repositorios.notificarMudanca()
        }
    }

    Tela("Configurações", voltar = { nav.popBackStack() }) { pad ->
        Column(Modifier.fillMaxSize().padding(pad).verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Cartao {
                Text("Meta de faturamento", fontWeight = FontWeight.Bold)
                Text(Fmt.mesAtualExtenso(), color = Cores.TextoApoio, style = MaterialTheme.typography.bodySmall)
                Campo("Valor da meta do mês", meta, { meta = it }, teclado = KeyboardType.Decimal, prefixo = "R$ ")
                Button(onClick = {
                    val v = Fmt.lerValor(meta)
                    if (v == null || v <= 0) { android.widget.Toast.makeText(context, "Informe um valor maior que zero.", android.widget.Toast.LENGTH_SHORT).show(); return@Button }
                    escopo.gravar(context, "Meta salva.") { Repositorios.perfil.salvarMeta(anoMes, v) }
                }, modifier = Modifier.fillMaxWidth().height(48.dp)) { Text("Salvar meta") }
            }
            Cartao {
                Text("Backup dos dados", fontWeight = FontWeight.Bold)
                Text(
                    "Todos os dados ficam somente neste aparelho, no arquivo meubalcao.db. Faça backups com frequência.",
                    color = Cores.TextoApoio, style = MaterialTheme.typography.bodySmall,
                )
                Spacer(Modifier.height(8.dp))
                BotaoConfig(Icons.Default.Backup, "Exportar backup") { salvarBackup.launch("meubalcao-backup-${LocalDate.now()}.db") }
                BotaoConfig(Icons.Default.Restore, "Restaurar backup") { confirmar = "restaurar" }
            }
            Cartao {
                Text("Dados do aplicativo", fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                BotaoConfig(Icons.Default.Dataset, "Carregar dados de exemplo") { confirmar = "exemplo" }
                BotaoConfig(Icons.Default.DeleteForever, "Apagar todos os dados", Cores.Perigo) { confirmar = "apagar" }
            }
        }
    }

    when (confirmar) {
        "restaurar" -> Confirmar(
            "Restaurar backup?", "Os dados atuais serão substituídos pelos do arquivo escolhido.", "Escolher arquivo",
            aoConfirmar = { abrirBackup.launch(arrayOf("*/*")) }, aoFechar = { confirmar = null },
        )
        "exemplo" -> Confirmar(
            "Carregar dados de exemplo?", "Todos os dados atuais serão substituídos pela confeitaria fictícia \"Doce Ponto\" (12 produtos, 10 clientes e 100 vendas).", "Carregar",
            aoConfirmar = { escopo.gravar(context, "Dados de exemplo carregados.") { Repositorios.db.carregarExemplo(); Repositorios.notificarMudanca(); meta = "" } },
            aoFechar = { confirmar = null },
        )
        "apagar" -> Confirmar(
            "Apagar todos os dados?", "Produtos, clientes, vendas, campanhas e metas serão apagados deste aparelho. Esta ação não pode ser desfeita.", "Apagar tudo", perigo = true,
            aoConfirmar = { escopo.gravar(context, "Todos os dados foram apagados.") { Repositorios.db.apagarTudo(); Repositorios.notificarMudanca(); meta = "" } },
            aoFechar = { confirmar = null },
        )
    }
}

@Composable
private fun BotaoConfig(icone: ImageVector, texto: String, cor: Color = Cores.Primaria, aoClicar: () -> Unit) {
    OutlinedButton(onClick = aoClicar, modifier = Modifier.fillMaxWidth().height(48.dp).padding(vertical = 2.dp)) {
        Icon(icone, null, tint = cor); Spacer(Modifier.width(8.dp)); Text(texto, color = cor, modifier = Modifier.weight(1f))
    }
}

/** Integrante do grupo: os nomes ficam fixos no código (RF28). */
data class Integrante(val nome: String, val funcao: String)

val integrantes = listOf(
    Integrante("Henrique Bin Estramar", "Gerente de projeto e documentação"),
    Integrante("Lucas Cechett Hofmann", "Modelagem e banco de dados"),
    Integrante("Mauricio Andryell Porgeri dos Santos", "Desenvolvimento do aplicativo (Kotlin)"),
    Integrante("Marcus Vinicius Sena Pereira", "Design de interface, testes e entrega do APK"),
)

/** T17 · Sobre o app e integrantes do grupo (RF28). Não usa o banco de dados. */
@Composable
fun SobreScreen(nav: NavController) {
    Tela("Sobre o app", voltar = { nav.popBackStack() }) { pad ->
        Column(Modifier.fillMaxSize().padding(pad).verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(Modifier.size(80.dp).clip(RoundedCornerShape(22.dp)).background(Cores.Primaria), contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Storefront, null, tint = Color.White, modifier = Modifier.size(44.dp))
                }
                Spacer(Modifier.height(8.dp))
                Text("MeuBalcão", fontWeight = FontWeight.Bold, fontSize = 24.sp)
                Text("Versão ${BuildConfig.VERSION_NAME} (build ${BuildConfig.VERSION_CODE})", color = Cores.TextoApoio)
            }
            Cartao {
                LinhaValor("Projeto", "Projeto Integrador VI-B")
                LinhaValor("Curso", "Análise e Desenv. de Sistemas")
                LinhaValor("Instituição", "Universidade de Caxias do Sul")
            }
            Secao("Integrantes do grupo")
            Cartao {
                integrantes.forEachIndexed { i, p ->
                    if (i > 0) HorizontalDivider(Modifier.padding(vertical = 8.dp), color = Cores.Borda)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Avatar(p.nome, 40.dp)
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(p.nome, fontWeight = FontWeight.SemiBold)
                            Text(p.funcao, style = MaterialTheme.typography.bodySmall, color = Cores.TextoApoio)
                        }
                    }
                }
            }
            Secao("Tecnologia")
            Cartao {
                LinhaValor("Linguagem", "Kotlin")
                LinhaValor("Interface", "Jetpack Compose · Material 3")
                LinhaValor("Banco de dados local", "SQLite (meubalcao.db)")
                LinhaValor("Funciona sem internet", "Sim")
            }
        }
    }
}

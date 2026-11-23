package com.meubalcao.app.ui.screens

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.meubalcao.app.data.repo.Repositorios
import com.meubalcao.app.ui.theme.Cores
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

/** T01 · Abertura: abre/cria o banco local enquanto mostra a marca. */
@Composable
fun AberturaScreen(aoConcluir: () -> Unit) {
    var etapa by remember { mutableStateOf("Abrindo o banco de dados local…") }
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            // Força a abertura: na primeira execução cria as 9 tabelas a partir do schema.sql
            Repositorios.db.writableDatabase
        }
        etapa = "Tudo pronto!"
        delay(700)
        aoConcluir()
    }
    Box(Modifier.fillMaxSize().background(Cores.Primaria), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
            Box(
                Modifier.size(104.dp).clip(RoundedCornerShape(28.dp)).background(Color.White),
                contentAlignment = Alignment.Center,
            ) { Icon(Icons.Default.Storefront, null, tint = Cores.Primaria, modifier = Modifier.size(60.dp)) }
            Spacer(Modifier.height(20.dp))
            Text("MeuBalcão", color = Color.White, fontSize = 34.sp, fontWeight = FontWeight.Bold)
            Text("Vitrine digital e análise de vendas", color = Color.White.copy(alpha = 0.85f), fontSize = 16.sp)
            Spacer(Modifier.height(40.dp))
            LinearProgressIndicator(
                modifier = Modifier.width(200.dp).clip(RoundedCornerShape(50)),
                color = Color.White, trackColor = Color.White.copy(alpha = 0.3f),
            )
            Spacer(Modifier.height(12.dp))
            Text(etapa, color = Color.White.copy(alpha = 0.85f), fontSize = 13.sp)
        }
        Row(
            Modifier.align(Alignment.BottomCenter).padding(32.dp),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center,
        ) {
            Icon(Icons.Default.CloudOff, null, tint = Color.White.copy(alpha = 0.85f), modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(6.dp))
            Text("Funciona sem internet", color = Color.White.copy(alpha = 0.85f), fontSize = 13.sp)
        }
    }
}

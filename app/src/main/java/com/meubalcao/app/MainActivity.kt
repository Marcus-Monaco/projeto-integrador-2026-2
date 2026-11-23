package com.meubalcao.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.meubalcao.app.data.repo.Repositorios
import com.meubalcao.app.ui.MeuBalcaoApp
import com.meubalcao.app.ui.theme.MeuBalcaoTema

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        Repositorios.iniciar(this)
        setContent {
            MeuBalcaoTema { MeuBalcaoApp() }
        }
    }
}

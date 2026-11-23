package com.meubalcao.app.data.repo

import android.content.Context
import com.meubalcao.app.data.db.AppDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Ponto único de acesso aos repositórios. As telas nunca escrevem SQL:
 * chamam os repositórios, que usam o AppDatabase.
 */
object Repositorios {
    lateinit var db: AppDatabase
        private set

    lateinit var perfil: PerfilRepository; private set
    lateinit var catalogo: CatalogoRepository; private set
    lateinit var clientes: ClienteRepository; private set
    lateinit var vendas: VendaRepository; private set
    lateinit var campanhas: CampanhaRepository; private set
    lateinit var relatorios: RelatorioRepository; private set

    private val _versao = MutableStateFlow(0)

    /** Incrementa a cada gravação; as telas observam e recarregam seus dados. */
    val versao: StateFlow<Int> = _versao

    fun notificarMudanca() { _versao.value++ }

    fun iniciar(context: Context, forcar: Boolean = false) {
        if (::db.isInitialized && !forcar) return
        db = AppDatabase.get(context)
        perfil = PerfilRepository(db)
        catalogo = CatalogoRepository(db)
        clientes = ClienteRepository(db)
        vendas = VendaRepository(db)
        campanhas = CampanhaRepository(db)
        relatorios = RelatorioRepository(db)
    }
}

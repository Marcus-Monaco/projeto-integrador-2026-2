package com.meubalcao.app.ui.components

import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.meubalcao.app.data.repo.RegraNegocioException
import com.meubalcao.app.data.repo.Repositorios
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Camada de estado das telas.
 *
 * [carregar] executa a consulta em segundo plano (Dispatchers.IO) e devolve o resultado
 * como estado do Compose. Sempre que algum repositório grava no banco, [Repositorios.versao]
 * muda e todas as telas visíveis recarregam automaticamente.
 */
@Composable
fun <T> carregar(vararg chaves: Any?, consulta: () -> T): T? {
    val versao by Repositorios.versao.collectAsState()
    var valor by remember { mutableStateOf<T?>(null) }
    LaunchedEffect(versao, *chaves) {
        valor = withContext(Dispatchers.IO) { consulta() }
    }
    return valor
}

/**
 * Executa uma gravação em segundo plano e trata os erros de regra de negócio
 * e de restrição do banco (UNIQUE, CHECK, FOREIGN KEY) com uma mensagem amigável.
 */
fun CoroutineScope.gravar(
    context: Context,
    sucesso: String? = null,
    depois: (Any?) -> Unit = {},
    bloco: () -> Any?,
) = launch {
    try {
        val r = withContext(Dispatchers.IO) { bloco() }
        sucesso?.let { Toast.makeText(context, it, Toast.LENGTH_SHORT).show() }
        depois(r)
    } catch (e: RegraNegocioException) {
        Toast.makeText(context, e.message, Toast.LENGTH_LONG).show()
    } catch (e: IllegalArgumentException) {
        Toast.makeText(context, e.message ?: "Dados inválidos.", Toast.LENGTH_LONG).show()
    } catch (e: SQLiteConstraintException) {
        Toast.makeText(context, mensagemRestricao(e), Toast.LENGTH_LONG).show()
    }
}

fun mensagemRestricao(e: SQLiteConstraintException): String {
    val m = e.message.orEmpty()
    return when {
        "cliente.telefone" in m -> "Já existe um cliente com este telefone."
        "categoria.nome" in m -> "Já existe uma categoria com este nome."
        "produto.categoria_id, produto.nome" in m -> "Já existe um produto com este nome nesta categoria."
        "estoque_atual" in m || "CHECK" in m -> "Operação recusada: estoque ou valores inválidos."
        "FOREIGN KEY" in m -> "Registro relacionado não encontrado."
        else -> "Não foi possível salvar: ${m.substringBefore(" (code")}"
    }
}

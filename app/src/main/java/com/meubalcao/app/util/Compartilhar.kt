package com.meubalcao.app.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File

/**
 * Integração com outros apps (WhatsApp, discador, folha de compartilhamento).
 * É a única parte do app que pode precisar de internet: o envio final da mensagem (RNF01).
 */
object Compartilhar {

    /** Mantém só os dígitos e acrescenta o código do Brasil (55) quando faltar. */
    fun telefoneInternacional(telefone: String): String {
        val digitos = telefone.filter(Char::isDigit)
        return if (digitos.length <= 11) "55$digitos" else digitos
    }

    /** Abre uma conversa no WhatsApp com a mensagem pronta (RF04, RF16, RF19). */
    fun whatsapp(context: Context, telefone: String?, mensagem: String) {
        val alvo = telefone?.takeIf { it.any(Char::isDigit) }?.let(::telefoneInternacional) ?: ""
        val uri = Uri.parse("https://wa.me/$alvo?text=" + Uri.encode(mensagem))
        abrir(context, Intent(Intent.ACTION_VIEW, uri))
    }

    fun ligar(context: Context, telefone: String) =
        abrir(context, Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + telefone.filter { it.isDigit() || it == '+' })))

    fun texto(context: Context, texto: String, titulo: String = "Compartilhar") {
        val envio = Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_TEXT, texto) }
        abrir(context, Intent.createChooser(envio, titulo))
    }

    fun imagem(context: Context, bitmap: Bitmap, nomeArquivo: String, texto: String? = null) {
        val arquivo = arquivoTemporario(context, nomeArquivo)
        arquivo.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        arquivoGerado(context, arquivo, "image/png", texto)
    }

    fun arquivoGerado(context: Context, arquivo: File, mime: String, texto: String? = null) {
        val uri = FileProvider.getUriForFile(context, context.packageName + ".fileprovider", arquivo)
        val envio = Intent(Intent.ACTION_SEND).apply {
            type = mime
            putExtra(Intent.EXTRA_STREAM, uri)
            texto?.let { putExtra(Intent.EXTRA_TEXT, it) }
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        abrir(context, Intent.createChooser(envio, "Compartilhar"))
    }

    fun arquivoTemporario(context: Context, nome: String): File =
        File(context.cacheDir, "compartilhar").apply { mkdirs() }.let { File(it, nome) }

    private fun abrir(context: Context, intent: Intent) {
        try {
            context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(context, "Nenhum aplicativo disponível para esta ação.", Toast.LENGTH_LONG).show()
        }
    }
}

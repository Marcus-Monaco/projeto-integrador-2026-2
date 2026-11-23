package com.meubalcao.app.util

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import com.meubalcao.app.data.model.PerfilNegocio
import com.meubalcao.app.data.model.Produto

/**
 * Monta a "vitrine digital" (RF02): uma imagem com o nome do negócio, destaques
 * do catálogo com preços e o QR Code de contato, pronta para WhatsApp/Instagram.
 */
object Vitrine {

    /** Conteúdo do QR Code do negócio: link do WhatsApp, Instagram ou o nome. */
    fun conteudoQr(p: PerfilNegocio): String = when {
        !p.whatsapp.isNullOrBlank() ->
            "https://wa.me/" + Compartilhar.telefoneInternacional(p.whatsapp) + "?text=" +
                android.net.Uri.encode("Olá, ${p.nomeFantasia}! Vi a vitrine e quero fazer um pedido.")
        !p.instagram.isNullOrBlank() -> "https://instagram.com/" + p.instagram.removePrefix("@")
        else -> p.nomeFantasia
    }

    fun textoCatalogo(p: PerfilNegocio, produtos: List<Produto>): String = buildString {
        appendLine("*${p.nomeFantasia}*")
        p.slogan?.let { appendLine("_${it}_") }
        appendLine()
        produtos.forEach { appendLine("• ${it.nome} — ${Fmt.moeda(it.preco)}") }
        appendLine()
        p.whatsapp?.let { appendLine("Pedidos pelo WhatsApp: $it") }
        p.instagram?.let { appendLine("Instagram: $it") }
        p.endereco?.let { appendLine("Endereço: $it") }
        p.horarioFuncionamento?.let { appendLine("Horário: $it") }
    }.trim()

    fun gerar(p: PerfilNegocio, produtos: List<Produto>): Bitmap {
        val largura = 1080
        val linhas = produtos.take(8)
        val altura = 820 + linhas.size * 110 + 520
        val bmp = Bitmap.createBitmap(largura, altura, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)
        val cor = runCatching { Color.parseColor(p.corTema) }.getOrDefault(Color.parseColor("#0F766E"))

        c.drawColor(Color.parseColor("#F8FAFC"))
        val faixa = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = cor }
        c.drawRect(0f, 0f, largura.toFloat(), 560f, faixa)

        val branco = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE; textAlign = Paint.Align.CENTER }
        Imagens.carregar(p.logoPath, 300)?.let { logo ->
            val destino = RectF(largura / 2f - 110, 60f, largura / 2f + 110, 280f)
            c.drawBitmap(logo, null, destino, null)
        }
        branco.textSize = 76f; branco.typeface = Typeface.DEFAULT_BOLD
        c.drawText(p.nomeFantasia, largura / 2f, 380f, branco)
        branco.textSize = 40f; branco.typeface = Typeface.DEFAULT
        p.slogan?.let { c.drawText(it, largura / 2f, 450f, branco) }

        val titulo = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#0F172A"); textSize = 48f; typeface = Typeface.DEFAULT_BOLD }
        c.drawText("Nossos destaques", 80f, 680f, titulo)

        val nome = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#0F172A"); textSize = 42f }
        val preco = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = cor; textSize = 42f; typeface = Typeface.DEFAULT_BOLD; textAlign = Paint.Align.RIGHT }
        val linha = Paint().apply { color = Color.parseColor("#E2E8F0"); strokeWidth = 3f }
        var y = 790f
        linhas.forEach { prod ->
            val texto = if (prod.nome.length > 34) prod.nome.take(32) + "…" else prod.nome
            c.drawText(texto, 80f, y, nome)
            c.drawText(Fmt.moeda(prod.preco), largura - 80f, y, preco)
            c.drawLine(80f, y + 38, largura - 80f, y + 38, linha)
            y += 110f
        }

        val qr = Imagens.qrCode(conteudoQr(p), 360)
        c.drawBitmap(qr, 80f, y + 20, null)
        val info = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#334155"); textSize = 36f }
        var yi = y + 90
        listOfNotNull(
            p.whatsapp?.let { "WhatsApp: $it" },
            p.instagram?.let { "Instagram: $it" },
            p.horarioFuncionamento,
            p.endereco,
        ).forEach { t ->
            c.drawText(if (t.length > 30) t.take(29) + "…" else t, 480f, yi, info); yi += 60
        }
        info.textSize = 30f; info.color = Color.parseColor("#64748B")
        c.drawText("Aponte a câmera para o QR Code", 480f, y + 360, info)
        return bmp
    }
}

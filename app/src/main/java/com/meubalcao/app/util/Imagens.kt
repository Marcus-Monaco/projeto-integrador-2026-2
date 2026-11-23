package com.meubalcao.app.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import java.io.File

object Imagens {

    /**
     * Copia a foto escolhida para a pasta privada do app, já reduzida (máx. 800 px),
     * e devolve o caminho que é gravado em produto.imagem_path / perfil_negocio.logo_path.
     */
    fun salvarFoto(context: Context, uri: Uri, prefixo: String): String? = runCatching {
        val limites = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, limites) }
        var amostra = 1
        while (maxOf(limites.outWidth, limites.outHeight) / amostra > 1600) amostra *= 2
        val bmp = context.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = amostra })
        } ?: return null
        val escala = 800f / maxOf(bmp.width, bmp.height)
        val final = if (escala < 1f) Bitmap.createScaledBitmap(bmp, (bmp.width * escala).toInt(), (bmp.height * escala).toInt(), true) else bmp
        val pasta = File(context.filesDir, "imagens").apply { mkdirs() }
        val arquivo = File(pasta, "${prefixo}_${System.currentTimeMillis()}.jpg")
        arquivo.outputStream().use { final.compress(Bitmap.CompressFormat.JPEG, 85, it) }
        arquivo.absolutePath
    }.getOrNull()

    fun carregar(caminho: String?, maxLado: Int = 600): Bitmap? {
        if (caminho.isNullOrBlank() || !File(caminho).exists()) return null
        val limites = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(caminho, limites)
        var amostra = 1
        while (maxOf(limites.outWidth, limites.outHeight) / (amostra * 2) >= maxLado) amostra *= 2
        return BitmapFactory.decodeFile(caminho, BitmapFactory.Options().apply { inSampleSize = amostra })
    }

    /** Gera o QR Code (RF03) com a biblioteca ZXing, totalmente offline. */
    fun qrCode(conteudo: String, tamanho: Int = 512, cor: Int = Color.BLACK): Bitmap {
        val matriz = QRCodeWriter().encode(
            conteudo, BarcodeFormat.QR_CODE, tamanho, tamanho,
            mapOf(EncodeHintType.CHARACTER_SET to "UTF-8", EncodeHintType.MARGIN to 1),
        )
        val pixels = IntArray(tamanho * tamanho) { i ->
            if (matriz[i % tamanho, i / tamanho]) cor else Color.WHITE
        }
        return Bitmap.createBitmap(pixels, tamanho, tamanho, Bitmap.Config.ARGB_8888)
    }
}

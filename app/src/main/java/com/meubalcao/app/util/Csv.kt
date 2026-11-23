package com.meubalcao.app.util

import java.io.File

/** Exportação de relatórios em CSV (RF25), no padrão aceito pelo Excel/Planilhas em português. */
object Csv {
    val CABECALHO_VENDAS = listOf(
        "venda", "data_hora", "cliente", "canal", "forma_pagamento", "status",
        "produto", "quantidade", "preco_unitario", "subtotal", "desconto_venda", "total_venda",
    )

    /** Usa ';' como separador (padrão brasileiro) e aspas quando necessário. */
    fun linha(campos: List<String>): String = campos.joinToString(";") { c ->
        if (c.any { it == ';' || it == '"' || it == '\n' }) "\"" + c.replace("\"", "\"\"") + "\"" else c
    }

    fun escrever(arquivo: File, cabecalho: List<String>, linhas: List<List<String>>) {
        arquivo.bufferedWriter(Charsets.UTF_8).use { w ->
            w.write("﻿") // BOM: o Excel reconhece acentos
            w.write(linha(cabecalho)); w.newLine()
            linhas.forEach { w.write(linha(it.map { v -> v.toDoubleOrNull()?.takeIf { _ -> v.contains('.') }?.let { Fmt.numero(it, 2) } ?: v })); w.newLine() }
        }
    }
}

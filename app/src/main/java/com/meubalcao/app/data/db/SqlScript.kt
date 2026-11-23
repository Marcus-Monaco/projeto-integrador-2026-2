package com.meubalcao.app.data.db

/**
 * Divide um script SQL em comandos individuais.
 * Ignora comentários de linha (--) e respeita textos entre aspas simples,
 * para que um ';' ou '--' dentro de uma mensagem não quebre o comando.
 */
object SqlScript {
    fun dividir(script: String): List<String> {
        val comandos = mutableListOf<String>()
        val atual = StringBuilder()
        var i = 0
        var emTexto = false
        while (i < script.length) {
            val c = script[i]
            when {
                emTexto -> {
                    atual.append(c)
                    if (c == '\'') {
                        // '' é aspas escapadas dentro do texto
                        if (i + 1 < script.length && script[i + 1] == '\'') {
                            atual.append('\''); i++
                        } else emTexto = false
                    }
                }
                c == '\'' -> { emTexto = true; atual.append(c) }
                c == '-' && i + 1 < script.length && script[i + 1] == '-' -> {
                    while (i < script.length && script[i] != '\n') i++
                    atual.append('\n')
                }
                c == ';' -> {
                    atual.toString().trim().takeIf { it.isNotEmpty() }?.let(comandos::add)
                    atual.clear()
                }
                else -> atual.append(c)
            }
            i++
        }
        atual.toString().trim().takeIf { it.isNotEmpty() }?.let(comandos::add)
        return comandos
    }
}

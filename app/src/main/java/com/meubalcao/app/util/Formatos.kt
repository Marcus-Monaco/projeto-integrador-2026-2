package com.meubalcao.app.util

import java.text.NumberFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Formatação em português do Brasil (moeda, números e datas). */
object Fmt {
    val ptBR: Locale = Locale.forLanguageTag("pt-BR")
    private val moeda = NumberFormat.getCurrencyInstance(ptBR)
    private val dbHora = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    // STRICT: recusa datas inexistentes como 31/02 (em vez de ajustar para 29/02)
    private val br = DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(java.time.format.ResolverStyle.STRICT)
    private val brHora = DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm")
    private val hora = DateTimeFormatter.ofPattern("HH:mm")

    fun moeda(v: Double): String = moeda.format(v).replace(' ', ' ')
    fun numero(v: Double, casas: Int = 1): String = String.format(ptBR, "%.${casas}f", v)

    /** Converte texto digitado ("12,50" ou "12.50") em Double. */
    fun lerValor(texto: String): Double? {
        val t = texto.trim().removePrefix("R$").trim()
        return if (t.contains(',')) t.replace(".", "").replace(',', '.').toDoubleOrNull() else t.toDoubleOrNull()
    }

    fun valorEditavel(v: Double): String = if (v == 0.0) "" else numero(v, 2)

    private fun parse(s: String): LocalDateTime? = runCatching { LocalDateTime.parse(s, dbHora) }.getOrNull()
        ?: runCatching { LocalDate.parse(s.take(10)).atStartOfDay() }.getOrNull()

    fun dataHora(s: String?): String = s?.let { parse(it)?.format(brHora) } ?: "-"
    fun data(s: String?): String = s?.let { parse(it)?.format(br) } ?: "-"
    fun hora(s: String?): String = s?.let { parse(it)?.format(hora) } ?: ""

    /** "2026-11-23" → "Hoje", "Ontem" ou "segunda-feira, 16/11/2026". */
    fun diaRelativo(s: String): String {
        val d = parse(s)?.toLocalDate() ?: return s
        val hoje = LocalDate.now()
        return when (d) {
            hoje -> "Hoje"
            hoje.minusDays(1) -> "Ontem"
            else -> d.format(DateTimeFormatter.ofPattern("EEEE, dd/MM/yyyy", ptBR))
        }
    }

    fun diaCurto(s: String): String = runCatching {
        LocalDate.parse(s.take(10)).format(DateTimeFormatter.ofPattern("dd/MM"))
    }.getOrDefault(s)

    fun diaSemana(s: String): String = runCatching {
        LocalDate.parse(s.take(10)).format(DateTimeFormatter.ofPattern("EEE", ptBR)).take(3).replaceFirstChar { it.uppercase() }
    }.getOrDefault(s)

    /** dd/mm/aaaa → aaaa-mm-dd (formato gravado no banco). null se inválida. */
    fun dataParaBanco(br: String): String? = runCatching {
        LocalDate.parse(br.trim(), this.br).toString()
    }.getOrNull()

    fun dataDoBanco(iso: String?): String = iso?.let { runCatching { LocalDate.parse(it.take(10)).format(br) }.getOrNull() } ?: ""

    fun anoMesAtual(): String = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"))
    fun mesAtualExtenso(): String =
        LocalDate.now().format(DateTimeFormatter.ofPattern("MMMM 'de' yyyy", ptBR)).replaceFirstChar { it.uppercase() }

    fun iniciais(nome: String): String =
        nome.split(" ").filter { it.isNotBlank() }.let { p ->
            ((p.firstOrNull()?.take(1) ?: "") + (if (p.size > 1) p.last().take(1) else "")).uppercase()
        }
}

package com.meubalcao.app.data.model

/** Classes de dados que espelham as tabelas do banco (ver docs/05-modelo-er.md). */

data class PerfilNegocio(
    val nomeFantasia: String = "",
    val slogan: String? = null,
    val descricao: String? = null,
    val whatsapp: String? = null,
    val instagram: String? = null,
    val endereco: String? = null,
    val horarioFuncionamento: String? = null,
    val logoPath: String? = null,
    val corTema: String = "#0F766E",
)

data class Categoria(
    val id: Long = 0,
    val nome: String,
    val descricao: String? = null,
    val ativa: Boolean = true,
    val qtdProdutos: Int = 0,
)

data class Produto(
    val id: Long = 0,
    val categoriaId: Long,
    val categoriaNome: String = "",
    val nome: String,
    val descricao: String? = null,
    val preco: Double,
    val custo: Double = 0.0,
    val estoqueAtual: Int = 0,
    val estoqueMinimo: Int = 0,
    val imagemPath: String? = null,
    val destaque: Boolean = false,
    val ativo: Boolean = true,
) {
    val estoqueBaixo get() = ativo && estoqueAtual <= estoqueMinimo
    val margem get() = if (preco > 0) (preco - custo) / preco * 100 else 0.0
}

enum class Origem(val rotulo: String) {
    BALCAO("Balcão"), WHATSAPP("WhatsApp"), INSTAGRAM("Instagram"), INDICACAO("Indicação"), OUTRO("Outro")
}

data class Cliente(
    val id: Long = 0,
    val nome: String,
    val telefone: String? = null,
    val email: String? = null,
    val dataNascimento: String? = null, // AAAA-MM-DD
    val cidade: String? = null,
    val origem: Origem = Origem.BALCAO,
    val aceitaPromocoes: Boolean = true,
    val observacoes: String? = null,
    val criadoEm: String = "",
)

/** Cliente com os números calculados a partir das vendas concluídas. */
data class ClienteResumo(
    val cliente: Cliente,
    val compras: Int,
    val totalGasto: Double,
    val ultimaCompra: String?,
    val diasSemComprar: Int?,
    val aniversariante: Boolean,
) {
    val ticketMedio get() = if (compras > 0) totalGasto / compras else 0.0
    val inativo get() = diasSemComprar != null && diasSemComprar > 30
}

enum class Segmento(val rotulo: String) {
    TODOS("Todos"), RECORRENTES("Recorrentes"), INATIVOS("Inativos"), NOVOS("Novos"), ANIVERSARIANTES("Aniversariantes")
}

enum class Canal(val rotulo: String) {
    BALCAO("Balcão"), WHATSAPP("WhatsApp"), INSTAGRAM("Instagram"), DELIVERY("Delivery"), OUTRO("Outro")
}

enum class FormaPagamento(val rotulo: String) {
    PIX("Pix"), DINHEIRO("Dinheiro"), CARTAO_DEBITO("Débito"), CARTAO_CREDITO("Crédito"), OUTRO("Outro")
}

enum class StatusVenda(val rotulo: String) { CONCLUIDA("Concluída"), CANCELADA("Cancelada") }

data class Venda(
    val id: Long = 0,
    val clienteId: Long? = null,
    val clienteNome: String? = null,
    val dataHora: String = "",
    val canal: Canal = Canal.BALCAO,
    val formaPagamento: FormaPagamento = FormaPagamento.PIX,
    val desconto: Double = 0.0,
    val valorTotal: Double = 0.0,
    val status: StatusVenda = StatusVenda.CONCLUIDA,
    val observacao: String? = null,
    val qtdItens: Int = 0,
)

data class ItemVenda(
    val id: Long = 0,
    val produtoId: Long,
    val produtoNome: String = "",
    val quantidade: Int,
    val precoUnitario: Double,
    val custoUnitario: Double = 0.0,
) {
    val subtotal get() = quantidade * precoUnitario
}

enum class StatusCampanha(val rotulo: String) { RASCUNHO("Rascunho"), ATIVA("Ativa"), ENCERRADA("Encerrada") }
enum class CanalCampanha(val rotulo: String) { WHATSAPP("WhatsApp"), INSTAGRAM("Instagram"), OUTRO("Outro") }

data class Campanha(
    val id: Long = 0,
    val titulo: String,
    val mensagem: String,
    val produtoId: Long? = null,
    val produtoNome: String? = null,
    val segmento: Segmento = Segmento.TODOS,
    val canal: CanalCampanha = CanalCampanha.WHATSAPP,
    val dataInicio: String? = null,
    val dataFim: String? = null,
    val status: StatusCampanha = StatusCampanha.RASCUNHO,
    val alcancados: Int = 0,
    val compraramDepois: Int = 0,
)

data class Indicadores(val vendas: Int, val faturamento: Double, val ticketMedio: Double, val lucroBruto: Double)

data class MetaMes(val anoMes: String, val valorMeta: Double, val realizado: Double) {
    val percentual get() = if (valorMeta > 0) realizado / valorMeta * 100 else 0.0
}

/** Linha genérica de ranking/gráfico: rótulo + quantidade + valor. */
data class Linha(val rotulo: String, val quantidade: Int, val valor: Double)

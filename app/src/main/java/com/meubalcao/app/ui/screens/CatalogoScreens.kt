package com.meubalcao.app.ui.screens

import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.meubalcao.app.data.model.Categoria
import com.meubalcao.app.data.model.Produto
import com.meubalcao.app.data.repo.Repositorios
import com.meubalcao.app.ui.Rotas
import com.meubalcao.app.ui.components.Campo
import com.meubalcao.app.ui.components.Cartao
import com.meubalcao.app.ui.components.Chave
import com.meubalcao.app.ui.components.Filtros
import com.meubalcao.app.ui.components.Foto
import com.meubalcao.app.ui.components.GraficoColunas
import com.meubalcao.app.ui.components.Indicador
import com.meubalcao.app.ui.components.LinhaValor
import com.meubalcao.app.ui.components.Selecao
import com.meubalcao.app.ui.components.Selo
import com.meubalcao.app.ui.components.Tela
import com.meubalcao.app.ui.components.Vazio
import com.meubalcao.app.ui.components.carregar
import com.meubalcao.app.ui.components.gravar
import com.meubalcao.app.ui.theme.Cores
import com.meubalcao.app.util.Compartilhar
import com.meubalcao.app.util.Fmt
import com.meubalcao.app.util.Imagens
import com.meubalcao.app.util.Vitrine
import java.time.LocalDate

/** T03 · Catálogo: busca, filtro por categoria e grade de produtos (RF02, RF05 a RF09). */
@Composable
fun CatalogoScreen(nav: NavController) {
    var busca by rememberSaveable { mutableStateOf("") }
    var categoriaId by rememberSaveable { mutableStateOf<Long?>(null) }
    val categorias = carregar { Repositorios.catalogo.categorias(somenteAtivas = true) } ?: emptyList()
    val produtos = carregar(busca, categoriaId) { Repositorios.catalogo.produtos(busca, categoriaId) }
    val context = LocalContext.current
    val escopo = rememberCoroutineScope()

    Tela(
        titulo = "Catálogo",
        acoes = {
            IconButton(onClick = { nav.navigate(Rotas.CATEGORIAS) }) { Icon(Icons.Default.Category, "Categorias") }
            IconButton(onClick = {
                // Compartilha a vitrine digital (imagem + texto do catálogo)
                escopo.gravar(context, depois = { r ->
                    val (bmp, txt) = r as Pair<*, *>
                    Compartilhar.imagem(context, bmp as Bitmap, "vitrine.png", txt as String)
                }) {
                    val p = Repositorios.perfil.obter() ?: throw IllegalArgumentException("Cadastre o seu negócio em Mais → Meu negócio.")
                    val lista = Repositorios.catalogo.destaques()
                    Vitrine.gerar(p, lista) to Vitrine.textoCatalogo(p, lista)
                }
            }) { Icon(Icons.Default.Share, "Compartilhar vitrine") }
        },
        botao = {
            ExtendedFloatingActionButton(
                onClick = { nav.navigate(Rotas.produtoForm()) },
                icon = { Icon(Icons.Default.Add, null) }, text = { Text("Novo produto") },
                containerColor = Cores.Primaria, contentColor = androidx.compose.ui.graphics.Color.White,
            )
        },
    ) { pad ->
        Column(Modifier.fillMaxSize().padding(pad)) {
            OutlinedTextField(
                value = busca, onValueChange = { busca = it },
                placeholder = { Text("Buscar produto") }, leadingIcon = { Icon(Icons.Default.Search, null) },
                singleLine = true, shape = androidx.compose.foundation.shape.RoundedCornerShape(50),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            )
            Filtros(
                listOf<Categoria?>(null) + categorias, categorias.firstOrNull { it.id == categoriaId },
                { it?.nome ?: "Todos" }, { categoriaId = it?.id },
                Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
            when {
                produtos == null -> Unit
                produtos.isEmpty() -> Vazio(
                    Icons.Default.Inventory2,
                    if (busca.isBlank()) "Cadastre seu primeiro produto" else "Nenhum produto encontrado",
                    if (busca.isBlank()) "Os produtos aparecem aqui e na sua vitrine digital." else "Tente outro nome ou categoria.",
                )
                else -> LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 88.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    item(span = { GridItemSpan(2) }) {
                        Text("${produtos.size} produto(s)", color = Cores.TextoApoio, style = MaterialTheme.typography.bodySmall)
                    }
                    items(produtos, key = { it.id }) { p -> CartaoProduto(p) { nav.navigate(Rotas.produto(p.id)) } }
                }
            }
        }
    }
}

@Composable
private fun CartaoProduto(p: Produto, aoClicar: () -> Unit) {
    Cartao(onClick = aoClicar, cor = if (p.ativo) androidx.compose.ui.graphics.Color.White else Cores.Fundo) {
        Box {
            Foto(p.imagemPath, Modifier.fillMaxWidth().aspectRatio(1.3f), Icons.Default.Inventory2)
            if (p.destaque) Icon(Icons.Default.Star, "Destaque", tint = Cores.Alerta, modifier = Modifier.align(Alignment.TopEnd).padding(6.dp))
        }
        Spacer(Modifier.height(8.dp))
        Text(p.nome, fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis, minLines = 2)
        Text(Fmt.moeda(p.preco), color = Cores.Primaria, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(4.dp))
        when {
            !p.ativo -> Selo("Inativo", Cores.TextoApoio, Cores.Borda)
            p.estoqueBaixo -> Selo("Estoque: ${p.estoqueAtual}")
            else -> Text("Estoque: ${p.estoqueAtual}", style = MaterialTheme.typography.bodySmall, color = Cores.TextoApoio)
        }
    }
}

/** T04 · Cadastro/edição de produto (RF05, RF06, RF08). */
@Composable
fun ProdutoFormScreen(nav: NavController, id: Long) {
    val context = LocalContext.current
    val escopo = rememberCoroutineScope()
    val categorias = carregar { Repositorios.catalogo.categorias(somenteAtivas = true) } ?: emptyList()
    val original = carregar(id) { if (id > 0) Repositorios.catalogo.produto(id) else null }

    var iniciado by rememberSaveable { mutableStateOf(id == 0L) }
    var nome by rememberSaveable { mutableStateOf("") }
    var descricao by rememberSaveable { mutableStateOf("") }
    var categoriaId by rememberSaveable { mutableStateOf<Long?>(null) }
    var preco by rememberSaveable { mutableStateOf("") }
    var custo by rememberSaveable { mutableStateOf("") }
    var estoque by rememberSaveable { mutableStateOf("") }
    var minimo by rememberSaveable { mutableStateOf("") }
    var imagem by rememberSaveable { mutableStateOf<String?>(null) }
    var destaque by rememberSaveable { mutableStateOf(false) }
    var ativo by rememberSaveable { mutableStateOf(true) }
    var tentou by remember { mutableStateOf(false) }
    var novaCategoria by remember { mutableStateOf(false) }

    LaunchedEffect(original) {
        val p = original ?: return@LaunchedEffect
        if (iniciado) return@LaunchedEffect
        nome = p.nome; descricao = p.descricao.orEmpty(); categoriaId = p.categoriaId
        preco = Fmt.valorEditavel(p.preco); custo = Fmt.valorEditavel(p.custo)
        estoque = p.estoqueAtual.toString(); minimo = p.estoqueMinimo.toString()
        imagem = p.imagemPath; destaque = p.destaque; ativo = p.ativo
        iniciado = true
    }
    LaunchedEffect(categorias) { if (categoriaId == null && id == 0L) categoriaId = categorias.firstOrNull()?.id }

    val seletor = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) escopo.gravar(context, depois = { imagem = it as String? }) { Imagens.salvarFoto(context, uri, "produto") }
    }

    val precoNum = Fmt.lerValor(preco)
    val erroNome = if (tentou && nome.isBlank()) "Informe o nome" else null
    val erroPreco = if (tentou && (precoNum == null || precoNum < 0)) "Informe um preço válido" else null

    Tela(titulo = if (id == 0L) "Novo produto" else "Editar produto", voltar = { nav.popBackStack() }) { pad ->
        Column(Modifier.fillMaxSize().padding(pad).verticalScroll(rememberScrollState()).padding(16.dp)) {
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Foto(imagem, Modifier.size(140.dp).clickable {
                    seletor.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                }, Icons.Default.AddAPhoto)
            }
            TextButton(
                onClick = { seletor.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                modifier = Modifier.align(Alignment.CenterHorizontally),
            ) { Text(if (imagem == null) "Adicionar foto" else "Trocar foto") }

            Campo("Nome do produto *", nome, { nome = it }, erro = erroNome)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Selecao("Categoria *", categorias, categorias.firstOrNull { it.id == categoriaId }, { it.nome }, { categoriaId = it.id }, Modifier.weight(1f), vazio = "Cadastre uma categoria")
                TextButton(onClick = { novaCategoria = true }) { Text("Nova") }
            }
            Campo("Descrição", descricao, { descricao = it }, linhas = 2)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Campo("Preço de venda *", preco, { preco = it }, Modifier.weight(1f), KeyboardType.Decimal, erro = erroPreco, prefixo = "R$ ")
                Campo("Custo", custo, { custo = it }, Modifier.weight(1f), KeyboardType.Decimal, prefixo = "R$ ")
            }
            val c = Fmt.lerValor(custo) ?: 0.0
            if (precoNum != null && precoNum > 0) {
                Text("Margem: ${Fmt.numero((precoNum - c) / precoNum * 100)}% · lucro de ${Fmt.moeda(precoNum - c)} por unidade",
                    style = MaterialTheme.typography.bodySmall, color = Cores.TextoApoio)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Campo("Estoque atual", estoque, { estoque = it.filter(Char::isDigit) }, Modifier.weight(1f), KeyboardType.Number)
                Campo("Estoque mínimo", minimo, { minimo = it.filter(Char::isDigit) }, Modifier.weight(1f), KeyboardType.Number, dica = "Gera alerta")
            }
            Chave("Destaque da vitrine", "Aparece primeiro no catálogo e na vitrine compartilhada", destaque) { destaque = it }
            Chave("Produto ativo", "Produtos inativos não aparecem para venda", ativo) { ativo = it }
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = {
                    tentou = true
                    val cat = categoriaId
                    if (nome.isBlank() || precoNum == null || precoNum < 0) return@Button
                    if (cat == null) { novaCategoria = true; return@Button }
                    escopo.gravar(context, "Produto salvo.", depois = { nav.popBackStack() }) {
                        Repositorios.catalogo.salvarProduto(
                            Produto(
                                id = id, categoriaId = cat, nome = nome, descricao = descricao,
                                preco = precoNum, custo = Fmt.lerValor(custo) ?: 0.0,
                                estoqueAtual = estoque.toIntOrNull() ?: 0, estoqueMinimo = minimo.toIntOrNull() ?: 0,
                                imagemPath = imagem, destaque = destaque, ativo = ativo,
                            ),
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
            ) { Text("Salvar produto") }
        }
    }

    if (novaCategoria) DialogoCategoria(null, { novaCategoria = false }) { novoId -> categoriaId = novoId }
}

/** Diálogo para criar/editar categoria (RF05). */
@Composable
fun DialogoCategoria(categoria: Categoria?, aoFechar: () -> Unit, aoSalvar: (Long) -> Unit = {}) {
    val context = LocalContext.current
    val escopo = rememberCoroutineScope()
    var nome by remember { mutableStateOf(categoria?.nome.orEmpty()) }
    var descricao by remember { mutableStateOf(categoria?.descricao.orEmpty()) }
    AlertDialog(
        onDismissRequest = aoFechar,
        title = { Text(if (categoria == null) "Nova categoria" else "Editar categoria") },
        text = {
            Column {
                Campo("Nome *", nome, { nome = it })
                Campo("Descrição", descricao, { descricao = it })
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (nome.isBlank()) return@TextButton
                escopo.gravar(context, "Categoria salva.", depois = { aoSalvar(it as Long); aoFechar() }) {
                    Repositorios.catalogo.salvarCategoria(
                        (categoria ?: Categoria(nome = nome)).copy(nome = nome, descricao = descricao),
                    )
                }
            }) { Text("Salvar") }
        },
        dismissButton = { TextButton(onClick = aoFechar) { Text("Cancelar") } },
    )
}

/** Gerenciar categorias: criar, editar e desativar (RF05, RN06). */
@Composable
fun CategoriasScreen(nav: NavController) {
    val categorias = carregar { Repositorios.catalogo.categorias() }
    var editando by remember { mutableStateOf<Categoria?>(null) }
    var nova by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val escopo = rememberCoroutineScope()

    Tela("Categorias", voltar = { nav.popBackStack() }, botao = {
        ExtendedFloatingActionButton(onClick = { nova = true }, icon = { Icon(Icons.Default.Add, null) }, text = { Text("Nova categoria") })
    }) { pad ->
        LazyColumn(Modifier.fillMaxSize().padding(pad), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (categorias?.isEmpty() == true) item { Vazio(Icons.Default.Category, "Nenhuma categoria", "Crie categorias para organizar o catálogo.") }
            items(categorias.orEmpty(), key = { it.id }) { c ->
                Cartao(onClick = { editando = c }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(c.nome, fontWeight = FontWeight.SemiBold, color = if (c.ativa) Cores.Texto else Cores.TextoApoio)
                            Text("${c.qtdProdutos} produto(s)" + (c.descricao?.let { " · $it" } ?: ""), style = MaterialTheme.typography.bodySmall, color = Cores.TextoApoio)
                        }
                        androidx.compose.material3.Switch(checked = c.ativa, onCheckedChange = { ativa ->
                            escopo.gravar(context) { Repositorios.catalogo.salvarCategoria(c.copy(ativa = ativa)) }
                        })
                    }
                }
            }
        }
    }
    if (nova) DialogoCategoria(null, { nova = false })
    editando?.let { DialogoCategoria(it, { editando = null }) }
}

/** T05 · Detalhe do produto: desempenho, compartilhar, QR Code e editar (RF03, RF04, RF06). */
@Composable
fun ProdutoDetalheScreen(nav: NavController, id: Long) {
    val context = LocalContext.current
    val dados = carregar(id) {
        val p = Repositorios.catalogo.produto(id)
        Triple(p, Repositorios.catalogo.desempenho30Dias(id), Repositorios.catalogo.unidadesPorDia(id)) to Repositorios.perfil.obter()
    }
    var mostrarQr by remember { mutableStateOf(false) }
    val p = dados?.first?.first

    Tela(p?.nome ?: "Produto", voltar = { nav.popBackStack() }, acoes = {
        IconButton(onClick = { nav.navigate(Rotas.produtoForm(id)) }) { Icon(Icons.Default.Edit, "Editar") }
    }) { pad ->
        if (dados == null || p == null) return@Tela
        val (_, desempenho, porDia) = dados.first
        val perfil = dados.second
        val mensagem = buildString {
            append("*${p.nome}*\n")
            p.descricao?.let { append("$it\n") }
            append("Por apenas *${Fmt.moeda(p.preco)}*")
            perfil?.let { append("\n\n${it.nomeFantasia}") }
        }
        Column(Modifier.fillMaxSize().padding(pad).verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Foto(p.imagemPath, Modifier.fillMaxWidth().height(200.dp), Icons.Default.Inventory2)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(p.categoriaNome, color = Cores.TextoApoio, style = MaterialTheme.typography.bodySmall)
                    Text(Fmt.moeda(p.preco), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Cores.Primaria)
                }
                if (p.destaque) Selo("★ Destaque", Cores.Alerta, Cores.AlertaFundo)
                if (!p.ativo) { Spacer(Modifier.width(6.dp)); Selo("Inativo", Cores.TextoApoio, Cores.Borda) }
            }
            p.descricao?.let { Text(it) }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Indicador("Custo", Fmt.moeda(p.custo), Modifier.weight(1f))
                Indicador("Margem", "${Fmt.numero(p.margem)}%", Modifier.weight(1f))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Indicador("Estoque", "${p.estoqueAtual} (mín. ${p.estoqueMinimo})", Modifier.weight(1f))
                Indicador("Vendidos 30 dias", "${desempenho.quantidade} un.", Modifier.weight(1f))
            }
            if (p.estoqueBaixo) Cartao(cor = Cores.AlertaFundo) {
                Text("Estoque baixo: hora de repor este produto.", color = Cores.Alerta, fontWeight = FontWeight.SemiBold)
            }
            Cartao {
                Text("Vendas da semana (unidades)", fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                val hoje = LocalDate.now()
                GraficoColunas((6 downTo 0).map { d ->
                    val dia = hoje.minusDays(d.toLong()).toString()
                    Fmt.diaSemana(dia) to (porDia[dia] ?: 0.0)
                }, altura = 100, rotuloValor = { Fmt.numero(it, 0) })
                Spacer(Modifier.height(4.dp))
                LinhaValor("Receita nos últimos 30 dias", Fmt.moeda(desempenho.valor))
            }
            Button(onClick = { Compartilhar.whatsapp(context, null, mensagem) }, modifier = Modifier.fillMaxWidth().height(48.dp)) {
                Icon(Icons.Default.Share, null); Spacer(Modifier.width(8.dp)); Text("Compartilhar no WhatsApp")
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = { mostrarQr = true }, modifier = Modifier.weight(1f).height(48.dp)) {
                    Icon(Icons.Default.QrCode2, null); Spacer(Modifier.width(6.dp)); Text("QR Code")
                }
                OutlinedButton(onClick = { nav.navigate(Rotas.produtoForm(id)) }, modifier = Modifier.weight(1f).height(48.dp)) {
                    Icon(Icons.Default.Edit, null); Spacer(Modifier.width(6.dp)); Text("Editar")
                }
            }
        }
        if (mostrarQr) {
            // O QR abre o WhatsApp do negócio já com o pedido do produto
            val conteudo = perfil?.whatsapp?.takeIf { it.isNotBlank() }?.let {
                "https://wa.me/${Compartilhar.telefoneInternacional(it)}?text=" + android.net.Uri.encode("Olá! Quero pedir: ${p.nome} (${Fmt.moeda(p.preco)})")
            } ?: mensagem
            DialogoQr(p.nome, conteudo, "qr-produto.png", mensagem) { mostrarQr = false }
        }
    }
}

@Composable
fun DialogoQr(titulo: String, conteudo: String, arquivo: String, texto: String?, aoFechar: () -> Unit) {
    val context = LocalContext.current
    val bmp = remember(conteudo) { Imagens.qrCode(conteudo, 600) }
    AlertDialog(
        onDismissRequest = aoFechar,
        title = { Text(titulo) },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Image(bmp.asImageBitmap(), "QR Code", Modifier.size(240.dp))
                Spacer(Modifier.height(8.dp))
                Text("Ao ler o código, o cliente abre a conversa no WhatsApp.", style = MaterialTheme.typography.bodySmall, color = Cores.TextoApoio)
            }
        },
        confirmButton = { TextButton(onClick = { Compartilhar.imagem(context, bmp, arquivo, texto) }) { Text("Compartilhar") } },
        dismissButton = { TextButton(onClick = aoFechar) { Text("Fechar") } },
    )
}

-- =====================================================================
-- MeuBalcão - Esquema do banco de dados local (SQLite 3)
-- Arquivo no aparelho: meubalcao.db  |  Versão do esquema: 1
-- Observações:
--   * Datas/horas são guardadas como TEXT no formato ISO-8601
--     (AAAA-MM-DD HH:MM:SS), padrão recomendado para o SQLite.
--   * Valores lógicos (verdadeiro/falso) usam INTEGER com 0 ou 1.
--   * As chaves estrangeiras exigem PRAGMA foreign_keys = ON,
--     que o app ativa a cada abertura do banco (ver docs/10).
-- =====================================================================

PRAGMA foreign_keys = ON;

-- 1) Perfil do negócio (vitrine digital) - sempre uma única linha (id = 1)
CREATE TABLE perfil_negocio (
    id                    INTEGER PRIMARY KEY CHECK (id = 1),
    nome_fantasia         TEXT    NOT NULL,
    slogan                TEXT,
    descricao             TEXT,
    whatsapp              TEXT,
    instagram             TEXT,
    endereco              TEXT,
    horario_funcionamento TEXT,
    logo_path             TEXT,
    cor_tema              TEXT    NOT NULL DEFAULT '#0F766E',
    atualizado_em         TEXT    NOT NULL DEFAULT (datetime('now','localtime'))
);

-- 2) Categorias do catálogo
CREATE TABLE categoria (
    id        INTEGER PRIMARY KEY AUTOINCREMENT,
    nome      TEXT    NOT NULL UNIQUE COLLATE NOCASE,
    descricao TEXT,
    ativa     INTEGER NOT NULL DEFAULT 1 CHECK (ativa IN (0,1))
);

-- 3) Produtos / serviços do catálogo
CREATE TABLE produto (
    id             INTEGER PRIMARY KEY AUTOINCREMENT,
    categoria_id   INTEGER NOT NULL
                   REFERENCES categoria(id) ON UPDATE CASCADE ON DELETE RESTRICT,
    nome           TEXT    NOT NULL,
    descricao      TEXT,
    preco          REAL    NOT NULL CHECK (preco >= 0),
    custo          REAL    NOT NULL DEFAULT 0 CHECK (custo >= 0),
    estoque_atual  INTEGER NOT NULL DEFAULT 0 CHECK (estoque_atual >= 0),
    estoque_minimo INTEGER NOT NULL DEFAULT 0 CHECK (estoque_minimo >= 0),
    imagem_path    TEXT,
    destaque       INTEGER NOT NULL DEFAULT 0 CHECK (destaque IN (0,1)),
    ativo          INTEGER NOT NULL DEFAULT 1 CHECK (ativo IN (0,1)),
    criado_em      TEXT    NOT NULL DEFAULT (datetime('now','localtime')),
    UNIQUE (categoria_id, nome)
);

-- 4) Clientes
CREATE TABLE cliente (
    id               INTEGER PRIMARY KEY AUTOINCREMENT,
    nome             TEXT    NOT NULL,
    telefone         TEXT    UNIQUE,
    email            TEXT,
    data_nascimento  TEXT,
    cidade           TEXT,
    origem           TEXT    NOT NULL DEFAULT 'BALCAO'
                     CHECK (origem IN ('BALCAO','WHATSAPP','INSTAGRAM','INDICACAO','OUTRO')),
    aceita_promocoes INTEGER NOT NULL DEFAULT 1 CHECK (aceita_promocoes IN (0,1)),
    observacoes      TEXT,
    criado_em        TEXT    NOT NULL DEFAULT (datetime('now','localtime'))
);

-- 5) Vendas (cabeçalho). O cliente é opcional (venda de balcão sem cadastro)
CREATE TABLE venda (
    id              INTEGER PRIMARY KEY AUTOINCREMENT,
    cliente_id      INTEGER
                    REFERENCES cliente(id) ON UPDATE CASCADE ON DELETE SET NULL,
    data_hora       TEXT    NOT NULL DEFAULT (datetime('now','localtime')),
    canal           TEXT    NOT NULL DEFAULT 'BALCAO'
                    CHECK (canal IN ('BALCAO','WHATSAPP','INSTAGRAM','DELIVERY','OUTRO')),
    forma_pagamento TEXT    NOT NULL DEFAULT 'PIX'
                    CHECK (forma_pagamento IN ('DINHEIRO','PIX','CARTAO_DEBITO','CARTAO_CREDITO','OUTRO')),
    desconto        REAL    NOT NULL DEFAULT 0 CHECK (desconto >= 0),
    valor_total     REAL    NOT NULL CHECK (valor_total >= 0),
    status          TEXT    NOT NULL DEFAULT 'CONCLUIDA'
                    CHECK (status IN ('CONCLUIDA','CANCELADA')),
    observacao      TEXT
);

-- 6) Itens da venda (tabela associativa venda x produto)
CREATE TABLE item_venda (
    id             INTEGER PRIMARY KEY AUTOINCREMENT,
    venda_id       INTEGER NOT NULL
                   REFERENCES venda(id) ON UPDATE CASCADE ON DELETE CASCADE,
    produto_id     INTEGER NOT NULL
                   REFERENCES produto(id) ON UPDATE CASCADE ON DELETE RESTRICT,
    quantidade     INTEGER NOT NULL CHECK (quantidade > 0),
    preco_unitario REAL    NOT NULL CHECK (preco_unitario >= 0),
    custo_unitario REAL    NOT NULL DEFAULT 0 CHECK (custo_unitario >= 0),
    subtotal       REAL    NOT NULL CHECK (subtotal >= 0),
    UNIQUE (venda_id, produto_id)
);

-- 7) Campanhas promocionais (divulgação digital)
CREATE TABLE campanha (
    id          INTEGER PRIMARY KEY AUTOINCREMENT,
    titulo      TEXT    NOT NULL,
    mensagem    TEXT    NOT NULL,
    produto_id  INTEGER
                REFERENCES produto(id) ON UPDATE CASCADE ON DELETE SET NULL,
    segmento    TEXT    NOT NULL DEFAULT 'TODOS'
                CHECK (segmento IN ('TODOS','NOVOS','RECORRENTES','INATIVOS','ANIVERSARIANTES')),
    canal       TEXT    NOT NULL DEFAULT 'WHATSAPP'
                CHECK (canal IN ('WHATSAPP','INSTAGRAM','OUTRO')),
    data_inicio TEXT,
    data_fim    TEXT,
    status      TEXT    NOT NULL DEFAULT 'RASCUNHO'
                CHECK (status IN ('RASCUNHO','ATIVA','ENCERRADA')),
    criada_em   TEXT    NOT NULL DEFAULT (datetime('now','localtime')),
    CHECK (data_inicio IS NULL OR data_fim IS NULL OR data_fim >= data_inicio)
);

-- 8) Envios de campanha (tabela associativa campanha x cliente)
CREATE TABLE envio_campanha (
    campanha_id INTEGER NOT NULL
                REFERENCES campanha(id) ON UPDATE CASCADE ON DELETE CASCADE,
    cliente_id  INTEGER NOT NULL
                REFERENCES cliente(id) ON UPDATE CASCADE ON DELETE CASCADE,
    enviado_em  TEXT    NOT NULL DEFAULT (datetime('now','localtime')),
    PRIMARY KEY (campanha_id, cliente_id)
);

-- 9) Metas de faturamento por mês
CREATE TABLE meta_mensal (
    id         INTEGER PRIMARY KEY AUTOINCREMENT,
    ano_mes    TEXT    NOT NULL UNIQUE
               CHECK (ano_mes GLOB '[0-9][0-9][0-9][0-9]-[0-1][0-9]'),
    valor_meta REAL    NOT NULL CHECK (valor_meta > 0),
    criada_em  TEXT    NOT NULL DEFAULT (datetime('now','localtime'))
);

-- Índices para acelerar listagens e relatórios
CREATE INDEX idx_produto_categoria ON produto (categoria_id);
CREATE INDEX idx_cliente_nome      ON cliente (nome);
CREATE INDEX idx_venda_data        ON venda (data_hora);
CREATE INDEX idx_venda_cliente     ON venda (cliente_id);
CREATE INDEX idx_item_venda_venda  ON item_venda (venda_id);
CREATE INDEX idx_item_venda_prod   ON item_venda (produto_id);
CREATE INDEX idx_campanha_status   ON campanha (status);

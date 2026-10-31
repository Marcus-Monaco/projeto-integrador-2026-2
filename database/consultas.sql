-- MeuBalcão - Consultas analíticas usadas pelas telas de painel e relatórios
-- Testadas com database/schema.sql + database/seed.sql (SQLite 3)

-- Painel: vendas, faturamento e ticket médio (últimos 30 dias)
SELECT COUNT(*) AS vendas,
       ROUND(COALESCE(SUM(valor_total),0),2) AS faturamento,
       ROUND(COALESCE(AVG(valor_total),0),2) AS ticket_medio
FROM venda
WHERE status = 'CONCLUIDA'
  AND date(data_hora) >= date('now','localtime','-29 days');

-- Painel: lucro bruto (últimos 30 dias)
SELECT ROUND(SUM(v.valor_total) - SUM(c.custo_total),2) AS lucro_bruto
FROM venda v
JOIN (SELECT venda_id, SUM(custo_unitario * quantidade) AS custo_total
      FROM item_venda GROUP BY venda_id) c ON c.venda_id = v.id
WHERE v.status = 'CONCLUIDA'
  AND date(v.data_hora) >= date('now','localtime','-29 days');

-- Gráfico: faturamento por dia (últimos 7 dias)
SELECT date(data_hora) AS dia, ROUND(SUM(valor_total),2) AS faturamento
FROM venda
WHERE status = 'CONCLUIDA'
  AND date(data_hora) >= date('now','localtime','-6 days')
GROUP BY date(data_hora)
ORDER BY dia;

-- Ranking: produtos mais vendidos (últimos 30 dias)
SELECT p.nome, SUM(iv.quantidade) AS unidades, ROUND(SUM(iv.subtotal),2) AS receita
FROM item_venda iv
JOIN venda v   ON v.id = iv.venda_id
JOIN produto p ON p.id = iv.produto_id
WHERE v.status = 'CONCLUIDA'
  AND date(v.data_hora) >= date('now','localtime','-29 days')
GROUP BY p.id
ORDER BY unidades DESC
LIMIT 5;

-- Gráfico: faturamento por canal de venda
SELECT canal, COUNT(*) AS vendas, ROUND(SUM(valor_total),2) AS faturamento
FROM venda
WHERE status = 'CONCLUIDA'
  AND date(data_hora) >= date('now','localtime','-29 days')
GROUP BY canal
ORDER BY faturamento DESC;

-- Alerta: produtos com estoque no mínimo ou abaixo
SELECT nome, estoque_atual, estoque_minimo
FROM produto
WHERE ativo = 1 AND estoque_atual <= estoque_minimo
ORDER BY (estoque_atual - estoque_minimo);

-- Painel: progresso da meta do mês atual
SELECT m.valor_meta,
       ROUND(COALESCE(SUM(v.valor_total),0),2) AS realizado,
       ROUND(100.0 * COALESCE(SUM(v.valor_total),0) / m.valor_meta, 1) AS percentual
FROM meta_mensal m
LEFT JOIN venda v ON strftime('%Y-%m', v.data_hora) = m.ano_mes AND v.status = 'CONCLUIDA'
WHERE m.ano_mes = strftime('%Y-%m','now','localtime')
GROUP BY m.id;

-- Segmento: clientes inativos (sem compra há mais de 30 dias)
SELECT c.nome, MAX(v.data_hora) AS ultima_compra
FROM cliente c
JOIN venda v ON v.cliente_id = c.id AND v.status = 'CONCLUIDA'
WHERE c.aceita_promocoes = 1
GROUP BY c.id
HAVING date(MAX(v.data_hora)) < date('now','localtime','-30 days')
ORDER BY ultima_compra;

-- Segmento: clientes recorrentes (3 ou mais compras)
SELECT c.nome, COUNT(v.id) AS compras, ROUND(SUM(v.valor_total),2) AS total_gasto
FROM cliente c
JOIN venda v ON v.cliente_id = c.id AND v.status = 'CONCLUIDA'
GROUP BY c.id
HAVING COUNT(v.id) >= 3
ORDER BY total_gasto DESC;

-- Segmento: aniversariantes do mês
SELECT nome, telefone
FROM cliente
WHERE aceita_promocoes = 1
  AND strftime('%m', data_nascimento) = strftime('%m','now','localtime');

-- Campanhas: clientes alcançados e quantos compraram depois
SELECT ca.titulo, COUNT(DISTINCT e.cliente_id) AS clientes_alcancados,
       COUNT(DISTINCT v.cliente_id) AS compraram_depois
FROM campanha ca
LEFT JOIN envio_campanha e ON e.campanha_id = ca.id
LEFT JOIN venda v ON v.cliente_id = e.cliente_id AND v.status = 'CONCLUIDA'
                  AND v.data_hora >= e.enviado_em
GROUP BY ca.id;


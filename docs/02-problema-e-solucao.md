# 2. Problema que resolve

## 2.1 Contexto

Pequenos negócios (microempreendedores individuais, comércios de bairro, confeitarias, lojas de roupas, artesãos e autônomos) vendem por vários canais ao mesmo tempo: no balcão, pelo WhatsApp e pelo Instagram. Na prática, a gestão costuma ser feita em **caderno, planilhas soltas, mensagens e memória**, e a divulgação, de forma improvisada.

A digitalização aumentou a concorrência: o cliente espera encontrar preço, foto e horário na internet e receber atendimento rápido. Ao mesmo tempo, o empreendedor tem **pouco tempo, pouco dinheiro para ferramentas e, muitas vezes, conexão de internet instável**.

## 2.2 Problemas identificados

| ID | Problema | Consequência para o negócio |
|---|---|---|
| P1 | **Presença digital fraca.** O catálogo fica espalhado em fotos soltas no WhatsApp e no Instagram, sem padrão de preço, descrição ou horário. | O cliente não encontra as informações e desiste da compra ou pergunta tudo de novo a cada atendimento. |
| P2 | **Decisões sem dados.** O dono não sabe com precisão o que mais vende, qual a margem de lucro, o ticket médio, o melhor dia ou o canal mais rentável. | Compra de estoque errada, promoções que não dão resultado, preços sem base. |
| P3 | **Clientes desorganizados.** Os contatos ficam misturados na agenda do celular, sem histórico de compras. | Não se sabe quem comprou várias vezes, quem sumiu ou quem faz aniversário. Perdem-se oportunidades de venda. |
| P4 | **Divulgação sem direcionamento.** A mesma mensagem vai para todos, e ninguém mede se funcionou. | Esforço gasto sem retorno e risco de incomodar clientes. |
| P5 | **Estoque sem controle.** O produto mais pedido acaba sem aviso e outros encalham. | Vendas perdidas e desperdício. |
| P6 | **Ferramentas inadequadas.** Sistemas de gestão costumam exigir internet, cadastro em nuvem, mensalidade e conhecimento técnico. | O empreendedor não adota a ferramenta ou a abandona. |

## 2.3 Como o MeuBalcão resolve

| Problema | Solução do aplicativo | Funcionalidades (ver [item 4](04-caracteristicas.md)) |
|---|---|---|
| P1 Presença digital fraca | Catálogo organizado com foto, preço e categoria, **vitrine digital** com a identidade do negócio e compartilhamento por **WhatsApp e QR Code**. | RF01 a RF08 |
| P2 Decisões sem dados | **Painel** com faturamento, número de vendas, ticket médio e lucro bruto, **meta mensal**, gráficos por dia e por canal, rankings e **exportação CSV**. | RF21 a RF25 |
| P3 Clientes desorganizados | **Mini-CRM** com histórico de compras e segmentos automáticos: recorrentes, inativos, novos e aniversariantes, além de contato rápido por WhatsApp ou telefone. | RF13 a RF16 |
| P4 Divulgação sem direcionamento | **Campanhas** com público segmentado, mensagem pronta para compartilhar e **medição de resultado** (quantos clientes alcançados voltaram a comprar). | RF17 a RF20 |
| P5 Estoque sem controle | Baixa automática de estoque a cada venda, **alerta de estoque mínimo** e devolução ao estoque em caso de cancelamento. | RF09 a RF11 |
| P6 Ferramentas inadequadas | Aplicativo **offline-first**: banco **SQLite no próprio celular**, sem mensalidade, sem servidor e com telas simples, pensadas para o uso rápido atrás do balcão. | RF26 a RF28 e requisitos não funcionais |

## 2.4 Diferenciais da proposta

- **Funciona sem internet:** cadastro, vendas, relatórios e campanhas são preparados localmente. Apenas o envio da mensagem pelo WhatsApp depende de conexão, naturalmente.
- **Os dados ficam com o dono do negócio:** as informações de clientes e vendas permanecem no aparelho, com backup manual sob controle do usuário. Isso simplifica o cuidado com dados pessoais previsto na LGPD, pois não há compartilhamento automático com terceiros.
- **Tudo em um só lugar:** vitrine, vendas, clientes, campanhas e análise de dados integrados pelo mesmo banco, de modo que cada venda alimenta automaticamente os gráficos, os rankings e os segmentos de clientes.
- **Simples e acessível:** poucos toques para vender, sem treinamento prévio e sem custo de assinatura.

## 2.5 Resposta à questão norteadora

> *"Qual aplicativo de celular pode auxiliar no crescimento e inovação dos negócios?"*

Um aplicativo que una **presença digital** (para atrair e se comunicar com clientes) e **inteligência de dados** (para decidir com base em números), de forma leve e utilizável por quem não é especialista, rodando no celular que o empreendedor já tem. O MeuBalcão entrega essa combinação: ele ajuda o negócio a **ser encontrado e lembrado** (vitrine e campanhas) e a **crescer com segurança** (painel, metas e relatórios).

## 2.6 Indicadores de sucesso propostos

Metas de usabilidade e de valor para a validação do protótipo com usuários reais (a confirmar na etapa de testes):

| Indicador | Meta proposta |
|---|---|
| Tempo para registrar uma venda simples | até 30 segundos |
| Tempo para cadastrar um produto com foto | até 1 minuto |
| Telas principais utilizáveis sem internet | 100% |
| Campanha criada e compartilhada | em até 3 minutos |
| Usuário consegue responder "o que mais vendo?" e "quem sumiu?" | em até 2 toques a partir do painel |

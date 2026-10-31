# 3. Público-alvo

## 3.1 Quem são os usuários

### Usuário principal: o(a) dono(a) de pequeno negócio

Pessoas que administram, elas mesmas, um negócio de produtos com venda presencial e/ou pelo WhatsApp e Instagram:

- Microempreendedores individuais (MEI) e autônomos
- Comércios de bairro: confeitarias, padarias, lanchonetes, lojas de roupas e acessórios, papelarias, floriculturas
- Artesãos, produtores caseiros e vendedores de feiras
- Revendedores e vendedores por catálogo

### Usuário secundário: atendentes e familiares

Quem ajuda no balcão e precisa apenas **registrar vendas** e **consultar o catálogo** de forma rápida.

### Beneficiário indireto: o cliente final

Não usa o aplicativo, mas recebe o **catálogo, o QR Code e as promoções** compartilhados pelo negócio e passa a ter uma experiência de atendimento mais organizada e personalizada.

## 3.2 Perfil de uso

| Característica | Descrição |
|---|---|
| Faixa etária típica | Adultos de 20 a 60 anos |
| Familiaridade com tecnologia | Usa bem o WhatsApp e o Instagram, mas tem pouca ou nenhuma experiência com sistemas de gestão |
| Dispositivo | Smartphone Android, muitas vezes de entrada ou intermediário |
| Conexão | Internet móvel ou Wi-Fi instável, com momentos sem sinal |
| Tempo disponível | Pouco. Usa o app entre um atendimento e outro, geralmente em pé |
| Orçamento | Limitado. Prefere ferramentas sem mensalidade |
| Preocupação | Vender mais, não perder clientes e saber se está tendo lucro |

## 3.3 Personas

### Marta, 42 anos: dona de confeitaria

- **Situação:** vende bolos e doces no balcão e por encomenda no WhatsApp. Anota as vendas em um caderno e guarda as fotos dos produtos na galeria do celular.
- **Dores:** não sabe quais produtos dão mais lucro, esquece de avisar clientes antigos sobre novidades e perde tempo reenviando fotos e preços.
- **Objetivos:** ter um catálogo bonito para mandar aos clientes, saber quem não compra há tempo e acompanhar a meta do mês.
- **Como o MeuBalcão ajuda:** vitrine compartilhável, segmento de clientes inativos, campanhas e painel de metas.

### Rafael, 29 anos: vendedor de roupas pelo Instagram

- **Situação:** vende pelo Instagram, WhatsApp e em feiras aos fins de semana. Muitas vezes está em locais sem sinal de internet.
- **Dores:** não controla o estoque de tamanhos e peças, não sabe qual canal traz mais dinheiro e não consegue usar sistemas que dependem de internet.
- **Objetivos:** registrar a venda na hora, mesmo offline, e entender onde investir tempo e dinheiro.
- **Como o MeuBalcão ajuda:** funcionamento 100% offline, controle de estoque com alerta, relatório de vendas por canal e QR Code do catálogo para mostrar nas feiras.

### Dona Lúcia, 58 anos: costureira e artesã

- **Situação:** vende peças feitas à mão para uma rede de clientes fiéis da vizinhança. Tem receio de tecnologia complicada.
- **Dores:** esquece aniversários e encomendas, não separa o que gasta com material do que ganha.
- **Objetivos:** um aplicativo simples, com letras legíveis e poucos botões.
- **Como o MeuBalcão ajuda:** interface com textos grandes, fluxo curto de venda, aniversariantes do mês e cálculo automático de lucro a partir do custo do produto.

## 3.4 Necessidades do público e requisitos derivados

| Necessidade do público | Decisão de projeto |
|---|---|
| Usar sem depender de sinal | Banco de dados SQLite local e todas as funções essenciais offline |
| Pouco tempo para aprender | Telas objetivas, botões grandes, textos claros em português e fluxo de venda em poucos toques |
| Não pagar mensalidade | Sem servidor, sem assinatura. Dados no próprio aparelho |
| Divulgar usando o que já conhece | Integração com WhatsApp e Instagram por compartilhamento, sem exigir novas contas |
| Confiar nos próprios dados | Backup e restauração do banco pelo próprio usuário |
| Entender números sem ser especialista | Painel visual com poucos indicadores e gráficos simples |

## 3.5 Fora do público-alvo (nesta versão)

Empresas com várias lojas, equipes grandes, necessidade de emissão de nota fiscal, integração com sistemas de pagamento ou acesso simultâneo de vários usuários. Essas demandas exigiriam servidor em nuvem e ficam como evolução futura.

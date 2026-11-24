# 12. Vídeo demonstrativo

## 12.1 Link do vídeo

> 🎬 **Vídeo no YouTube (não listado):** <https://www.youtube.com/watch?v=bT8lheNQnls>
>
> Duração: até 2 minutos.

O vídeo mostra a **tela inteira do computador**: o código-fonte no Android Studio, a organização das pastas, as etapas e componentes da construção e o aplicativo funcionando no emulador.

## 12.2 Roteiro (máximo 2 minutos)

Gravar a **tela inteira** do computador, com o Android Studio aberto e o emulador visível ao lado.

| Tempo | O que mostrar na tela | O que falar |
|---|---|---|
| 0:00 – 0:10 | README do repositório no GitHub | "MeuBalcão: vitrine digital e análise de vendas para pequenos negócios, em Kotlin com banco SQLite local." |
| 0:10 – 0:30 | Android Studio: árvore `app/src/main/java/com/meubalcao/app` aberta (pastas `data`, `ui`, `util`) | "O código é organizado em camadas: telas em Compose, repositórios com todo o SQL e a classe AppDatabase, que acessa o banco." |
| 0:30 – 0:50 | Abrir `database/schema.sql` e depois `AppDatabase.kt` (`onCreate` e `onConfigure`) | "O banco tem 9 tabelas relacionadas. Na primeira abertura, o AppDatabase executa este script, que vai dentro do APK, e ativa as chaves estrangeiras." |
| 0:50 – 1:00 | Abrir `VendaRepository.kt` → `registrar` | "A venda grava em três tabelas dentro de uma transação: se faltar estoque, nada é salvo." |
| 1:00 – 1:05 | `app/build.gradle.kts` e o comando `gradlew assembleRelease` (ou *Build → Generate APKs*) | "O APK é gerado pelo Gradle com Android Gradle Plugin, Kotlin e Compose." |
| 1:05 – 1:50 | **Emulador:** abertura → painel → *Carregar dados de exemplo* → Catálogo → **Vender** (adicionar 2 produtos, finalizar) → estoque baixou no catálogo → Clientes (segmentos) → Relatórios | "Painel com faturamento, meta e gráficos; registro de venda com baixa de estoque; clientes segmentados; relatórios por período." |
| 1:50 – 2:00 | **Mais → Sobre o app** | "Na tela Sobre estão os nomes completos dos integrantes do grupo." |

## 12.3 Como publicar no YouTube

1. Gravar a tela inteira (no Windows: **Xbox Game Bar** com `Win + Alt + R` grava só a janela ativa; para a **tela inteira**, use o [OBS Studio](https://obsproject.com) com a fonte "Captura de tela" ou a Ferramenta de Captura do Windows 11, opção *Gravar*).
2. Em [studio.youtube.com](https://studio.youtube.com), clicar em **Criar → Enviar vídeos**.
3. Em **Visibilidade**, escolher **Não listado**.
4. Copiar o link e colar no item 12.1 acima e no [README](../README.md).

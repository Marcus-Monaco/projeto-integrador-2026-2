# 9. Tecnologias, bibliotecas e linguagens

Estas são as tecnologias **efetivamente usadas** na construção do MeuBalcão (Etapa 2).

> **Mudança em relação à Etapa 1:** o planejamento previa Flutter/Dart com bibliotecas do pub.dev. O aplicativo foi implementado em **Kotlin nativo com Jetpack Compose**. A tabela 9.5 mostra o equivalente de cada biblioteca planejada.

## 9.1 Linguagens

| Linguagem | Uso no projeto | Link |
|---|---|---|
| Kotlin | Todo o código do aplicativo | [kotlinlang.org](https://kotlinlang.org) |
| SQL (dialeto SQLite) | Criação do banco, dados de exemplo e consultas | [sqlite.org/lang.html](https://www.sqlite.org/lang.html) |
| Kotlin Script (Gradle KTS) | Configuração do build | [docs.gradle.org/kotlin_dsl](https://docs.gradle.org/current/userguide/kotlin_dsl.html) |
| XML | Manifesto, ícone e tema do Android | [developer.android.com/guide/topics/manifest](https://developer.android.com/guide/topics/manifest/manifest-intro) |
| Markdown | Documentação do repositório | [markdownguide.org](https://www.markdownguide.org) |

## 9.2 Plataforma, interface e banco de dados

| Tecnologia | Uso no projeto | Link |
|---|---|---|
| Android SDK (API 26 a 36) | Plataforma do aplicativo | [developer.android.com](https://developer.android.com) |
| Jetpack Compose | Construção das telas (interface declarativa) | [developer.android.com/compose](https://developer.android.com/compose) |
| Material Design 3 | Componentes visuais (botões, cartões, campos, barra inferior) | [m3.material.io](https://m3.material.io) |
| SQLite (nativo do Android) | Banco de dados local `meubalcao.db` | [sqlite.org](https://www.sqlite.org) |
| `android.database.sqlite` (`SQLiteOpenHelper`) | Abertura, criação, versão e transações do banco | [developer.android.com/…/SQLiteOpenHelper](https://developer.android.com/reference/android/database/sqlite/SQLiteOpenHelper) |

## 9.3 Bibliotecas do aplicativo

| Biblioteca | Uso no projeto | Link |
|---|---|---|
| `androidx.compose` (BOM 2025.06.01) | Interface, gráficos desenhados com `Canvas` | [developer.android.com/jetpack/compose/bom](https://developer.android.com/develop/ui/compose/bom) |
| `androidx.compose.material3` | Componentes Material 3 | [developer.android.com/jetpack/androidx/releases/compose-material3](https://developer.android.com/jetpack/androidx/releases/compose-material3) |
| `androidx.compose.material:material-icons-extended` | Ícones das telas | [developer.android.com/…/material-icons](https://developer.android.com/reference/kotlin/androidx/compose/material/icons/package-summary) |
| `androidx.navigation:navigation-compose` | Navegação entre as telas e barra inferior | [developer.android.com/guide/navigation](https://developer.android.com/guide/navigation) |
| `androidx.activity:activity-compose` | Activity principal, seletor de fotos e escolha de arquivos (backup) | [developer.android.com/jetpack/androidx/releases/activity](https://developer.android.com/jetpack/androidx/releases/activity) |
| `androidx.lifecycle:lifecycle-runtime-compose` | Ciclo de vida das telas | [developer.android.com/jetpack/androidx/releases/lifecycle](https://developer.android.com/jetpack/androidx/releases/lifecycle) |
| `androidx.core:core-ktx` | `FileProvider` para compartilhar imagens e CSV | [developer.android.com/jetpack/androidx/releases/core](https://developer.android.com/jetpack/androidx/releases/core) |
| `kotlinx.coroutines` | Consultas ao banco fora da tela principal (`Dispatchers.IO`) | [kotlinlang.org/docs/coroutines-overview](https://kotlinlang.org/docs/coroutines-overview.html) |
| ZXing `core` 3.5.3 | Geração do QR Code, sem internet | [github.com/zxing/zxing](https://github.com/zxing/zxing) |

## 9.4 Bibliotecas de testes

| Biblioteca | Uso no projeto | Link |
|---|---|---|
| JUnit 4 | Estrutura dos testes automatizados | [junit.org/junit4](https://junit.org/junit4/) |
| Robolectric 4.14 | Executa o **SQLite real do Android** no computador, sem celular | [robolectric.org](https://robolectric.org) |
| AndroidX Test Core | Contexto do aplicativo nos testes | [developer.android.com/training/testing](https://developer.android.com/training/testing) |

## 9.5 Planejado (Etapa 1) × utilizado (Etapa 2)

| Função | Planejado (Flutter) | Utilizado (Kotlin) |
|---|---|---|
| Banco SQLite | `sqflite` | `SQLiteOpenHelper` nativo do Android |
| Estado das telas | `provider` | Estado do Compose + coroutines + observação de mudanças (`StateFlow`) |
| Gráficos | `fl_chart` | Gráficos desenhados com `Canvas` do Compose |
| Moeda e datas pt-BR | `intl` | `java.text.NumberFormat` e `java.time` |
| Fotos de produtos e logo | `image_picker` | Seletor de fotos do Android (`PickVisualMedia`) |
| Compartilhar | `share_plus` | `Intent.ACTION_SEND` + `FileProvider` |
| WhatsApp e ligação | `url_launcher` | `Intent.ACTION_VIEW` (`wa.me`) e `Intent.ACTION_DIAL` |
| QR Code | `qr_flutter` | ZXing |
| CSV | `csv` | Gerador próprio (`util/Csv.kt`), separador `;` |
| Escolher backup | `file_picker` | `OpenDocument` / `CreateDocument` do Android |
| Versão na tela Sobre | `package_info_plus` | `BuildConfig.VERSION_NAME` |
| Testes do banco | `sqflite_common_ffi` | Robolectric |

## 9.6 Ferramentas

| Ferramenta | Uso no projeto | Link |
|---|---|---|
| Android Studio | Editor, Android SDK, emulador e build | [developer.android.com/studio](https://developer.android.com/studio) |
| Gradle 8.14 (Wrapper) + Android Gradle Plugin 8.12 | Compilação e geração do APK | [gradle.org](https://gradle.org) |
| R8 | Redução e otimização do APK de release | [developer.android.com/build/shrink-code](https://developer.android.com/build/shrink-code) |
| Android Emulator | Testes e gravação do vídeo | [developer.android.com/studio/run/emulator](https://developer.android.com/studio/run/emulator) |
| Android Debug Bridge (adb) | Instalar o APK e capturar telas | [developer.android.com/tools/adb](https://developer.android.com/tools/adb) |
| Git e GitHub | Controle de versão e entrega | [github.com](https://github.com) |
| DB Browser for SQLite | Conferir o banco exportado pelo backup | [sqlitebrowser.org](https://sqlitebrowser.org) |
| Mermaid e Graphviz | Diagramas da documentação e do modelo ER | [mermaid.js.org](https://mermaid.js.org) |
| HTML e CSS | Código-fonte dos wireframes | [developer.mozilla.org/docs/Web/HTML](https://developer.mozilla.org/docs/Web/HTML) |

#!/usr/bin/env bash
# =============================================================================
#  MeuBalcão - configura o ambiente e inicia o aplicativo
#
#  Funciona no Windows (Git Bash), Linux e macOS. Execute na raiz do repositório:
#
#      bash configurar-e-rodar.sh              # configura, testa, compila e abre no emulador
#      bash configurar-e-rodar.sh --sem-testes # pula os testes automatizados
#      bash configurar-e-rodar.sh --apk        # só gera o APK de release (sem emulador)
#      bash configurar-e-rodar.sh --ajuda
#
#  O que o script faz:
#    1. Confere Git e Java (JDK 17+). Sem JDK, baixa o JDK 21 (Temurin) só para o projeto.
#    2. Encontra o Android SDK (Android Studio). Sem SDK, instala o mínimo necessário.
#    3. Cria o local.properties com o caminho do SDK.
#    4. Roda os testes automatizados e compila o app com o Gradle Wrapper.
#    5. Abre um emulador (ou usa um celular conectado), instala e inicia o MeuBalcão.
#
#  Nada é instalado fora da pasta do usuário: JDK e SDK baixados ficam em ~/.meubalcao
#  e no local padrão do Android SDK.
# =============================================================================
set -euo pipefail

# ----------------------------------------------------------------- configurações
APP_ID="com.meubalcao.app"
AVD_NOME="MeuBalcao_API_36"
API_ANDROID="36"
BUILD_TOOLS="36.0.0"
# Versão das "command-line tools" do Android (https://developer.android.com/studio#command-tools)
CMDLINE_TOOLS_VERSAO="${CMDLINE_TOOLS_VERSAO:-13114758}"
PASTA_FERRAMENTAS="$HOME/.meubalcao"

RODAR_TESTES=1
SOMENTE_APK=0
for arg in "$@"; do
  case "$arg" in
    --sem-testes) RODAR_TESTES=0 ;;
    --apk)        SOMENTE_APK=1 ;;
    -h|--ajuda|--help) sed -n '2,22p' "$0" | sed 's/^# \{0,1\}//'; exit 0 ;;
    *) echo "Opção desconhecida: $arg (use --ajuda)"; exit 1 ;;
  esac
done

# ----------------------------------------------------------------- mensagens
if [ -t 1 ]; then VERDE=$'\e[32m'; AMARELO=$'\e[33m'; VERMELHO=$'\e[31m'; AZUL=$'\e[36m'; NEGRITO=$'\e[1m'; FIM=$'\e[0m'
else VERDE=""; AMARELO=""; VERMELHO=""; AZUL=""; NEGRITO=""; FIM=""; fi
etapa() { echo; echo "${AZUL}${NEGRITO}==> $*${FIM}"; }
ok()    { echo "${VERDE}  ✔ $*${FIM}"; }
aviso() { echo "${AMARELO}  ! $*${FIM}"; }
erro()  { echo "${VERMELHO}  ✖ $*${FIM}" >&2; exit 1; }

# ----------------------------------------------------------------- sistema operacional
case "$(uname -s)" in
  MINGW*|MSYS*|CYGWIN*) SO="windows" ;;
  Darwin*)              SO="mac" ;;
  Linux*)               SO="linux" ;;
  *) erro "Sistema operacional não suportado: $(uname -s)" ;;
esac
case "$(uname -m)" in
  arm64|aarch64) ARQ="arm64" ;;
  *)             ARQ="x64" ;;
esac
EXE=""; BAT=""
if [ "$SO" = "windows" ]; then EXE=".exe"; BAT=".bat"; fi

# Converte caminhos do Windows (C:\...) para o formato do Git Bash (/c/...)
caminho_unix() { if [ "$SO" = "windows" ] && [ -n "${1:-}" ]; then cygpath -u "$1"; else echo "${1:-}"; fi; }
# Caminho no formato aceito pelo local.properties (C:/... no Windows)
caminho_misto() { if [ "$SO" = "windows" ]; then cygpath -m "$1"; else echo "$1"; fi; }

baixar() { # baixar URL DESTINO
  if command -v curl >/dev/null 2>&1; then curl -fL --retry 3 -o "$2" "$1"
  elif command -v wget >/dev/null 2>&1; then wget -O "$2" "$1"
  else erro "Instale o curl ou o wget para baixar as ferramentas."; fi
}

extrair_zip() { # extrair_zip ARQUIVO DESTINO
  mkdir -p "$2"
  if command -v unzip >/dev/null 2>&1; then unzip -q -o "$1" -d "$2"
  elif [ "$SO" = "windows" ]; then
    powershell.exe -NoProfile -Command "Expand-Archive -Force -Path '$(cygpath -w "$1")' -DestinationPath '$(cygpath -w "$2")'"
  else erro "Instale o unzip para continuar."; fi
}

cd "$(dirname "$0")"
[ -f "settings.gradle.kts" ] && [ -d "app" ] || erro "Execute o script na raiz do repositório do MeuBalcão."

echo "${NEGRITO}MeuBalcão - configuração automática${FIM} (${SO}/${ARQ})"

# =============================================================================
etapa "1/5 Conferindo Git e Java"
# =============================================================================
command -v git >/dev/null 2>&1 && ok "Git: $(git --version)" || aviso "Git não encontrado (só é necessário para clonar/atualizar o repositório)."

versao_java() { # imprime a versão principal do java informado (ex.: 21)
  "$1" -version 2>&1 | head -n1 | sed -E 's/.*version "([0-9]+)(\.[0-9]+)?.*/\1/; s/^1$/8/'
}

java_valido() { [ -x "$1/bin/java$EXE" ] && [ "$(versao_java "$1/bin/java$EXE")" -ge 17 ] 2>/dev/null; }

encontrar_java() {
  local candidatos=()
  [ -n "${JAVA_HOME:-}" ] && candidatos+=("$(caminho_unix "$JAVA_HOME")")
  case "$SO" in
    windows) candidatos+=("/c/Program Files/Android/Android Studio/jbr" "$(caminho_unix "${LOCALAPPDATA:-}")/Programs/Android Studio/jbr") ;;
    mac)     candidatos+=("/Applications/Android Studio.app/Contents/jbr/Contents/Home") ;;
    linux)   candidatos+=("/opt/android-studio/jbr" "$HOME/android-studio/jbr" "/snap/android-studio/current/jbr") ;;
  esac
  candidatos+=("$PASTA_FERRAMENTAS/jdk")
  if command -v java >/dev/null 2>&1; then
    local j; j="$(command -v java)"; j="$(cd "$(dirname "$j")/.." && pwd)"; candidatos+=("$j")
  fi
  for c in "${candidatos[@]}"; do
    if [ -n "$c" ] && java_valido "$c"; then echo "$c"; return 0; fi
  done
  return 1
}

if JAVA_ENCONTRADO="$(encontrar_java)"; then
  export JAVA_HOME="$JAVA_ENCONTRADO"
else
  aviso "Nenhum JDK 17+ encontrado. Baixando o JDK 21 (Eclipse Temurin) para $PASTA_FERRAMENTAS/jdk ..."
  case "$SO" in windows) so_api="windows"; ext="zip" ;; mac) so_api="mac"; ext="tar.gz" ;; linux) so_api="linux"; ext="tar.gz" ;; esac
  arq_api="x64"; [ "$ARQ" = "arm64" ] && arq_api="aarch64"
  mkdir -p "$PASTA_FERRAMENTAS"; tmp="$PASTA_FERRAMENTAS/jdk-download.$ext"
  baixar "https://api.adoptium.net/v3/binary/latest/21/ga/$so_api/$arq_api/jdk/hotspot/normal/eclipse" "$tmp"
  rm -rf "$PASTA_FERRAMENTAS/jdk-extraido"; mkdir -p "$PASTA_FERRAMENTAS/jdk-extraido"
  if [ "$ext" = "zip" ]; then extrair_zip "$tmp" "$PASTA_FERRAMENTAS/jdk-extraido"
  else tar -xzf "$tmp" -C "$PASTA_FERRAMENTAS/jdk-extraido"; fi
  pasta="$(find "$PASTA_FERRAMENTAS/jdk-extraido" -maxdepth 4 -type f -name "java$EXE" -path "*/bin/*" | head -n1)"
  [ -n "$pasta" ] || erro "Não foi possível extrair o JDK."
  rm -rf "$PASTA_FERRAMENTAS/jdk"; mv "$(dirname "$(dirname "$pasta")")" "$PASTA_FERRAMENTAS/jdk"
  rm -rf "$PASTA_FERRAMENTAS/jdk-extraido" "$tmp"
  export JAVA_HOME="$PASTA_FERRAMENTAS/jdk"
fi
export PATH="$JAVA_HOME/bin:$PATH"
ok "Java $(versao_java "$JAVA_HOME/bin/java$EXE"): $JAVA_HOME"

# =============================================================================
etapa "2/5 Conferindo o Android SDK"
# =============================================================================
encontrar_sdk() {
  local candidatos=()
  [ -n "${ANDROID_HOME:-}" ] && candidatos+=("$(caminho_unix "$ANDROID_HOME")")
  [ -n "${ANDROID_SDK_ROOT:-}" ] && candidatos+=("$(caminho_unix "$ANDROID_SDK_ROOT")")
  if [ -f local.properties ]; then
    local d; d="$(grep -E '^sdk.dir=' local.properties | cut -d= -f2- | sed 's/\\\\/\\/g; s/\\:/:/g' || true)"
    [ -n "$d" ] && candidatos+=("$(caminho_unix "$d")")
  fi
  case "$SO" in
    windows) candidatos+=("$(caminho_unix "${LOCALAPPDATA:-}")/Android/Sdk") ;;
    mac)     candidatos+=("$HOME/Library/Android/sdk") ;;
    linux)   candidatos+=("$HOME/Android/Sdk") ;;
  esac
  for c in "${candidatos[@]}"; do
    if [ -n "$c" ] && [ -d "$c/platforms" -o -d "$c/cmdline-tools" -o -d "$c/platform-tools" ]; then echo "$c"; return 0; fi
  done
  return 1
}

if ! ANDROID_SDK="$(encontrar_sdk)"; then
  case "$SO" in
    windows) ANDROID_SDK="$(caminho_unix "${LOCALAPPDATA}")/Android/Sdk" ;;
    mac)     ANDROID_SDK="$HOME/Library/Android/sdk" ;;
    linux)   ANDROID_SDK="$HOME/Android/Sdk" ;;
  esac
  aviso "Android SDK não encontrado. Ele será instalado em: $ANDROID_SDK"
fi
export ANDROID_HOME="$ANDROID_SDK" ANDROID_SDK_ROOT="$ANDROID_SDK"

# Ferramentas de linha de comando (sdkmanager e avdmanager): baixadas só se forem necessárias
SDKMANAGER=""; AVDMANAGER=""
garantir_sdkmanager() {
  [ -n "$SDKMANAGER" ] && return 0
  SDKMANAGER="$(ls "$ANDROID_SDK"/cmdline-tools/*/bin/sdkmanager$BAT 2>/dev/null | sort -r | head -n1 || true)"
  if [ -z "$SDKMANAGER" ]; then
    aviso "Baixando as Android command-line tools ($CMDLINE_TOOLS_VERSAO)..."
    case "$SO" in windows) plat="win" ;; mac) plat="mac" ;; linux) plat="linux" ;; esac
    mkdir -p "$ANDROID_SDK/cmdline-tools"; local tmp="$ANDROID_SDK/cmdline-tools/tools.zip"
    baixar "https://dl.google.com/android/repository/commandlinetools-${plat}-${CMDLINE_TOOLS_VERSAO}_latest.zip" "$tmp"
    rm -rf "$ANDROID_SDK/cmdline-tools/latest" "$ANDROID_SDK/cmdline-tools/cmdline-tools"
    extrair_zip "$tmp" "$ANDROID_SDK/cmdline-tools"
    mv "$ANDROID_SDK/cmdline-tools/cmdline-tools" "$ANDROID_SDK/cmdline-tools/latest"
    rm -f "$tmp"
    SDKMANAGER="$ANDROID_SDK/cmdline-tools/latest/bin/sdkmanager$BAT"
  fi
  AVDMANAGER="$(dirname "$SDKMANAGER")/avdmanager$BAT"
}

# Pacotes mínimos para compilar e rodar no emulador
IMAGEM_ABI="x86_64"; [ "$ARQ" = "arm64" ] && IMAGEM_ABI="arm64-v8a"
IMAGEM="system-images;android-${API_ANDROID};google_apis;${IMAGEM_ABI}"
pacotes=()
[ -d "$ANDROID_SDK/platform-tools" ]                 || pacotes+=("platform-tools")
[ -d "$ANDROID_SDK/platforms/android-${API_ANDROID}" ] || pacotes+=("platforms;android-${API_ANDROID}")
ls -d "$ANDROID_SDK"/build-tools/3[5-9]* >/dev/null 2>&1 || pacotes+=("build-tools;${BUILD_TOOLS}")
if [ "$SOMENTE_APK" = "0" ]; then
  [ -d "$ANDROID_SDK/emulator" ] || pacotes+=("emulator")
  ls -d "$ANDROID_SDK"/system-images/android-${API_ANDROID}*/*/"$IMAGEM_ABI" >/dev/null 2>&1 || pacotes+=("$IMAGEM")
fi

if [ "${#pacotes[@]}" -gt 0 ]; then
  aviso "Instalando pacotes do SDK: ${pacotes[*]}"
  aviso "(o download pode levar alguns minutos)"
  garantir_sdkmanager
  yes | "$SDKMANAGER" --sdk_root="$(caminho_misto "$ANDROID_SDK")" --licenses >/dev/null 2>&1 || true
  yes | "$SDKMANAGER" --sdk_root="$(caminho_misto "$ANDROID_SDK")" "${pacotes[@]}" | grep -vE '^\[=*' || true
fi
ok "Android SDK: $ANDROID_SDK"

ADB="$ANDROID_SDK/platform-tools/adb$EXE"
EMULADOR="$ANDROID_SDK/emulator/emulator$EXE"

# =============================================================================
etapa "3/5 Configurando o projeto"
# =============================================================================
echo "sdk.dir=$(caminho_misto "$ANDROID_SDK")" > local.properties
ok "local.properties criado"
chmod +x gradlew 2>/dev/null || true

# =============================================================================
etapa "4/5 Testando e compilando (Gradle)"
# =============================================================================
if [ "$RODAR_TESTES" = "1" ]; then
  ./gradlew --console=plain testDebugUnitTest
  ok "Testes automatizados passaram"
else
  aviso "Testes pulados (--sem-testes)"
fi

if [ "$SOMENTE_APK" = "1" ]; then
  ./gradlew --console=plain assembleRelease
  ok "APK gerado: app/build/outputs/apk/release/app-release.apk"
  exit 0
fi
./gradlew --console=plain assembleDebug
APK="app/build/outputs/apk/debug/app-debug.apk"
ok "App compilado: $APK"

# =============================================================================
etapa "5/5 Abrindo o MeuBalcão"
# =============================================================================
"$ADB" start-server >/dev/null 2>&1 || true
dispositivo_pronto() { "$ADB" devices | awk 'NR>1 && $2=="device"' | grep -q .; }

if dispositivo_pronto; then
  ok "Usando o aparelho/emulador já conectado"
else
  AVD="$("$EMULADOR" -list-avds 2>/dev/null | tr -d '\r' | head -n1 || true)"
  if [ -z "$AVD" ]; then
    aviso "Nenhum emulador encontrado. Criando \"$AVD_NOME\"..."
    imagem_existente="$(ls -d "$ANDROID_SDK"/system-images/android-${API_ANDROID}*/*/"$IMAGEM_ABI" 2>/dev/null | head -n1)"
    [ -n "$imagem_existente" ] || erro "Imagem do sistema não encontrada em $ANDROID_SDK/system-images."
    rel="${imagem_existente#"$ANDROID_SDK"/}"; pacote_imagem="$(echo "$rel" | tr '/' ';')"
    garantir_sdkmanager
    echo "no" | "$AVDMANAGER" create avd -n "$AVD_NOME" -k "$pacote_imagem" -d pixel_6 --force >/dev/null
    AVD="$AVD_NOME"
  fi
  ok "Iniciando o emulador \"$AVD\" (a primeira inicialização pode levar alguns minutos)..."
  nohup "$EMULADOR" -avd "$AVD" -no-boot-anim -no-snapshot-save >/dev/null 2>&1 &
  "$ADB" wait-for-device
  printf "  aguardando o Android iniciar"
  for _ in $(seq 1 120); do
    [ "$("$ADB" shell getprop sys.boot_completed 2>/dev/null | tr -d '\r')" = "1" ] && break
    printf "."; sleep 3
  done
  echo
  [ "$("$ADB" shell getprop sys.boot_completed 2>/dev/null | tr -d '\r')" = "1" ] \
    || erro "O emulador não terminou de iniciar. Abra-o pelo Android Studio (Device Manager) e rode o script de novo."
  "$ADB" shell input keyevent KEYCODE_WAKEUP >/dev/null 2>&1 || true
  "$ADB" shell wm dismiss-keyguard >/dev/null 2>&1 || true
fi

"$ADB" install -r "$APK" >/dev/null
ok "App instalado"
"$ADB" shell am start -n "$APP_ID/.MainActivity" >/dev/null
ok "MeuBalcão aberto! No painel, toque em \"Carregar dados de exemplo\" para ver o app com dados."

echo
echo "${VERDE}${NEGRITO}Tudo pronto.${FIM} Para rodar de novo depois: bash configurar-e-rodar.sh --sem-testes"

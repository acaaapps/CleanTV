# Script de compilacion e instalacion del launcher para Nilait Prisma NI-50UB7001S
# Compila con Gradle, instala el APK, configura Home por defecto, activa HomeKeyService y sincroniza recursos.

param(
    [string]$TvHost = $env:TV_HOST,
    [switch]$SkipBuild
)

if (-not $TvHost) { $TvHost = "192.168.1.18" }

$ErrorActionPreference = 'Stop'
$OutputEncoding = New-Object System.Text.UTF8Encoding $false

Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host " Compilacion e Instalacion del Launcher Limpio" -ForegroundColor Cyan
Write-Host " Destino: $TvHost" -ForegroundColor Cyan
Write-Host "==========================================================" -ForegroundColor Cyan

# 1. Configurar entorno de desarrollo local
if (Test-Path "$PSScriptRoot\tools\jdk17") {
    $env:JAVA_HOME = "$PSScriptRoot\tools\jdk17"
    $env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
}
if (Test-Path "$PSScriptRoot\tools\android-sdk") {
    $env:ANDROID_HOME = "$PSScriptRoot\tools\android-sdk"
    $env:PATH = "$env:ANDROID_HOME\platform-tools;$env:PATH"
}

$adb = "$PSScriptRoot\tools\android-sdk\platform-tools\adb.exe"
if (-not (Test-Path $adb)) { $adb = "adb" }

# 2. Compilar APK si no se omite
if (-not $SkipBuild) {
    Write-Host "`n[1/6] Compilando APK con Gradle (Release)..." -ForegroundColor Yellow
    Push-Location "$PSScriptRoot\launcher"
    try {
        .\gradlew.bat assembleRelease --no-daemon --console=plain
        if ($LASTEXITCODE -ne 0) { throw "Error al compilar el proyecto Gradle." }
    } finally {
        Pop-Location
    }
} else {
    Write-Host "`n[1/6] Omitiendo compilacion (se usa el APK existente)..." -ForegroundColor Yellow
}

$apkPath = "$PSScriptRoot\launcher\app\build\outputs\apk\release\app-release.apk"
if (-not (Test-Path $apkPath)) {
    throw "No se encontro el archivo APK en $apkPath. Asegurate de compilar primero."
}

# 3. Conectar al televisor
Write-Host "`n[2/6] Conectando con el televisor (${TvHost}:5555)..." -ForegroundColor Yellow
& $adb connect "${TvHost}:5555"

# 4. Instalar APK
Write-Host "`n[3/6] Instalando APK en el televisor..." -ForegroundColor Yellow
& $adb -s "${TvHost}:5555" install -r "$apkPath"

# 5. Configurar como Home y desactivar launcher stock
Write-Host "`n[4/6] Configurando el launcher como pantalla de inicio predeterminada..." -ForegroundColor Yellow
& $adb -s "${TvHost}:5555" shell pm disable-user --user 0 com.google.android.tvlauncher
& $adb -s "${TvHost}:5555" shell cmd package set-home-activity com.gathin.tvhome/.MainActivity

# 6. Activar servicio de accesibilidad HomeKeyService
Write-Host "`n[5/6] Activando servicio de control del boton Home..." -ForegroundColor Yellow
& $adb -s "${TvHost}:5555" shell settings put secure enabled_accessibility_services com.gathin.tvhome/.HomeKeyService
& $adb -s "${TvHost}:5555" shell settings put secure accessibility_enabled 1

# 7. Configurar servicio de Salvapantallas del sistema
& $adb -s "${TvHost}:5555" shell settings put secure screensaver_components com.gathin.tvhome/.ClockDream
& $adb -s "${TvHost}:5555" shell settings put secure screensaver_default_component com.gathin.tvhome/.ClockDream
& $adb -s "${TvHost}:5555" shell settings put secure screensaver_enabled 1

# 8. Sincronizar datos (Frases y Salvapantallas)
Write-Host "`n[6/6] Sincronizando datos multimedia en el televisor..." -ForegroundColor Yellow
if (Test-Path "$PSScriptRoot\work\quotes.json") {
    Write-Host " -> Sincronizando frases y poemas (quotes.json)..."
    & $adb -s "${TvHost}:5555" push "$PSScriptRoot\work\quotes.json" /sdcard/Android/data/com.gathin.tvhome/files/quotes.json
}

if (Test-Path "$PSScriptRoot\work\screensaver") {
    Write-Host " -> Sincronizando imagenes del salvapantallas (Naturaleza y Arte)..."
    & $adb -s "${TvHost}:5555" shell "mkdir -p /sdcard/Android/data/com.gathin.tvhome/files/screensaver/nature /sdcard/Android/data/com.gathin.tvhome/files/screensaver/art"
    if (Test-Path "$PSScriptRoot\work\screensaver\nature") {
        & $adb -s "${TvHost}:5555" push "$PSScriptRoot\work\screensaver\nature\." /sdcard/Android/data/com.gathin.tvhome/files/screensaver/nature/
    }
    if (Test-Path "$PSScriptRoot\work\screensaver\art") {
        & $adb -s "${TvHost}:5555" push "$PSScriptRoot\work\screensaver\art\." /sdcard/Android/data/com.gathin.tvhome/files/screensaver/art/
    }
}

Write-Host "`n==========================================================" -ForegroundColor Green
Write-Host " ¡Instalacion completada con exito!" -ForegroundColor Green
Write-Host " El nuevo launcher esta activo y listo en tu televisor." -ForegroundColor Green
Write-Host "==========================================================" -ForegroundColor Green

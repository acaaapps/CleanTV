# Script de restauración completa para Nilait Prisma NI-50UB7001S (Android TV 14)
# Reactiva los paquetes del fabricante y restablece el launcher y salvapantallas original de Google TV.

param(
    [string]$TvHost = $env:TV_HOST
)

if (-not $TvHost) { $TvHost = "192.168.1.18" }

$ErrorActionPreference = 'Continue'
$OutputEncoding = New-Object System.Text.UTF8Encoding $false

Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host " Restauracion de fabrica - Nilait Prisma NI-50UB7001S" -ForegroundColor Cyan
Write-Host " Host objetivo: $TvHost" -ForegroundColor Cyan
Write-Host "==========================================================" -ForegroundColor Cyan

$adb = "$PSScriptRoot\tools\android-sdk\platform-tools\adb.exe"
if (-not (Test-Path $adb)) {
    $adb = "adb"
}

Write-Host "`n[1/5] Conectando con el televisor (${TvHost}:5555)..." -ForegroundColor Yellow
& $adb connect "${TvHost}:5555"

$packages = @(
    "com.vestel.vestelreporter",
    "com.vestel.smartlogger",
    "com.vestel.tv.speedup",
    "com.vestel.smartcenter",
    "android.autoinstalls.config.vestel.malin",
    "com.android.adservices.api",
    "com.android.ondevicepersonalization.services",
    "com.android.federatedcompute.services",
    "com.google.android.partnersetup",
    "com.google.android.feedback",
    "com.android.tv.feedbackconsent",
    "com.android.healthconnect.controller",
    "com.android.health.connect.backuprestore",
    "com.android.managedprovisioning",
    "com.android.dynsystem",
    "com.android.cts.ctsshim",
    "com.android.cts.priv.ctsshim",
    "com.google.android.syncadapters.calendar",
    "com.android.htmlviewer",
    "com.android.backupconfirm",
    "com.android.se",
    "com.disney.disneyplus",
    "org.videolan.vlc",
    "com.google.android.tvrecommendations",
    "com.google.android.tvlauncher"
)

Write-Host "`n[2/5] Reactivando paquetes desactivados..." -ForegroundColor Yellow
foreach ($pkg in $packages) {
    Write-Host " -> Reactivando: $pkg"
    & $adb -s "${TvHost}:5555" shell pm enable $pkg
}

Write-Host "`n[3/5] Restableciendo el launcher de stock (Google TV Launcher)..." -ForegroundColor Yellow
& $adb -s "${TvHost}:5555" shell cmd package set-home-activity com.google.android.tvlauncher/.MainActivity

Write-Host "`n[4/5] Restableciendo salvapantallas oficial de Google TV..." -ForegroundColor Yellow
& $adb -s "${TvHost}:5555" shell settings put secure screensaver_components com.google.android.backdrop/.Backdrop
& $adb -s "${TvHost}:5555" shell settings put secure screensaver_default_component com.google.android.backdrop/.Backdrop
& $adb -s "${TvHost}:5555" shell settings put secure screensaver_enabled 1

Write-Host "`n[5/5] Desactivando servicios de accesibilidad personalizados..." -ForegroundColor Yellow
& $adb -s "${TvHost}:5555" shell settings put secure enabled_accessibility_services '""'
& $adb -s "${TvHost}:5555" shell settings put secure accessibility_enabled 0

Write-Host "`n==========================================================" -ForegroundColor Green
Write-Host " ¡Restauracion completada con exito!" -ForegroundColor Green
Write-Host " El televisor ha recuperado su configuracion de fabrica." -ForegroundColor Green
Write-Host "==========================================================" -ForegroundColor Green

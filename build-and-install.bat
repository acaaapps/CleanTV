@echo off
chcp 65001 >nul
echo Iniciando compilacion e instalacion del launcher...
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0build-and-install.ps1" %*
echo.
pause

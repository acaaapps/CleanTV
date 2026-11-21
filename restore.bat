@echo off
chcp 65001 >nul
echo Iniciando restauracion del televisor...
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0restore.ps1" %*
echo.
pause

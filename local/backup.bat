@echo off
rem Salva uma copia dos seus dados na pasta backups. Guarde tambem uma copia fora do computador.
cd /d "%~dp0.."
if not exist backups mkdir backups
for /f %%i in ('powershell -NoProfile -Command "Get-Date -Format yyyy-MM-dd-HHmm"') do set TS=%%i
set ARQ=backups\planilha-%TS%.sql
docker compose -f docker-compose.local.yml --env-file .env.local exec -T db pg_dump -U planilha planilha > "%ARQ%"
if errorlevel 1 goto erro
for %%f in ("%ARQ%") do if %%~zf==0 goto erro
echo Backup salvo em %ARQ%
pause
exit /b 0
:erro
echo O backup falhou. O sistema esta ligado?
del "%ARQ%" 2>nul
pause
exit /b 1

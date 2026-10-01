@echo off
rem Baixa a versao mais nova do codigo e refaz o sistema. Os dados sao mantidos.
cd /d "%~dp0.."
git pull
if errorlevel 1 goto erro
cd ..\Sistema-Planilha-Front
git pull
if errorlevel 1 goto erro
cd ..\Sistema-Planilha-Back
docker compose -f docker-compose.local.yml --env-file .env.local up -d --build
if errorlevel 1 goto erro
echo Atualizado. Abra o sistema de novo no navegador.
pause
exit /b 0
:erro
echo Algo deu errado. Veja a mensagem acima.
pause
exit /b 1

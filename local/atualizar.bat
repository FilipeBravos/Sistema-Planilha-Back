@echo off
rem Baixa a versao mais nova do codigo e refaz o sistema. Os dados sao mantidos.
cd /d "%~dp0.."
git pull
if errorlevel 1 goto erro
cd ..\Sistema-Planilha-Front
git pull
if errorlevel 1 goto erro
cd ..\Sistema-Planilha-Back
call local\_rede.bat
if "%TS_ERRO%"=="1" goto semtailscale
docker compose %COMPOSE_ARQ% --env-file .env.local up -d --build
if errorlevel 1 goto erro
echo Atualizado. Abra o sistema de novo no navegador.
pause
exit /b 0
:semtailscale
echo ACESSO_REDE=tailscale, mas nao encontrei o Tailscale conectado neste computador.
echo Abra o Tailscale, conecte-se e rode de novo.
pause
exit /b 1
:erro
echo Algo deu errado. Veja a mensagem acima.
pause
exit /b 1

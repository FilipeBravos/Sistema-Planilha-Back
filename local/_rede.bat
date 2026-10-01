@echo off
rem Usado por iniciar.bat e atualizar.bat: traduz o .env.local para o que o compose precisa.
set ENDERECO_REDE=127.0.0.1
set ACESSO_REDE_ATIVO=0
set COMPOSE_ARQ=-f docker-compose.local.yml
set TS_ERRO=0
set PORTA=8080
set ACESSO=nao
for /f "tokens=1,* delims==" %%a in ('findstr /b /c:"PORTA=" .env.local') do set PORTA=%%b
for /f "tokens=1,* delims==" %%a in ('findstr /b /c:"ACESSO_REDE=" .env.local') do set ACESSO=%%b
if /i "%ACESSO%"=="sim" goto rede
if /i "%ACESSO%"=="tailscale" goto tailscale
exit /b 0

:rede
set ENDERECO_REDE=0.0.0.0
set ACESSO_REDE_ATIVO=1
exit /b 0

rem ACESSO_REDE=tailscale: publica a porta so no endereco Tailscale (100.x.y.z) deste computador.
rem Se o Tailscale nao estiver instalado ou conectado, PARA em vez de abrir o sistema para outras redes.
:tailscale
set TS_IP=
where tailscale >nul 2>nul
if errorlevel 1 set "PATH=%PATH%;%ProgramFiles%\Tailscale"
for /f "delims=" %%i in ('tailscale ip -4 2^>nul') do if not defined TS_IP set TS_IP=%%i
if not defined TS_IP goto semtailscale
echo %TS_IP%| findstr /b "100." >nul
if errorlevel 1 goto semtailscale
set ENDERECO_TAILSCALE=%TS_IP%
set COMPOSE_ARQ=-f docker-compose.local.yml -f docker-compose.tailscale.yml
set ACESSO_REDE_ATIVO=2
exit /b 0

:semtailscale
set TS_ERRO=1
exit /b 1

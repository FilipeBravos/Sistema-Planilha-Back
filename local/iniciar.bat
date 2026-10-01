@echo off
rem Liga o sistema na sua maquina. A 1a vez demora uns minutos para montar tudo.
cd /d "%~dp0.."
where docker >nul 2>nul
if errorlevel 1 goto semdocker
docker info >nul 2>nul
if errorlevel 1 goto dockerparado
if not exist "..\Sistema-Planilha-Front" goto semfront
if not exist .env.local goto criaenv
findstr /c:"troque" .env.local >nul
if not errorlevel 1 goto editaenv

docker compose -f docker-compose.local.yml --env-file .env.local up -d
if errorlevel 1 goto erro

set PORTA=8080
for /f "tokens=2 delims==" %%a in ('findstr /b "PORTA=" .env.local') do set PORTA=%%a
set /a TENTATIVAS=0
echo Aguardando o sistema iniciar...
:espera
curl -fs http://localhost:%PORTA%/api/saude >nul 2>nul
if not errorlevel 1 goto pronto
set /a TENTATIVAS+=1
if %TENTATIVAS% GEQ 60 goto demorou
timeout /t 3 /nobreak >nul
goto espera

:pronto
echo Pronto! Abrindo http://localhost:%PORTA%
start "" http://localhost:%PORTA%
pause
exit /b 0

:semdocker
echo Docker nao encontrado. Instale o Docker Desktop: https://www.docker.com/products/docker-desktop/
pause
exit /b 1
:dockerparado
echo O Docker nao esta rodando. Abra o Docker Desktop, espere ele ficar pronto e rode de novo.
pause
exit /b 1
:semfront
echo Nao encontrei a pasta ..\Sistema-Planilha-Front. Ela precisa ficar ao lado desta pasta.
pause
exit /b 1
:criaenv
copy .env.local.example .env.local >nul
echo Criei o arquivo .env.local. Vou abri-lo: troque as senhas, salve e rode este arquivo de novo.
notepad .env.local
pause
exit /b 1
:editaenv
echo O .env.local ainda tem senhas de exemplo com a palavra troque. Edite-o, salve e rode de novo.
notepad .env.local
pause
exit /b 1
:demorou
echo Demorou mais que o esperado. Veja os detalhes com: docker compose -f docker-compose.local.yml --env-file .env.local logs backend
pause
exit /b 1
:erro
echo Algo deu errado ao iniciar. Veja a mensagem acima.
pause
exit /b 1

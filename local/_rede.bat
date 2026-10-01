@echo off
rem Usado por iniciar.bat e atualizar.bat: traduz o .env.local para o que o compose precisa.
set ENDERECO_REDE=127.0.0.1
set ACESSO_REDE_ATIVO=0
set PORTA=8080
set ACESSO=nao
for /f "tokens=1,* delims==" %%a in ('findstr /b /c:"PORTA=" .env.local') do set PORTA=%%b
for /f "tokens=1,* delims==" %%a in ('findstr /b /c:"ACESSO_REDE=" .env.local') do set ACESSO=%%b
if /i "%ACESSO%"=="sim" goto rede
exit /b 0
:rede
set ENDERECO_REDE=0.0.0.0
set ACESSO_REDE_ATIVO=1
exit /b 0

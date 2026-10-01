# Rodar o sistema na sua máquina

Aqui o sistema roda no **seu computador**, só para você: abre em `http://localhost:8080`, com login, e **não fica acessível para outros aparelhos nem para a internet**. Tudo roda dentro do Docker, então você **não precisa instalar** Java, Node nem PostgreSQL.

## O que você precisa
- **Docker Desktop** (grátis para uso pessoal): https://www.docker.com/products/docker-desktop/
  - Windows 10/11: o instalador cuida do WSL2; pode pedir para reiniciar o computador.
  - Mac e Linux também funcionam (no Linux, Docker Engine com o plugin `compose`).
- Os dois repositórios **lado a lado** na mesma pasta: `Sistema-Planilha-Back` e `Sistema-Planilha-Front`.
  - Com Git: `git clone https://github.com/FilipeBravos/Sistema-Planilha-Back.git` e o mesmo para o `-Front`.
  - Sem Git: em cada repositório do GitHub, `Code → Download ZIP`, extraia e **renomeie as pastas** para `Sistema-Planilha-Back` e `Sistema-Planilha-Front` (o ZIP vem com `-main` no nome).

## Primeira vez
1. Abra o **Docker Desktop** e espere aparecer que ele está rodando.
2. Entre na pasta `Sistema-Planilha-Back/local` e rode:
   - **Windows:** dê dois cliques em `iniciar.bat`
   - **Mac/Linux:** `./local/iniciar.sh` (dentro de `Sistema-Planilha-Back`)
3. Na primeira vez ele cria o arquivo `.env.local` e pede para você editá-lo: troque as senhas (o texto "troque…"), salve e rode o `iniciar` de novo.
   - `POSTGRES_PASSWORD`: invente um texto longo qualquer (é interno, você não digita depois).
   - `APP_USUARIOS_INICIAIS`: quem entra no sistema, no formato `login:senha,login2:senha2` (senhas com 8+ caracteres, sem vírgula).
4. Espere montar (alguns minutos só na primeira vez). O navegador abre em **http://localhost:8080**. Entre com um dos logins e **troque a senha** em "Alterar senha".

## Dia a dia
| O que | Windows | Mac/Linux |
|---|---|---|
| Ligar | `local\iniciar.bat` | `./local/iniciar.sh` |
| Desligar | `local\parar.bat` | `./local/parar.sh` |
| Backup | `local\backup.bat` | `./local/backup.sh` |
| Atualizar para a versão nova | `local\atualizar.bat` | `./local/atualizar.sh` |

- O **Docker Desktop precisa estar aberto** para o sistema funcionar. Nas configurações dele há a opção de abrir junto com o computador; assim o sistema volta sozinho quando você ligar o PC (se você não o tiver desligado com `parar`).
- Desligar o sistema **não apaga seus dados**.

## Seus dados e backup
Os dados ficam no banco PostgreSQL, dentro de um "volume" do Docker, que sobrevive a desligar e atualizar.
**Não apague esse volume** (nem use `docker compose down -v` ou "Delete volume" no Docker Desktop), e lembre que **desinstalar o Docker Desktop apaga tudo**.

- **Fazer backup:** rode o script de backup. Ele cria um arquivo na pasta `backups/` (`.sql.gz` no Mac/Linux, `.sql` no Windows).
- **Guarde uma cópia fora do computador** (pendrive, Google Drive etc.). Se o computador estragar, o backup salva seus dados. Faça de vez em quando, por exemplo uma vez por semana.
- **Restaurar** um backup (com o sistema ligado e o banco vazio):
  - Mac/Linux: `gunzip -c backups/ARQUIVO.sql.gz | docker compose -f docker-compose.local.yml --env-file .env.local exec -T db psql -U planilha planilha`
  - Windows (Prompt de Comando): `type backups\ARQUIVO.sql | docker compose -f docker-compose.local.yml --env-file .env.local exec -T db psql -U planilha planilha`

## Problemas comuns
- **"Docker não está rodando":** abra o Docker Desktop e espere ele ficar pronto.
- **A porta 8080 está ocupada:** mude `PORTA=` no `.env.local` (ex.: `8090`) e rode o `iniciar` de novo; o endereço passa a ser `http://localhost:8090`.
- **Esqueci a senha:** apague o usuário e deixe o `.env.local` recriá-lo com a senha provisória de lá (depois troque pela tela):
  ```
  docker compose -f docker-compose.local.yml --env-file .env.local exec db psql -U planilha planilha -c "delete from usuario where login='filipe'"
  docker compose -f docker-compose.local.yml --env-file .env.local restart backend
  ```
- **"Muitas tentativas":** o login bloqueia por 15 minutos depois de 5 senhas erradas seguidas.
- **Ver o que está acontecendo:** `docker compose -f docker-compose.local.yml --env-file .env.local logs backend`

## Usar em dois computadores (ou celular) na mesma rede Wi-Fi
Rode o sistema em **um computador só** (o "principal") e abra o endereço dele nos outros. Assim todos usam o **mesmo banco de dados**. Não rode o sistema em cada computador: cada um teria seus próprios dados.

1. **No computador principal**, abra o arquivo `.env.local` e mude `ACESSO_REDE=nao` para `ACESSO_REDE=sim` (escreva exatamente assim, sem espaços). Rode o `iniciar` de novo.
2. O `iniciar` mostra o endereço para usar nos outros aparelhos, algo como `http://192.168.0.15:8080`. (Se precisar descobrir sozinho: no Windows, `ipconfig`, linha "Endereço IPv4" da rede Wi-Fi; no Mac, `ipconfig getifaddr en0`; no Linux, `hostname -I`.)
3. **Firewall:** o Windows pode perguntar se permite o Docker receber conexões. Permita em rede **Privada**. Se o outro aparelho não conseguir abrir, libere a porta 8080 no Firewall do Windows.
4. **No outro computador**, abra esse endereço no navegador e entre com o seu login. Pronto.

Para voltar a deixar o sistema só neste computador, mude para `ACESSO_REDE=nao` e rode o `iniciar` de novo.

**Atualização automática:** a tela se atualiza sozinha a cada 15 segundos (enquanto a aba está aberta e visível), então o que um computador lança aparece no outro em segundos, sem apertar F5. No topo há "↻ Atualizado às HH:MM:SS"; clique ali para atualizar na hora.

**Cuidados**
- **O principal precisa estar ligado**, com o Docker aberto; se ele desligar, os outros perdem o acesso (os dados não se perdem).
- **IP fixo:** o IP do principal pode mudar quando o roteador reinicia. Reserve um IP fixo para ele nas configurações do roteador ("reserva de DHCP"), para o endereço não mudar.
- **Segurança:** dentro da rede a conexão é HTTP, sem criptografia, e qualquer aparelho conectado ao seu Wi-Fi consegue abrir a tela de login (só o login protege os dados, com bloqueio após 5 senhas erradas). Use senha forte no Wi-Fi e senhas longas no sistema.
- **Edição ao mesmo tempo:** se duas pessoas editarem o mesmo lançamento ao mesmo tempo, vale o último que salvar.
- **Backup:** faça no computador principal.

Para acessar de qualquer lugar pela internet, veja o [DEPLOY.md](DEPLOY.md).

> Observação: os scripts de Mac/Linux (`.sh`) foram testados. Os de Windows (`.bat`) seguem o mesmo roteiro, mas não pude executá-los em um Windows; se algum falhar, mande a mensagem de erro.

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
O `ACESSO_REDE` do `.env.local` tem três valores: `nao` (padrão: só este computador), `sim` (todo o seu Wi-Fi, descrito aqui) e `tailscale` (só quem usar o Tailscale, veja a próxima seção).

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

Para dar acesso a alguém que **não está no seu Wi-Fi**, veja a próxima seção (Tailscale). Para deixar o sistema na internet o tempo todo, veja o [DEPLOY.md](DEPLOY.md).

## Dar acesso a alguém que não está na sua rede (Tailscale)
O **Tailscale** é uma VPN pessoal. Ele liga o computador dessa pessoa ao seu por um túnel **criptografado**, **sem abrir portas no roteador** e sem colocar o sistema na internet. Dentro do túnel a conexão é protegida, o que também resolve o fato de o sistema usar HTTP. O plano gratuito ("Personal") não expira e permite alguns usuários (hoje, 6; confira em https://tailscale.com/pricing).

O sistema continua rodando no **seu** computador, com os mesmos dados de todos. A outra pessoa só abre um endereço no navegador.

### Parte 1: você, no computador principal
1. Crie uma conta gratuita no Tailscale (https://login.tailscale.com), instale o aplicativo (https://tailscale.com/download) no computador principal e entre com a sua conta.
2. No `.env.local`, mude para `ACESSO_REDE=tailscale` e rode o `iniciar` de novo. Com essa opção o sistema é publicado **só** no `localhost` e no endereço Tailscale do seu computador: quem usar o Tailscale consegue abrir, mas **os aparelhos do seu Wi-Fi e de qualquer outra rede continuam sem acesso**. O `iniciar` mostra o endereço para passar à pessoa. Se o Tailscale não estiver instalado e conectado, o `iniciar` **para** com um aviso, em vez de abrir o sistema para outras redes.
   (`ACESSO_REDE=sim` também funcionaria, mas abriria o sistema para todo o seu Wi-Fi; só use se você também quiser isso.)
3. **Crie o login da pessoa.** Ainda no `.env.local`, acrescente o nome dela no fim de `APP_USUARIOS_INICIAIS`:
   ```
   APP_USUARIOS_INICIAIS=filipe:senha-do-filipe1,vagner:senha-do-vagner1,maria:senha-da-maria1
   ```
   Salve e rode o `iniciar` de novo. Isso cria **só** o usuário novo e não mexe nas senhas dos outros. A senha precisa ter 8 ou mais caracteres e não pode ter vírgula. Passe a senha provisória para a pessoa e peça que ela a troque no primeiro acesso ("Alterar senha").
4. Anote o **endereço Tailscale** do seu computador: o próprio `iniciar` mostra, e também aparece no aplicativo do Tailscale ou na página "Machines" do painel (https://login.tailscale.com/admin/machines). Ele parece com `100.101.102.103`.
5. **Compartilhe o seu computador com a pessoa:** no painel, em "Machines", encontre o seu computador e escolha a opção de **compartilhar** ("Share"). Envie o convite por e-mail ou copie o link e mande para ela. Use o **compartilhamento de uma máquina**, e não o convite de usuário para a sua rede: assim ela enxerga **só o seu computador**, e não o resto da sua rede.

### Parte 2: a outra pessoa
1. Instalar o Tailscale (computador ou celular), criar a **própria** conta gratuita e entrar.
2. Abrir o convite que você enviou e aceitar.
3. Abrir no navegador `http://ENDEREÇO-TAILSCALE:8080` (o endereço do passo 4, por exemplo `http://100.101.102.103:8080`) e entrar com o login e a senha provisória que você criou. O Tailscale precisa estar **conectado** nesse aparelho.

O navegador pode mostrar "Não seguro", por ser `http`. Isso é esperado: o túnel do Tailscale já criptografa a conexão.

### Cuidados
- **Depois de reiniciar o computador:** com `ACESSO_REDE=tailscale`, se o Docker subir o sistema antes de o Tailscale conectar, ele pode não abrir. Se isso acontecer, rode o `iniciar` de novo (ele confere o Tailscale e liga tudo).
- **O seu computador precisa estar ligado e acordado**, com o Docker Desktop aberto e o Tailscale conectado. Se ele dormir ou desligar, a pessoa perde o acesso (os dados não se perdem). Desative a suspensão automática do computador se ela for usar com frequência.
- **Todos os usuários veem e alteram tudo** (Uber, despesas, empréstimos e relatórios): ainda não existem perfis de acesso nem "somente leitura".
- **Para tirar o acesso de alguém:**
  1. No painel do Tailscale, pare de compartilhar o seu computador com ela.
  2. Remova o nome dela de `APP_USUARIOS_INICIAIS` no `.env.local` (senão o login seria recriado na próxima vez que ligar o sistema).
  3. Apague o login dela do sistema (troque `maria` pelo login dela):
     ```
     docker compose -f docker-compose.local.yml --env-file .env.local exec db psql -U planilha planilha -c "delete from usuario where login='maria'"
     ```
- **Se a pessoa não conseguir abrir:**
  - Confirme que o Tailscale está conectado nos **dois** computadores e que o convite foi aceito.
  - No Tailscale do **seu** computador, "Permitir conexões de entrada" ("Allow incoming connections") precisa estar ligado.
  - Confirme que o sistema está de pé no seu computador (`http://localhost:8080` abre) e que o `.env.local` está com `ACESSO_REDE=tailscale` (ou `sim`) e você rodou o `iniciar` depois de mudar.
  - No Windows, se o Firewall bloquear, crie uma regra de entrada liberando a porta 8080 para os endereços `100.64.0.0/10` (a faixa de endereços do Tailscale).
- **Segurança:** mantenha senhas longas e diferentes para cada pessoa, e só compartilhe o computador com quem você conhece.

> Observação: a publicação só pelo endereço Tailscale foi testada com um endereço simulado (porta aberta no `localhost` e nele, fechada para o IP da rede); este roteiro segue a documentação do Tailscale e não foi testado com uma conta real. Se algum nome de botão ou tela estiver diferente do atual, a documentação oficial (https://tailscale.com/docs) prevalece.

> Observação: os scripts de Mac/Linux (`.sh`) foram testados. Os de Windows (`.bat`) seguem o mesmo roteiro, mas não pude executá-los em um Windows; se algum falhar, mande a mensagem de erro.

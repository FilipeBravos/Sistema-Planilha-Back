# Como colocar o sistema online

Este guia publica o sistema em **um servidor só** (banco + backend + site), com **HTTPS automático** e login.
Você precisa de:

- Um **servidor Linux** (VPS) com Ubuntu 22.04 ou 24.04 e pelo menos 1 GB de RAM (2 GB é mais confortável).
- Um **domínio** (ex.: `planilha.seudominio.com.br`). O HTTPS depende dele. Como o sistema tem login, **não use na internet sem HTTPS**.

> O backend e o front ficam em repositórios separados. O `docker-compose.prod.yml` (neste repositório) espera os dois **lado a lado**:
> `Sistema-Planilha-Back/` e `Sistema-Planilha-Front/`.

## 1. Apontar o domínio para o servidor
No painel do seu domínio, crie um registro **A** para o endereço (ex.: `planilha`) com o **IP do servidor**. Pode levar de minutos a algumas horas para valer.

## 2. Preparar o servidor
Conecte por SSH e rode:

```bash
# Docker (instalador oficial)
curl -fsSL https://get.docker.com | sh

# Firewall: só SSH, HTTP e HTTPS
sudo ufw allow OpenSSH && sudo ufw allow 80 && sudo ufw allow 443 && sudo ufw enable
```

## 3. Baixar o código
```bash
git clone https://github.com/FilipeBravos/Sistema-Planilha-Back.git
git clone https://github.com/FilipeBravos/Sistema-Planilha-Front.git
cd Sistema-Planilha-Back
```
(Se os repositórios forem privados, o `git clone` pedirá usuário e um *token* do GitHub como senha.)
Enquanto as alterações estiverem em branch separada, use `git clone -b <branch> ...` nos dois.

## 4. Configurar
```bash
cp .env.example .env
nano .env
```
Edite os valores:

| Variável | O que colocar |
|---|---|
| `POSTGRES_PASSWORD` | uma senha longa para o banco (invente) |
| `APP_USUARIOS_INICIAIS` | os usuários, no formato `login:senha,login2:senha2` (senhas com 8+ caracteres, sem vírgula) |
| `SITE_ADDRESS` | o seu domínio, ex.: `planilha.seudominio.com.br` |
| `COOKIE_SEGURO` | `true` (deixe assim com HTTPS) |

O arquivo `.env` guarda segredos: **nunca** o envie para o GitHub (ele já está no `.gitignore`).

## 5. Subir
```bash
docker compose -f docker-compose.prod.yml up -d --build
```
A primeira vez demora alguns minutos (compila tudo). Depois abra `https://seu-dominio` e entre com um dos usuários.
O certificado HTTPS é emitido sozinho na primeira visita.

**No primeiro acesso, cada pessoa deve trocar a senha** (botão "Alterar senha" no topo). Depois disso, as senhas do `.env` não valem mais para quem já trocou: o `.env` só cria usuários que ainda não existem.

## 6. Dia a dia

**Atualizar o sistema** (depois de novas versões no GitHub):
```bash
cd Sistema-Planilha-Front && git pull && cd ../Sistema-Planilha-Back && git pull
docker compose -f docker-compose.prod.yml up -d --build
```

**Ver o que está acontecendo:**
```bash
docker compose -f docker-compose.prod.yml ps
docker compose -f docker-compose.prod.yml logs -f backend
```

**Reiniciar:** `docker compose -f docker-compose.prod.yml restart`. Quem estiver logado precisará entrar de novo (as sessões ficam na memória do backend); os dados não se perdem.

## 7. Backup (importante!)
Seus dados financeiros ficam só no banco do servidor. Se o servidor for perdido, os dados também.

```bash
./scripts/backup.sh        # gera backups/planilha-AAAA-MM-DD-HHMM.sql.gz (e apaga os com mais de 30 dias)
```
Agende todo dia às 3h com `crontab -e` (ajuste o caminho):
```
0 3 * * * cd /home/SEU_USUARIO/Sistema-Planilha-Back && ./scripts/backup.sh >> backups/backup.log 2>&1
```
**Copie os backups para outro lugar** (seu computador, Google Drive etc.), por exemplo com `scp usuario@servidor:~/Sistema-Planilha-Back/backups/*.gz .`.
Muitas hospedagens também oferecem *snapshot* do servidor: vale ativar.

**Restaurar** um backup (num banco vazio):
```bash
gunzip -c backups/planilha-AAAA-MM-DD-HHMM.sql.gz | docker compose -f docker-compose.prod.yml exec -T db psql -U planilha planilha
```

## 8. Problemas comuns
- **O site não abre / certificado não sai:** confira se o domínio já aponta para o IP (`ping seu-dominio`) e se as portas 80 e 443 estão liberadas no firewall do servidor **e** no painel da hospedagem.
- **"Muitas tentativas":** o login bloqueia por 15 minutos depois de 5 senhas erradas seguidas. Aguarde ou reinicie o backend.
- **Esqueci a senha:** apague o usuário e deixe o `.env` recriá-lo com uma senha provisória, depois troque pela tela:
  ```bash
  docker compose -f docker-compose.prod.yml exec db psql -U planilha planilha -c "delete from usuario where login='filipe'"
  docker compose -f docker-compose.prod.yml restart backend
  ```
  (a senha provisória é a que está em `APP_USUARIOS_INICIAIS` no `.env`)
- **Só quero testar sem domínio:** use `SITE_ADDRESS=:80` e `COOKIE_SEGURO=false` e acesse `http://IP-do-servidor`. **Só com dados de teste**: sem HTTPS a senha trafega sem proteção.

## Cuidados de segurança
- Use chave SSH em vez de senha no servidor e mantenha-o atualizado (`sudo apt update && sudo apt upgrade`).
- O banco **não** é aberto para a internet (só o backend acessa); mantenha assim.
- Use senhas longas e diferentes para cada pessoa.
- Faça e guarde backups fora do servidor.

## Outras hospedagens
O front espera a API no **mesmo endereço** (`/api`), por causa do cookie de login. Em plataformas separadas (site de um lado, API do outro) é preciso configurar um redirecionamento de `/api` para o backend (ex.: *rewrites* no Netlify/Vercel/Cloudflare) e usar um banco PostgreSQL gerenciado (`DB_URL`, `DB_USER`, `DB_PASSWORD`). O caminho acima, com tudo no mesmo servidor, é o mais simples de manter.

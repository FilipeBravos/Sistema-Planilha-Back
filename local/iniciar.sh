#!/usr/bin/env bash
# Liga o sistema na sua máquina (a 1ª vez demora uns minutos para montar tudo).
set -euo pipefail
cd "$(dirname "$0")/.."

if ! command -v docker >/dev/null 2>&1; then
  echo "Docker não encontrado. Instale o Docker Desktop: https://www.docker.com/products/docker-desktop/" >&2; exit 1
fi
if ! docker info >/dev/null 2>&1; then
  echo "O Docker não está rodando. Abra o Docker Desktop, espere ele ficar pronto e rode de novo." >&2; exit 1
fi
if [ ! -d ../Sistema-Planilha-Front ]; then
  echo "Não encontrei a pasta ../Sistema-Planilha-Front. Ela precisa ficar ao lado desta pasta." >&2; exit 1
fi
if [ ! -f .env.local ]; then
  cp .env.local.example .env.local
  echo "Criei o arquivo .env.local. Abra-o, troque as senhas (e os logins, se quiser) e rode este script de novo." >&2; exit 1
fi
if grep -q "troque" .env.local; then
  echo "O .env.local ainda tem senhas de exemplo ('troque...'). Edite-o e rode de novo." >&2; exit 1
fi

source local/_comum.sh
docker compose "${COMPOSE_ARGS[@]}" --env-file .env.local up -d
echo -n "Aguardando o sistema iniciar"
for _ in $(seq 1 60); do
  if curl -fs "http://localhost:$PORTA/api/saude" >/dev/null 2>&1; then
    echo; echo "Pronto! Abra: http://localhost:$PORTA"
    [ "$ACESSO_REDE_ATIVO" = 1 ] && mostrar_enderecos
    [ "$ACESSO_REDE_ATIVO" = 2 ] && mostrar_tailscale
    (xdg-open "http://localhost:$PORTA" || open "http://localhost:$PORTA") >/dev/null 2>&1 || true
    exit 0
  fi
  echo -n "."; sleep 3
done
echo; echo "Demorou mais que o esperado. Veja os detalhes com: docker compose -f docker-compose.local.yml logs backend" >&2; exit 1

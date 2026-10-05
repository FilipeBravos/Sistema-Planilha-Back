#!/usr/bin/env bash
# Baixa a versão mais nova do código e refaz o sistema (os dados são mantidos).
set -euo pipefail
cd "$(dirname "$0")/.."
git pull
(cd ../Sistema-Planilha-Front && git pull)
source local/_comum.sh
docker compose "${COMPOSE_ARGS[@]}" --env-file .env.local up -d --build
echo "Atualizado. Abra o sistema de novo no navegador."

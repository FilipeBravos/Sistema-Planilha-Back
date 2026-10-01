#!/usr/bin/env bash
# Desliga o sistema. Seus dados continuam guardados; ligue de novo com iniciar.sh.
cd "$(dirname "$0")/.."
docker compose -f docker-compose.local.yml --env-file .env.local stop
echo "Sistema desligado. Os dados foram mantidos."

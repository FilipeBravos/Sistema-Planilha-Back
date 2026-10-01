#!/usr/bin/env bash
# Backup do banco (PostgreSQL) em backups/planilha-AAAA-MM-DD-HHMM.sql.gz; apaga os com mais de 30 dias.
# Rode na pasta do backend:  ./scripts/backup.sh [arquivo-compose] [arquivo-env]   (padrão: docker-compose.prod.yml;
# no uso local: ./scripts/backup.sh docker-compose.local.yml, ou local/backup.sh). Para agendar, veja DEPLOY.md.
set -euo pipefail
cd "$(dirname "$0")/.."
mkdir -p backups
compose="${1:-docker-compose.prod.yml}"
args=(-f "$compose")
[ -n "${2:-}" ] && args+=(--env-file "$2")
arquivo="backups/planilha-$(date +%F-%H%M).sql.gz"
docker compose "${args[@]}" exec -T db pg_dump -U planilha planilha | gzip > "$arquivo"
test -s "$arquivo" || { echo "Backup vazio, algo deu errado" >&2; rm -f "$arquivo"; exit 1; }
find backups -name 'planilha-*.sql.gz' -mtime +30 -delete
echo "Backup salvo em $arquivo"

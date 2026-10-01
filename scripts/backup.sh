#!/usr/bin/env bash
# Backup do banco (PostgreSQL) em backups/planilha-AAAA-MM-DD-HHMM.sql.gz; apaga os com mais de 30 dias.
# Rode no servidor, na pasta do backend:  ./scripts/backup.sh     (ou agende no cron, veja DEPLOY.md)
set -euo pipefail
cd "$(dirname "$0")/.."
mkdir -p backups
arquivo="backups/planilha-$(date +%F-%H%M).sql.gz"
docker compose -f docker-compose.prod.yml exec -T db pg_dump -U planilha planilha | gzip > "$arquivo"
test -s "$arquivo" || { echo "Backup vazio, algo deu errado" >&2; rm -f "$arquivo"; exit 1; }
find backups -name 'planilha-*.sql.gz' -mtime +30 -delete
echo "Backup salvo em $arquivo"

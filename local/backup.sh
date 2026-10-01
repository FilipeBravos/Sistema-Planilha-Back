#!/usr/bin/env bash
# Salva uma cópia dos seus dados em backups/. Guarde também uma cópia fora do computador.
cd "$(dirname "$0")/.."
exec ./scripts/backup.sh docker-compose.local.yml .env.local

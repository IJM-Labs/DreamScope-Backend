#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
ENV_FILE="${ENV_FILE:-$ROOT_DIR/.env.production}"
COMPOSE_FILE="${COMPOSE_FILE:-$ROOT_DIR/compose.prod.yaml}"
BACKUP_DIR="${BACKUP_DIR:-$ROOT_DIR/backups/mysql}"

if [[ ! -f "$ENV_FILE" ]]; then
  echo "Missing environment file: $ENV_FILE" >&2
  exit 1
fi

mkdir -p "$BACKUP_DIR"

set -a
source "$ENV_FILE"
set +a

timestamp="$(date -u +"%Y%m%dT%H%M%SZ")"
backup_file="$BACKUP_DIR/${DB_NAME}_${timestamp}.sql.gz"

docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" exec -T db \
  mysqldump --single-transaction --quick --routines --triggers \
  -u"$DB_USERNAME" -p"$DB_PASSWORD" "$DB_NAME" | gzip > "$backup_file"

echo "Backup written to $backup_file"

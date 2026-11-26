#!/usr/bin/env bash
# 备份 staging MySQL 为 gzip 文件并生成 SHA-256 校验和
set -euo pipefail

BACKUP_DIR="${1:-${BACKUP_DIR:-./backups}}"
CONTAINER="${DB_CONTAINER:-wanhui-mysql-staging}"
DB_NAME="${DB_NAME:-wanhui}"
DB_USER="${DB_USERNAME:-wanhui_app}"
DB_PASSWORD="${DB_PASSWORD:?DB_PASSWORD is required}"

mkdir -p "$BACKUP_DIR"
STAMP="$(date +%Y%m%d-%H%M%S)"
FILE="$BACKUP_DIR/${DB_NAME}-${STAMP}.sql.gz"

echo "[backup] dumping ${DB_NAME} from ${CONTAINER} -> ${FILE}"
docker exec -e MYSQL_PWD="$DB_PASSWORD" "$CONTAINER" \
  mysqldump -u"$DB_USER" --single-transaction --quick --routines --triggers --events "$DB_NAME" \
  | gzip -9 > "$FILE"

sha256sum "$FILE" > "$FILE.sha256"
echo "[backup] completed"
echo "[backup] sha256=$(cut -d' ' -f1 "$FILE.sha256")"

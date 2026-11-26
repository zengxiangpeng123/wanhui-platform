#!/usr/bin/env bash
# 从 gzip 备份恢复 staging MySQL，并在恢复前校验 SHA-256
set -euo pipefail

FILE="${1:?usage: restore.sh <backup.sql.gz>}"
CONTAINER="${DB_CONTAINER:-wanhui-mysql-staging}"
DB_NAME="${DB_NAME:-wanhui}"
DB_USER="${DB_USERNAME:-wanhui_app}"
DB_PASSWORD="${DB_PASSWORD:?DB_PASSWORD is required}"

if [ ! -f "$FILE" ]; then
  echo "[restore] backup not found: $FILE" >&2
  exit 1
fi

if [ -f "${FILE}.sha256" ]; then
  echo "[restore] verifying checksum"
  sha256sum -c "${FILE}.sha256"
else
  echo "[restore] warning: checksum file missing, skipping verification"
fi

echo "[restore] restoring ${DB_NAME} on ${CONTAINER}"
gunzip -c "$FILE" | docker exec -i -e MYSQL_PWD="$DB_PASSWORD" "$CONTAINER" mysql -u"$DB_USER" "$DB_NAME"
echo "[restore] completed"

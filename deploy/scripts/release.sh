#!/usr/bin/env bash
# 生产发布脚本：记录回滚点、切换镜像 tag、启动并做健康检查
set -euo pipefail

cd "$(dirname "$0")/.."
ENV_FILE="${ENV_FILE:-.env.prod}"
COMPOSE_FILE="${COMPOSE_FILE:-compose.prod.yml}"
STATE_FILE=".release-state"

if [ ! -f "$ENV_FILE" ]; then
  echo "[release] missing $ENV_FILE" >&2
  exit 1
fi

APP_IMAGE_TAG="${APP_IMAGE_TAG:?set APP_IMAGE_TAG}"
FRONTEND_IMAGE_TAG="${FRONTEND_IMAGE_TAG:?set FRONTEND_IMAGE_TAG}"

previous_app="$(grep -E '^APP_IMAGE_TAG=' "$ENV_FILE" | tail -n1 | cut -d= -f2- || true)"
previous_frontend="$(grep -E '^FRONTEND_IMAGE_TAG=' "$ENV_FILE" | tail -n1 | cut -d= -f2- || true)"

cat > "$STATE_FILE" <<EOF
previous_app_tag=${previous_app}
previous_frontend_tag=${previous_frontend}
released_app_tag=${APP_IMAGE_TAG}
released_frontend_tag=${FRONTEND_IMAGE_TAG}
released_at=$(date -u +%Y-%m-%dT%H:%M:%SZ)
EOF
echo "[release] rollback point saved to $STATE_FILE"

set_env() {
  local key="$1" value="$2"
  if grep -qE "^${key}=" "$ENV_FILE"; then
    sed -i.bak -E "s|^${key}=.*|${key}=${value}|" "$ENV_FILE" && rm -f "${ENV_FILE}.bak"
  else
    printf '%s=%s\n' "$key" "$value" >> "$ENV_FILE"
  fi
}
set_env APP_IMAGE_TAG "$APP_IMAGE_TAG"
set_env FRONTEND_IMAGE_TAG "$FRONTEND_IMAGE_TAG"

echo "[release] starting app=${APP_IMAGE_TAG} frontend=${FRONTEND_IMAGE_TAG}"
docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" up -d

echo "[release] waiting for readiness"
ready=0
for _ in $(seq 1 40); do
  if curl -kfsS https://127.0.0.1/api/v1/resources >/dev/null 2>&1; then
    ready=1
    break
  fi
  sleep 3
done

if [ "$ready" -ne 1 ]; then
  echo "[release] health check failed; run ./scripts/rollback.sh" >&2
  exit 1
fi

echo "[release] success"
docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" ps

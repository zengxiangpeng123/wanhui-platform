#!/usr/bin/env bash
# 生产回滚：读取 .release-state 恢复上一个可用镜像 tag 并验证
set -euo pipefail

cd "$(dirname "$0")/.."
ENV_FILE="${ENV_FILE:-.env.prod}"
COMPOSE_FILE="${COMPOSE_FILE:-compose.prod.yml}"
STATE_FILE=".release-state"

if [ ! -f "$STATE_FILE" ]; then
  echo "[rollback] missing $STATE_FILE" >&2
  exit 1
fi

previous_app="$(grep -E '^previous_app_tag=' "$STATE_FILE" | cut -d= -f2-)"
previous_frontend="$(grep -E '^previous_frontend_tag=' "$STATE_FILE" | cut -d= -f2-)"

if [ -z "$previous_app" ] || [ -z "$previous_frontend" ]; then
  echo "[rollback] previous tags are empty; manual intervention required" >&2
  exit 1
fi

set_env() {
  local key="$1" value="$2"
  if grep -qE "^${key}=" "$ENV_FILE"; then
    sed -i.bak -E "s|^${key}=.*|${key}=${value}|" "$ENV_FILE" && rm -f "${ENV_FILE}.bak"
  else
    printf '%s=%s\n' "$key" "$value" >> "$ENV_FILE"
  fi
}
set_env APP_IMAGE_TAG "$previous_app"
set_env FRONTEND_IMAGE_TAG "$previous_frontend"

echo "[rollback] restoring app=${previous_app} frontend=${previous_frontend}"
docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" up -d

echo "[rollback] waiting for readiness"
ready=0
for _ in $(seq 1 40); do
  if curl -kfsS https://127.0.0.1/api/v1/resources >/dev/null 2>&1; then
    ready=1
    break
  fi
  sleep 3
done

if [ "$ready" -ne 1 ]; then
  echo "[rollback] health check failed; manual intervention required" >&2
  exit 1
fi

echo "[rollback] success"
docker compose --env-file "$ENV_FILE" -f "$COMPOSE_FILE" ps

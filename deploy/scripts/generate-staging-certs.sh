#!/usr/bin/env bash
# 为 staging 生成自签名证书（生产请替换为受信任证书）
set -euo pipefail

CERT_DIR="${1:-$(cd "$(dirname "$0")/.." && pwd)/nginx/certs}"
mkdir -p "$CERT_DIR"

openssl req -x509 -nodes -newkey rsa:2048 -days 365 \
  -keyout "$CERT_DIR/server.key" \
  -out "$CERT_DIR/server.crt" \
  -subj "/CN=staging.wanhui.local"

chmod 600 "$CERT_DIR/server.key"
echo "[certs] generated: $CERT_DIR/server.crt"

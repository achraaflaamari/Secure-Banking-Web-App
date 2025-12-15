#!/bin/sh
set -eu

CERT_DIR=/etc/nginx/certs
CERT_KEY="$CERT_DIR/dev.key"
CERT_CRT="$CERT_DIR/dev.crt"

mkdir -p "$CERT_DIR"

if [ ! -f "$CERT_KEY" ] || [ ! -f "$CERT_CRT" ]; then
  openssl req -x509 -nodes -newkey rsa:2048 \
    -keyout "$CERT_KEY" \
    -out "$CERT_CRT" \
    -days 365 \
    -subj "/C=MA/ST=Dev/L=Dev/O=KMA/OU=Dev/CN=localhost"
fi

exec nginx -g 'daemon off;'

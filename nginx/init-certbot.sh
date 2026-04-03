#!/usr/bin/env bash
# ── init-certbot.sh ───────────────────────────────────────────────────────────
# Run ONCE on a fresh server to obtain the initial Let's Encrypt certificate.
#
# Prerequisites:
#   - Docker + Docker Compose installed
#   - DNS A records for zelus.gg, www.zelus.gg, api.zelus.gg pointing to this server
#   - .env file populated (cp .env.example .env && nano .env)
#
# Usage:
#   chmod +x nginx/init-certbot.sh
#   ./nginx/init-certbot.sh

set -euo pipefail

# ── Load env ──────────────────────────────────────────────────────────────────
if [ ! -f .env ]; then
    echo "[ERROR] .env file not found. Copy .env.example and fill in values."
    exit 1
fi
source .env

DOMAIN="${DOMAIN:-zelus.gg}"
EMAIL="${EMAIL:-admin@zelus.gg}"
STAGING="${STAGING:-0}"   # set STAGING=1 in .env to test without rate-limit

# ── Step 1: Spin up Nginx (HTTP only for ACME challenge) ─────────────────────
echo "==> Starting Nginx (HTTP mode)..."
docker compose --profile web up -d nginx

# Give Nginx a moment to start
sleep 3

# ── Step 2: Request certificate ───────────────────────────────────────────────
STAGING_FLAG=""
if [ "$STAGING" = "1" ]; then
    STAGING_FLAG="--staging"
    echo "[INFO] Using Let's Encrypt STAGING server (no rate limits, cert not trusted)."
fi

echo "==> Requesting TLS certificate for $DOMAIN, www.$DOMAIN, api.$DOMAIN ..."
docker compose run --rm certbot certonly \
    --webroot \
    --webroot-path=/var/www/certbot \
    --email "$EMAIL" \
    --agree-tos \
    --no-eff-email \
    $STAGING_FLAG \
    -d "$DOMAIN" \
    -d "www.${DOMAIN}" \
    -d "api.${DOMAIN}"

# ── Step 3: Download recommended TLS parameters ───────────────────────────────
echo "==> Fetching Let's Encrypt TLS options and dhparams..."
docker compose run --rm --entrypoint "" certbot sh -c "
    # options-ssl-nginx.conf (cipher suites, protocols)
    if [ ! -f /etc/letsencrypt/options-ssl-nginx.conf ]; then
        wget -q -O /etc/letsencrypt/options-ssl-nginx.conf \
            https://raw.githubusercontent.com/certbot/certbot/master/certbot-nginx/certbot_nginx/_internal/tls_configs/options-ssl-nginx.conf
    fi
    # DH parameters (2048-bit takes ~30 s; 4096-bit takes several minutes)
    if [ ! -f /etc/letsencrypt/ssl-dhparams.pem ]; then
        echo 'Generating 2048-bit DH params (this takes ~30 seconds)...'
        openssl dhparam -out /etc/letsencrypt/ssl-dhparams.pem 2048
    fi
"

# ── Step 4: Restart Nginx with full SSL config ────────────────────────────────
echo "==> Reloading Nginx with SSL enabled..."
docker compose --profile web up -d --force-recreate nginx

# ── Step 5: Start the certbot renewal daemon ──────────────────────────────────
echo "==> Starting Certbot auto-renewal daemon..."
docker compose --profile web up -d certbot

echo ""
echo "✓ Done! Your site is now live at:"
echo "    https://${DOMAIN}"
echo "    https://api.${DOMAIN}"
echo ""
echo "Certificates auto-renew every 12 hours when < 30 days remain."

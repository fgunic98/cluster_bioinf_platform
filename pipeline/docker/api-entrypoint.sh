#!/usr/bin/env sh
set -eu

until [ -f /vault/secrets/application.properties ]; do
  echo "Waiting for Vault Agent to render application.properties..."
  sleep 2
done

exec java -jar /app/app.jar
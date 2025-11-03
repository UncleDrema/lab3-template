#!/usr/bin/env bash
# ci/pull_and_update.sh
# Usage: ./pull_and_update.sh <compose-file-path>
# Example: ./pull_and_update.sh docker-compose.yml
set -euo pipefail

COMPOSE_FILE="${1:-docker-compose.yml}"

if [ ! -f "$COMPOSE_FILE" ]; then
  echo "Compose file not found: $COMPOSE_FILE"
  exit 2
fi

echo "Using compose file: $COMPOSE_FILE"

echo "Stopping services..."
docker compose -f "$COMPOSE_FILE" stop
docker compose -f "$COMPOSE_FILE" rm -f

echo "Pulling images..."
docker compose -f "$COMPOSE_FILE" pull

echo "Bringing services up..."
docker compose -f "$COMPOSE_FILE" up --build -d --remove-orphans

echo "Waiting for services to stabilize..."
for i in {1..30}; do
  echo "  ... $i/30"
  sleep 1
done

echo "Services status:"
docker compose -f "$COMPOSE_FILE" ps

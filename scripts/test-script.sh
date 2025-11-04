#!/usr/bin/env bash

set -e

variant=${1:-${VARIANT}}
service=${2:-${SERVICE_NAME}}
port=${3:-${PORT_NUMBER}}

path=$(dirname "$0")

dump_logs() {
  local exit_code=$?
  if [[ $exit_code -ne 0 ]]; then
    echo ""
    echo "=== ERROR OCCURRED! Dumping docker-compose logs ==="
    echo ""
    docker compose logs --tail=100
    echo ""
    echo "=== End of logs ==="
  fi
  return $exit_code
}

# Установить trap для отлова ошибок
trap 'dump_logs' ERR

timed() {
  end=$(date +%s)
  dt=$(("$end" - $1))
  dd=$(("$dt" / 86400))
  dt2=$(("$dt" - 86400 * "$dd"))
  dh=$(("$dt2" / 3600))
  dt3=$(("$dt2" - 3600 * "$dh"))
  dm=$(("$dt3" / 60))
  ds=$(("$dt3" - 60 * "$dm"))

  LC_NUMERIC=C printf "\nTotal runtime: %02d min %02d seconds\n" "$dm" "$ds"
}

init() {
    newman run \
    --delay-request=100 \
    --folder=init \
    --export-environment "$variant"/postman/environment.json \
    --environment "$variant"/postman/environment.json \
    "$variant"/postman/collection.json
}

success() {
  newman run \
    --delay-request=100 \
    --folder=success \
    --export-environment "$variant"/postman/environment.json \
    --environment "$variant"/postman/environment.json \
    "$variant"/postman/collection.json
}

step() {
  local step=$1
  [[ $((step % 2)) -eq 0 ]] && operation="start" || operation="stop"

  printf "=== Step %d: %s %s ===\n" "$step" "$operation" "$service"

  docker compose "$operation" "$service"
  if [[ "$operation" == "start" ]]; then
    "$path"/wait-for.sh -t 120 "http://localhost:$port/manage/health" -- echo "Host localhost:$port is active"
  fi

  newman run \
    --delay-request=100 \
    --folder=step"$step" \
    --export-environment "$variant"/postman/environment.json \
    --environment "$variant"/postman/environment.json \
    "$variant"/postman/collection.json

  printf "=== Step %d completed ===\n" "$step"
}

start=$(date +%s)
trap 'timed $start' EXIT

printf "=== Start test scenario ===\n"

# init data
init

# success execute
success

# stop service
step 1

# start service
step 2

# stop service
step 3

# start service
step 4

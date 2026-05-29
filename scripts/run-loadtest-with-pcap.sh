#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
RESULTS="$ROOT/loadtest/results"
mkdir -p "$RESULTS"

TARGET_TPS="${TARGET_TPS:-250}"
TARGET_REQUESTS="${TARGET_REQUESTS:-1000000}"
DURATION_SEC="${DURATION_SEC:-$((TARGET_REQUESTS / TARGET_TPS))}"
SMOKE_ONLY="${SMOKE_ONLY:-false}"
SKIP_PCAP="${SKIP_PCAP:-false}"

if [[ "$SMOKE_ONLY" == "true" ]]; then
  DURATION_SEC=60
  TARGET_REQUESTS=$((TARGET_TPS * DURATION_SEC))
fi

echo "TPS=$TARGET_TPS REQUESTS=$TARGET_REQUESTS DURATION=${DURATION_SEC}s"

curl -fsS http://localhost:8081/actuator/health >/dev/null || {
  echo "Start stack: docker compose up -d"
  exit 1
}

PCAP_CONTAINER=""
TX_CONTAINER="$(docker ps --filter 'name=transaction-service' --format '{{.Names}}' | head -1)"
if [[ "$SKIP_PCAP" != "true" && -n "$TX_CONTAINER" ]]; then
  docker run -d --name swiftpay-pcap-capture --net=container:"$TX_CONTAINER" \
    -v "$RESULTS:/out" nicolaka/netshoot \
    tcpdump -i any -s 0 -w /out/swiftpay-payment-load.pcap "tcp port 8081" || true
  PCAP_CONTAINER=swiftpay-pcap-capture
fi

export TRANSACTION_URL="${TRANSACTION_URL:-http://localhost:8081}"
export TARGET_TPS TARGET_REQUESTS DURATION_SEC

if command -v k6 >/dev/null; then
  k6 run "$ROOT/k6/load-test-1m.js"
else
  docker run --rm -v "$ROOT:/scripts" -w /scripts grafana/k6:latest run /scripts/load-test-1m.js
fi

[[ -n "$PCAP_CONTAINER" ]] && docker stop "$PCAP_CONTAINER" 2>/dev/null || true
echo "Done. PCAP: $RESULTS/swiftpay-payment-load.pcap"

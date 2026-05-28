#!/usr/bin/env bash
set -euo pipefail

root_dir="$(cd "$(dirname "$0")/.." && pwd)"
out_dir="$root_dir/docs/openapi"
mkdir -p "$out_dir"

echo "Exporting OpenAPI JSON from running services..."

curl -fsS "http://localhost:8081/api-docs" -o "$out_dir/transaction-service-openapi.json"
curl -fsS "http://localhost:8082/api-docs" -o "$out_dir/ledger-service-openapi.json"
curl -fsS "http://localhost:8083/api-docs" -o "$out_dir/analytics-service-openapi.json"

echo "Saved to: $out_dir"


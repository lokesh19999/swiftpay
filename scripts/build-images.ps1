$ErrorActionPreference = "Stop"

Set-Location -Path (Split-Path -Parent $PSScriptRoot)

Write-Host "Building SwiftPay images with repo-root build context..."

docker build -f transaction-service/Dockerfile -t swiftpay/transaction-service:local .
docker build -f ledger-service/Dockerfile -t swiftpay/ledger-service:local .
docker build -f analytics-service/Dockerfile -t swiftpay/analytics-service:local .

Write-Host "Done. Images:"
docker images | Select-String "swiftpay/"


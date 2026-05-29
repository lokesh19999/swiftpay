# Docker for transaction-service

## Use the root compose file

```powershell
cd D:\SwiftPay
docker compose -p swiftpay up -d --no-build
```

## Do not use broken paths

This command fails (`D:\ledger-service` not found) because an old compose file used wrong `dockerfile` paths:

```powershell
# WRONG — do not use
docker compose -f transaction-service/docker-compose.yml -p transaction-service up -d
```

If your IDE runs that automatically, change the run configuration to:

- **Compose file:** `D:\SwiftPay\docker-compose.yml`
- **Project name:** `swiftpay`

## Build one service (context = repo root)

```powershell
cd D:\SwiftPay
docker build -f transaction-service/Dockerfile -t swiftpay-transaction-service:latest .
```

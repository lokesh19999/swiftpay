# SwiftPay Hackathon Submission

## Project
- **Name:** SwiftPay Real-Time Payment Ledger
- **Stack:** Java 21, Spring Boot 3, PostgreSQL, Kafka, Redis, Docker, Kubernetes

## Core Deliverables
- Microservices source code:
  - `transaction-service/`
  - `ledger-service/`
  - `analytics-service/` (bonus)
  - `shared-kernel/`
- Infrastructure files:
  - `docker-compose.yml`
  - `k8s/`
  - `.github/workflows/ci.yml`
- API docs setup:
  - Swagger UI endpoints on each service
  - Export scripts:
    - `scripts/export-openapi.ps1`
    - `scripts/export-openapi.sh`

## OpenAPI JSON Deliverables
Generated API documents:
- `docs/openapi/transaction-service-openapi.json`
- `docs/openapi/ledger-service-openapi.json`
- `docs/openapi/analytics-service-openapi.json`

## How to Generate OpenAPI JSON
```powershell
docker compose up -d
powershell -ExecutionPolicy Bypass -File .\scripts\export-openapi.ps1
```

## API Endpoints
- Transaction service: `http://localhost:8081`
- Ledger service: `http://localhost:8082`
- Analytics service: `http://localhost:8083`

## Swagger URLs
- `http://localhost:8081/swagger-ui.html`
- `http://localhost:8082/swagger-ui.html`
- `http://localhost:8083/swagger-ui.html`

## Notes
- Core payment flow is asynchronous via Kafka (`payment.initiated` -> ledger -> `payment.completed`/`payment.failed`).
- Redis is used for idempotency with 24h TTL.
- Health endpoints are enabled with Spring Actuator.

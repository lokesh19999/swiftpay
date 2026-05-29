# Load Test (250 TPS / 1 Million Requests) + PCAP

Hackathon requirement (from challenge PDF):

> Perform a load test at **250 TPS** for a total of **1 million transactions**, and provide the **resulting PCAP trace**.

## Prerequisites

- Docker stack running: `docker compose up -d`
- **k6** installed ([https://k6.io/docs/get-started/installation/](https://k6.io/docs/get-started/installation/))  
  OR use Docker: `docker pull grafana/k6:latest`
- **Wireshark** (to open the PCAP): [https://www.wireshark.org/download.html](https://www.wireshark.org/download.html)

## Quick smoke run (~60s, good for verifying setup)

```powershell
cd D:\SwiftPay
.\scripts\run-loadtest-with-pcap.ps1 -SmokeOnly
```

Produces:
- `loadtest/results/swiftpay-payment-load.pcap` (short capture)
- ~15,000 HTTP requests at 250 TPS

## Full run (1,000,000 requests at 250 TPS)

Duration ≈ **4,000 seconds (~67 minutes)**.

```powershell
.\scripts\run-loadtest-with-pcap.ps1 -TargetRequests 1000000 -TargetTps 250
```

Shorter proof run (100k requests, ~7 minutes):

```powershell
.\scripts\run-loadtest-with-pcap.ps1 -TargetRequests 100000 -TargetTps 250
```

## PCAP capture details

The script starts a `tcpdump` container on Docker network `swiftpay_swiftpay-network` and captures **TCP traffic to port 8081** (transaction-service).

Open in Wireshark:

1. File → Open → `loadtest/results/swiftpay-payment-load.pcap`
2. Filter: `http` or `tcp.port == 8081`
3. Follow TCP streams → HTTP to inspect `POST /v1/payments` payloads

### Alternative: Windows pktmon (if Docker tcpdump fails)

Run **PowerShell as Administrator**:

```powershell
# Terminal 1
.\scripts\capture-pcap-pktmon.ps1 -Start

# Terminal 2
k6 run -e TARGET_TPS=250 -e DURATION_SEC=60 -e TARGET_REQUESTS=15000 .\k6\load-test-1m.js

# Terminal 1
.\scripts\capture-pcap-pktmon.ps1 -Stop
```

Output: `loadtest/results/swiftpay-payment-load.pcap`

## Manual k6 (without script)

```powershell
$env:TARGET_TPS="250"
$env:TARGET_REQUESTS="1000000"
$env:DURATION_SEC="4000"
$env:TRANSACTION_URL="http://localhost:8081"
k6 run .\k6\load-test-1m.js
```

## Submission package checklist

Include in zip/repo:

| Artifact | Path |
|----------|------|
| Load test script | `k6/load-test-1m.js` |
| PCAP trace | `loadtest/results/swiftpay-payment-load.pcap` |
| Run instructions | This file (`LOADTEST.md`) |
| Screenshot | Wireshark showing HTTP POST `/v1/payments` (optional but recommended) |

## Notes for judges

- Each request uses a **new idempotency key** (realistic load, not one deduplicated transaction).
- Seeded accounts: Alice `a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11`, Bob `b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a22`.
- The script **alternates $1 transfers** A→B / B→A so balances recycle for long runs.
- API returns `202` immediately; ledger settlement is async. HTTP TPS is what k6 measures at 250/s.
- Optional high-balance seed for one-way tests: `scripts/seed-loadtest-balances.sql`

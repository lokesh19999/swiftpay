#Requires -Version 5.1
<#
.SYNOPSIS
  Run k6 load test at 250 TPS (up to 1M requests) and capture PCAP for hackathon submission.

.PARAMETER TargetTps
  Target requests per second (default 250).

.PARAMETER TargetRequests
  Total HTTP requests (default 1000000). Duration = TargetRequests / TargetTps seconds.

.PARAMETER DurationSec
  Optional fixed duration in seconds (overrides TargetRequests for duration calc).

.PARAMETER SmokeOnly
  If set, runs 60s at TargetTps only (~15k requests) for quick PCAP demo.

.PARAMETER SkipPcap
  Skip tcpdump capture.

.EXAMPLE
  .\scripts\run-loadtest-with-pcap.ps1
  .\scripts\run-loadtest-with-pcap.ps1 -TargetRequests 1000000
  .\scripts\run-loadtest-with-pcap.ps1 -SmokeOnly
#>
param(
    [int]$TargetTps = 250,
    [long]$TargetRequests = 1000000,
    [int]$DurationSec = 0,
    [switch]$SmokeOnly,
    [switch]$SkipPcap
)

$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent $PSScriptRoot
$ResultsDir = Join-Path $Root "loadtest\results"
$PcapPath = Join-Path $ResultsDir "swiftpay-payment-load.pcap"
$SummaryJson = Join-Path $ResultsDir "loadtest-summary.json"
$K6Script = Join-Path $Root "k6\load-test-1m.js"

New-Item -ItemType Directory -Force -Path $ResultsDir | Out-Null

if ($SmokeOnly) {
    $DurationSec = 60
    $TargetRequests = $TargetTps * $DurationSec
} elseif ($DurationSec -le 0) {
    $DurationSec = [math]::Ceiling($TargetRequests / $TargetTps)
}

Write-Host "=== SwiftPay load test + PCAP capture ===" -ForegroundColor Cyan
Write-Host "TPS: $TargetTps | Requests: $TargetRequests | Duration: ${DurationSec}s (~$([math]::Round($TargetRequests / $DurationSec, 1)) TPS avg)"
Write-Host "Results: $ResultsDir"

# Health check
try {
    $h = Invoke-RestMethod -TimeoutSec 5 -Uri "http://localhost:8081/actuator/health"
    if ($h.status -ne "UP") { throw "transaction-service not UP" }
    Write-Host "transaction-service: UP" -ForegroundColor Green
} catch {
    Write-Host "ERROR: Start stack first: docker compose up -d" -ForegroundColor Red
    exit 1
}

$TcpdumpContainer = $null
$txContainer = (docker ps --filter "name=transaction-service" --format "{{.Names}}" | Select-Object -First 1)

if (-not $SkipPcap) {
    if (-not $txContainer) {
        Write-Host "WARN: transaction-service container not running; PCAP skipped." -ForegroundColor Yellow
        $SkipPcap = $true
    } else {
        Write-Host "Starting PCAP capture on container: $txContainer (all inbound HTTP)..."
        docker run -d --name swiftpay-pcap-capture --net=container:$txContainer `
            -v "${ResultsDir}:/out" nicolaka/netshoot `
            tcpdump -i any -s 0 -w /out/swiftpay-payment-load.pcap "tcp port 8081" 2>$null
        if ($LASTEXITCODE -ne 0) {
            Write-Host "WARN: tcpdump failed. Use host capture: pktmon (see LOADTEST.md)." -ForegroundColor Yellow
            $SkipPcap = $true
        } else {
            $TcpdumpContainer = "swiftpay-pcap-capture"
            Write-Host "PCAP capture started (container: $TcpdumpContainer)" -ForegroundColor Green
        }
    }
}

$env:TRANSACTION_URL = "http://localhost:8081"
$env:TARGET_TPS = "$TargetTps"
$env:TARGET_REQUESTS = "$TargetRequests"
$env:DURATION_SEC = "$DurationSec"

Write-Host "Starting k6 (this may take a long time for 1M requests)..."
$k6Args = @(
    "run",
    "-e", "TRANSACTION_URL=$($env:TRANSACTION_URL)",
    "-e", "TARGET_TPS=$($env:TARGET_TPS)",
    "-e", "TARGET_REQUESTS=$($env:TARGET_REQUESTS)",
    "-e", "DURATION_SEC=$($env:DURATION_SEC)",
    $K6Script
)
if (Get-Command k6 -ErrorAction SilentlyContinue) {
    & k6 @k6Args
} else {
    docker run --rm `
        -e TRANSACTION_URL=http://host.docker.internal:8081 `
        -e TARGET_TPS=$($env:TARGET_TPS) `
        -e TARGET_REQUESTS=$($env:TARGET_REQUESTS) `
        -e DURATION_SEC=$($env:DURATION_SEC) `
        --add-host=host.docker.internal:host-gateway `
        -v "${Root}:/scripts" -w /scripts grafana/k6:latest run /scripts/k6/load-test-1m.js
}
$K6Exit = $LASTEXITCODE

if ($TcpdumpContainer) {
    docker stop $TcpdumpContainer | Out-Null
    docker rm $TcpdumpContainer | Out-Null
    if (Test-Path $PcapPath) {
        $sizeMb = [math]::Round((Get-Item $PcapPath).Length / 1MB, 2)
        Write-Host "PCAP saved: $PcapPath ($sizeMb MB)" -ForegroundColor Green
        Write-Host "Open with Wireshark: File -> Open -> $PcapPath"
    }
}

if ($K6Exit -ne 0) {
    Write-Host "k6 exited with code $K6Exit" -ForegroundColor Red
    exit $K6Exit
}

Write-Host "Load test finished successfully." -ForegroundColor Green
Write-Host "Next: copy loadtest/results/swiftpay-payment-load.pcap into submission package."

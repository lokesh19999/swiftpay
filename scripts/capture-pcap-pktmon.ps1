#Requires -Version 5.1
<#
.SYNOPSIS
  Windows host PCAP capture using pktmon (no Docker tcpdump required).
  Run as Administrator in one terminal, then run k6 in another.

.EXAMPLE
  # Terminal 1 (Admin):
  .\scripts\capture-pcap-pktmon.ps1 -Start
  # Terminal 2:
  k6 run -e TARGET_TPS=250 -e DURATION_SEC=60 -e TARGET_REQUESTS=15000 .\k6\load-test-1m.js
  # Terminal 1 (Admin):
  .\scripts\capture-pcap-pktmon.ps1 -Stop
#>
param(
    [Parameter(Mandatory = $true)]
    [ValidateSet("Start", "Stop")]
    [string]$Action
)

$Root = Split-Path -Parent $PSScriptRoot
$ResultsDir = Join-Path $Root "loadtest\results"
$Etl = Join-Path $ResultsDir "swiftpay-payment-load.etl"
$Pcap = Join-Path $ResultsDir "swiftpay-payment-load.pcap"
New-Item -ItemType Directory -Force -Path $ResultsDir | Out-Null

if ($Action -eq "Start") {
    pktmon filter remove 2>$null
    pktmon filter add -p 8081 -t TCP
    pktmon start --etw -f $Etl -m both
    Write-Host "pktmon capturing port 8081 -> $Etl"
    Write-Host "Run k6, then: .\scripts\capture-pcap-pktmon.ps1 -Stop"
} else {
    pktmon stop
    pktmon format $Etl -o $Pcap
    Write-Host "PCAP written: $Pcap"
}

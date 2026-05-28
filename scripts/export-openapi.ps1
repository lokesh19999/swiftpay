$ErrorActionPreference = "Stop"

function Export-OpenApi {
    param(
        [Parameter(Mandatory = $true)][string]$ServiceName,
        [Parameter(Mandatory = $true)][string]$Url
    )

    Write-Host "Exporting $ServiceName OpenAPI from $Url"
    $json = Invoke-RestMethod -TimeoutSec 30 -Uri $Url
    $outDir = Join-Path -Path (Split-Path -Parent $PSScriptRoot) -ChildPath "docs/openapi"
    New-Item -ItemType Directory -Force -Path $outDir | Out-Null
    $outPath = Join-Path -Path $outDir -ChildPath "$ServiceName-openapi.json"
    $json | ConvertTo-Json -Depth 100 | Out-File -Encoding utf8 $outPath
    Write-Host "Saved: $outPath"
}

# Services must be running (docker compose up -d)
Export-OpenApi -ServiceName "transaction-service" -Url "http://localhost:8081/api-docs"
Export-OpenApi -ServiceName "ledger-service" -Url "http://localhost:8082/api-docs"
Export-OpenApi -ServiceName "analytics-service" -Url "http://localhost:8083/api-docs"


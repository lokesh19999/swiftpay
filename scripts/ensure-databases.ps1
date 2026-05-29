# Creates SwiftPay databases if the Postgres volume was initialized before init-databases.sql existed.
$ErrorActionPreference = "Stop"
$sql = @"
SELECT 'CREATE DATABASE swiftpay_transactions' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'swiftpay_transactions')\gexec
SELECT 'CREATE DATABASE swiftpay_ledger' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'swiftpay_ledger')\gexec
SELECT 'CREATE DATABASE swiftpay_analytics' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'swiftpay_analytics')\gexec
"@
docker exec -i swiftpay-postgres psql -U swiftuser -d postgres -c "SELECT 1 FROM pg_database WHERE datname = 'swiftpay_analytics';" | Select-String "1 row"
if ($LASTEXITCODE -ne 0) {
    Write-Host "Creating missing databases..."
    docker exec swiftpay-postgres psql -U swiftuser -d postgres -c "CREATE DATABASE swiftpay_transactions;"
    docker exec swiftpay-postgres psql -U swiftuser -d postgres -c "CREATE DATABASE swiftpay_ledger;"
    docker exec swiftpay-postgres psql -U swiftuser -d postgres -c "CREATE DATABASE swiftpay_analytics;"
    Write-Host "Done."
} else {
    Write-Host "All SwiftPay databases exist."
}

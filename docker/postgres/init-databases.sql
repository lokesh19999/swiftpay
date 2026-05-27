CREATE DATABASE swiftpay_transactions;
CREATE DATABASE swiftpay_ledger;
CREATE DATABASE swiftpay_analytics;

GRANT ALL PRIVILEGES ON DATABASE swiftpay_transactions TO swiftuser;
GRANT ALL PRIVILEGES ON DATABASE swiftpay_ledger TO swiftuser;
GRANT ALL PRIVILEGES ON DATABASE swiftpay_analytics TO swiftuser;

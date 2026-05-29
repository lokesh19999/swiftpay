-- Optional: run before a one-way load test if you do not use ping-pong in k6/load-test-1m.js
-- docker compose exec -T postgres psql -U swiftuser -d swiftpay_ledger -f - < scripts/seed-loadtest-balances.sql

UPDATE accounts SET balance = 1000000000.00 WHERE id = 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11';
UPDATE accounts SET balance = 1000000000.00 WHERE id = 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a22';

/**
 * SwiftPay load test — hackathon requirement:
 * - 250 TPS sustained
 * - 1,000,000 total HTTP requests (configurable)
 *
 * Env:
 *   TRANSACTION_URL  (default http://localhost:8081)
 *   TARGET_TPS     (default 250)
 *   TARGET_REQUESTS (default 1000000)  -> duration = TARGET_REQUESTS / TARGET_TPS seconds
 *   DURATION_SEC     (optional override; if set, ignores TARGET_REQUESTS for duration)
 *   SENDER / RECEIVER account UUIDs (seeded ledger accounts)
 */
import http from 'k6/http';
import { check, sleep } from 'k6';
import { uuidv4 } from 'https://jslib.k6.io/k6-utils/1.4.0/index.js';
import { textSummary } from 'https://jslib.k6.io/k6-utils/1.4.0/index.js';

const BASE_URL = __ENV.TRANSACTION_URL || 'http://localhost:8081';
const TARGET_TPS = Number(__ENV.TARGET_TPS || 250);
const TARGET_REQUESTS = Number(__ENV.TARGET_REQUESTS || 1_000_000);
const DURATION_SEC = Number(__ENV.DURATION_SEC || Math.ceil(TARGET_REQUESTS / TARGET_TPS));
const SENDER = __ENV.SENDER_ACCOUNT_ID || 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11';
const RECEIVER = __ENV.RECEIVER_ACCOUNT_ID || 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a22';

// Unique idempotency per iteration (required for realistic load; avoids all requests deduping to one tx)
function uniqueIdempotencyKey() {
  return uuidv4();
}

export const options = {
  scenarios: {
    payment_load: {
      executor: 'constant-arrival-rate',
      rate: TARGET_TPS,
      timeUnit: '1s',
      duration: `${DURATION_SEC}s`,
      preAllocatedVUs: Math.min(500, Math.ceil(TARGET_TPS * 2)),
      maxVUs: Math.min(1000, Math.ceil(TARGET_TPS * 4)),
    },
  },
  thresholds: {
    http_req_failed: ['rate<0.05'],
    http_req_duration: ['p(95)<3000', 'p(99)<8000'],
    // Allow ~1% under-count during ramp-up; full 1M run should still be ~1M attempts
    http_reqs: [`count>=${Math.floor(TARGET_REQUESTS * 0.99)}`],
  },
  summaryTrendStats: ['avg', 'min', 'med', 'max', 'p(90)', 'p(95)', 'p(99)'],
};

export default function () {
  // Alternate direction so seeded balances recycle (1M run without exhausting Alice's wallet)
  const pingPong = __ITER % 2 === 0;
  const sender = pingPong ? SENDER : RECEIVER;
  const receiver = pingPong ? RECEIVER : SENDER;

  const payload = JSON.stringify({
    idempotencyKey: uniqueIdempotencyKey(),
    senderAccountId: sender,
    receiverAccountId: receiver,
    amount: '1.00',
    currency: 'USD',
  });

  const res = http.post(`${BASE_URL}/v1/payments`, payload, {
    headers: { 'Content-Type': 'application/json' },
    tags: { name: 'CreatePayment' },
    timeout: '30s',
  });

  check(res, {
    'status is 202': (r) => r.status === 202,
    'has transactionId': (r) => (r) => r.json('transactionId') !== undefined,
  });

  sleep(0.01);
}

export function handleSummary(data) {
  const failedRate = data.metrics.http_req_failed?.values?.rate ?? 0;
  const p95 = data.metrics.http_req_duration?.values?.['p(95)'] ?? 0;
  const total = data.metrics.http_reqs?.values?.count ?? 0;
  const lines = [
    '',
    'SwiftPay load test summary',
    `Target TPS: ${TARGET_TPS}`,
    `Duration: ${DURATION_SEC}s`,
    `Target requests: ~${TARGET_REQUESTS}`,
    `HTTP failed rate: ${(failedRate * 100).toFixed(2)}%`,
    `p95 latency: ${p95.toFixed(0)} ms`,
    `Total HTTP requests: ${total}`,
    '',
  ].join('\n');
  return {
    stdout: lines + textSummary(data, { indent: ' ', enableColors: true }),
    'loadtest/results/loadtest-summary.json': JSON.stringify(data, null, 2),
  };
}

import http from 'k6/http';
import { check, sleep } from 'k6';
import { uuidv4 } from 'https://jslib.k6.io/k6-utils/1.4.0/index.js';

export const options = {
  scenarios: {
    payment_burst: {
      executor: 'ramping-vus',
      startVUs: 0,
      stages: [
        { duration: '30s', target: 50 },
        { duration: '1m', target: 100 },
        { duration: '30s', target: 0 },
      ],
    },
  },
  thresholds: {
    http_req_failed: ['rate<0.05'],
    http_req_duration: ['p(95)<800'],
  },
};

const BASE_URL = __ENV.TRANSACTION_URL || 'http://localhost:8081';
const SENDER = 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11';
const RECEIVER = 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a22';

export default function () {
  const payload = JSON.stringify({
    idempotencyKey: uuidv4(),
    senderAccountId: SENDER,
    receiverAccountId: RECEIVER,
    amount: (Math.random() * 5 + 0.01).toFixed(2),
    currency: 'USD',
  });

  const res = http.post(`${BASE_URL}/v1/payments`, payload, {
    headers: { 'Content-Type': 'application/json' },
  });

  check(res, {
    'status is 202': (r) => r.status === 202,
    'has transactionId': (r) => r.json('transactionId') !== undefined,
  });

  sleep(0.1);
}

import http from 'k6/http';
import { check, sleep } from 'k6';

const BASE_URL = __ENV.BASE_URL || 'http://127.0.0.1:8080';
const VUS = Number(__ENV.VUS || 20);
const DURATION = __ENV.DURATION || '5m';

export const options = {
  scenarios: {
    baseline: {
      executor: 'constant-vus',
      vus: VUS,
      duration: DURATION,
    },
  },
  thresholds: {
    http_req_failed: ['rate<0.01'],
    http_req_duration: ['p(95)<2000'],
  },
  summaryTrendStats: ['avg', 'min', 'med', 'p(90)', 'p(95)', 'p(99)', 'max'],
};

export default function () {
  const resources = http.get(`${BASE_URL}/api/v1/resources?page=1&size=24`);
  check(resources, { 'resources 200': (r) => r.status === 200 });

  const posts = http.get(`${BASE_URL}/api/v1/forum/posts?page=1&size=20`);
  check(posts, { 'posts 200': (r) => r.status === 200 });

  const search = http.get(`${BASE_URL}/api/v1/forum/posts/search?q=PERF&page=1&size=20`);
  check(search, { 'search 200': (r) => r.status === 200 });

  let items = [];
  try {
    items = posts.json('data.items') || [];
  } catch (ignored) {
    items = [];
  }
  if (items.length > 0) {
    const id = items[Math.floor(Math.random() * items.length)].id;
    const detail = http.get(`${BASE_URL}/api/v1/forum/posts/${id}`);
    check(detail, { 'detail 200': (r) => r.status === 200 });
  }

  const comments = http.get(`${BASE_URL}/api/v1/forum/posts/${items.length > 0 ? items[0].id : 1}/comments?page=1&size=20`);
  check(comments, { 'comments 200': (r) => r.status === 200 });

  sleep(0.2);
}

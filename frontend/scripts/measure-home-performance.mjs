import { writeFile } from 'node:fs/promises';
import { chromium } from '@playwright/test';

const url = process.env.PERF_URL || 'http://127.0.0.1:4173/';
const runCount = Number(process.env.PERF_RUNS || 5);
const outputPath = process.env.PERF_OUTPUT;
const lcpThresholdMs = Number(process.env.PERF_LCP_THRESHOLD_MS || 3000);

if (!Number.isInteger(runCount) || runCount < 1) {
  throw new Error('PERF_RUNS must be a positive integer');
}

const browser = await chromium.launch({ headless: true });
const browserVersion = browser.version();
const results = [];

for (let run = 1; run <= runCount; run += 1) {
  const context = await browser.newContext({ viewport: { width: 1440, height: 900 } });
  const page = await context.newPage();
  const consoleErrors = [];
  const pageErrors = [];
  const failedRequests = [];

  page.on('console', (message) => {
    if (message.type() === 'error') consoleErrors.push(message.text());
  });
  page.on('pageerror', (error) => pageErrors.push(error.message));
  page.on('requestfailed', (request) => {
    failedRequests.push(`${request.method()} ${request.url()} ${request.failure()?.errorText || ''}`);
  });

  await page.addInitScript(() => {
    window.__lcp = 0;
    new PerformanceObserver((list) => {
      const entries = list.getEntries();
      const last = entries[entries.length - 1];
      if (last) window.__lcp = last.startTime;
    }).observe({ type: 'largest-contentful-paint', buffered: true });
  });

  const cdp = await context.newCDPSession(page);
  await cdp.send('Network.enable');
  await cdp.send('Network.setCacheDisabled', { cacheDisabled: true });
  await cdp.send('Network.clearBrowserCache');
  await cdp.send('Network.emulateNetworkConditions', {
    offline: false,
    latency: 50,
    downloadThroughput: (20 * 1024 * 1024) / 8,
    uploadThroughput: (5 * 1024 * 1024) / 8,
    connectionType: 'wifi'
  });

  const started = performance.now();
  const resourcesResponse = page.waitForResponse(
    (response) => response.url().includes('/api/v1/resources') && response.status() === 200,
    { timeout: 30_000 }
  );
  await page.goto(url, { waitUntil: 'domcontentloaded', timeout: 30_000 });
  await resourcesResponse;
  await page.locator('.resource-card, .empty-state').first().waitFor({ state: 'visible', timeout: 30_000 });
  const firstResourceMs = performance.now() - started;
  await page.waitForFunction(() => window.__lcp > 0, null, { timeout: 10_000 });
  await page.waitForTimeout(500);

  const metrics = await page.evaluate(() => {
    const navigation = performance.getEntriesByType('navigation')[0];
    const paint = Object.fromEntries(
      performance.getEntriesByType('paint').map((entry) => [entry.name, entry.startTime])
    );
    return {
      lcpMs: window.__lcp,
      fcpMs: paint['first-contentful-paint'] ?? null,
      domContentLoadedMs: navigation?.domContentLoadedEventEnd ?? null,
      loadMs: navigation?.loadEventEnd ?? null,
      responseStartMs: navigation?.responseStart ?? null,
      resourceEntries: performance.getEntriesByType('resource').length
    };
  });

  results.push({
    run,
    firstResourceMs,
    ...metrics,
    consoleErrors,
    pageErrors,
    failedRequests
  });
  await context.close();
}

await browser.close();

const sortedLcp = results.map((result) => result.lcpMs).sort((left, right) => left - right);
const summary = {
  capturedAt: new Date().toISOString(),
  url,
  browserVersion,
  network: {
    downloadMbps: 20,
    uploadMbps: 5,
    latencyMs: 50,
    cache: 'disabled'
  },
  threshold: { lcpLessThanOrEqualMs: lcpThresholdMs },
  aggregated: {
    runs: runCount,
    lcpMinMs: sortedLcp[0],
    lcpMedianMs: sortedLcp[Math.floor(sortedLcp.length / 2)],
    lcpMaxMs: sortedLcp[sortedLcp.length - 1],
    pageErrors: results.reduce((total, result) => total + result.pageErrors.length, 0),
    failedRequests: results.reduce((total, result) => total + result.failedRequests.length, 0)
  },
  results
};

const serialized = `${JSON.stringify(summary, null, 2)}\n`;
if (outputPath) await writeFile(outputPath, serialized, 'utf8');
process.stdout.write(serialized);

if (summary.aggregated.lcpMaxMs > lcpThresholdMs) {
  throw new Error(`LCP ${summary.aggregated.lcpMaxMs}ms exceeds ${lcpThresholdMs}ms`);
}
if (summary.aggregated.pageErrors > 0 || summary.aggregated.failedRequests > 0) {
  throw new Error('Homepage performance run observed JavaScript errors or failed requests');
}
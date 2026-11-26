import { defineConfig, devices } from '@playwright/test';

const desktop = { ...devices['Desktop Chrome'], viewport: { width: 1440, height: 900 } };
const mobile = { ...devices['Pixel 5'], viewport: { width: 375, height: 667 } };

export default defineConfig({
  testDir: './e2e',
  fullyParallel: false,
  workers: 1,
  retries: 0,
  timeout: 60_000,
  expect: { timeout: 10_000 },
  reporter: [
    ['list'],
    ['html', { outputFolder: 'playwright-report', open: 'never' }]
  ],
  use: {
    baseURL: 'http://127.0.0.1:5173',
    trace: 'retain-on-failure',
    screenshot: 'only-on-failure'
  },
  projects: [
    { name: 'desktop-chromium', use: desktop },
    { name: 'mobile-chromium', use: mobile }
  ],
  webServer: [
    {
      command: 'mvnw.cmd -B -q spring-boot:run',
      cwd: '../backend',
      env: { ...process.env, WANHUI_SECURITY_RATE_LIMIT_ENABLED: 'false' },
      url: 'http://127.0.0.1:8080/api/v1/auth/csrf',
      reuseExistingServer: !process.env.CI,
      timeout: 180_000
    },
    {
      command: 'npm run dev',
      url: 'http://127.0.0.1:5173',
      reuseExistingServer: !process.env.CI,
      timeout: 90_000
    }
  ]
});

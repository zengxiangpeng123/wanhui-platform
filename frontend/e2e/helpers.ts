import { expect, type Page } from '@playwright/test';

export interface TestUser {
  username: string;
  email: string;
  password: string;
}

export function uniqueUser(prefix: string): TestUser {
  const stamp = Date.now().toString(36) + Math.random().toString(36).slice(2, 6);
  const username = `${prefix}_${stamp}`.slice(0, 30);
  return {
    username,
    email: `${username}@example.com`,
    password: 'Passw0rd!'
  };
}

export async function registerUser(page: Page, user: TestUser): Promise<void> {
  await page.goto('/register');
  await page.locator('input[autocomplete="username"]').fill(user.username);
  await page.locator('input[autocomplete="email"]').fill(user.email);
  await page.locator('input[autocomplete="new-password"]').fill(user.password);
  await page.getByTestId('register-submit').click();
  await expect(page).toHaveURL(/\/login/);
}

export async function loginUser(page: Page, user: TestUser): Promise<void> {
  await page.goto('/login');
  await page.locator('input[autocomplete="username"]').fill(user.username);
  await page.locator('input[autocomplete="current-password"]').fill(user.password);
  await page.getByTestId('login-submit').click();
  await expect(page.getByText('退出')).toBeVisible();
}

export async function registerAndLogin(page: Page, prefix: string): Promise<TestUser> {
  const user = uniqueUser(prefix);
  await registerUser(page, user);
  await loginUser(page, user);
  return user;
}

export async function expectNoHorizontalOverflow(page: Page): Promise<void> {
  const overflow = await page.evaluate(() => {
    const doc = document.documentElement;
    return doc.scrollWidth - window.innerWidth;
  });
  expect(overflow).toBeLessThanOrEqual(1);
}

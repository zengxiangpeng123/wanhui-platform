import { expect, test } from '@playwright/test';

const BRAND = 'rgb(23, 107, 91)';
const PAGE_BG = 'rgb(244, 246, 243)';

test.describe('高保真视觉对齐', () => {
  test('顶栏使用高保真品牌色和全局搜索', async ({ page }) => {
    await page.setViewportSize({ width: 1440, height: 900 });
    await page.goto('/');

    const brandMark = page.locator('.brand-mark');
    await expect(brandMark).toBeVisible();
    await expect(brandMark).toHaveCSS('background-color', BRAND);
    await expect(page.getByPlaceholder('搜索资源、工具或网站...')).toBeVisible();
    await expect(page.getByRole('link', { name: '论坛' })).toBeVisible();
    await expect(page.locator('.header-login')).toHaveCSS('background-color', BRAND);
    await expect(page.locator('body')).toHaveCSS('background-color', PAGE_BG);
  });
  test('资源首页使用工作台布局和绿色资源卡', async ({ page }) => {
    await page.setViewportSize({ width: 1440, height: 900 });
    await page.goto('/?category=design');

    await expect(page.locator('.welcome-card')).toBeVisible();
    await expect(page.locator('.welcome-card')).toHaveCSS('background-color', 'rgb(30, 90, 78)');
    await expect(page.locator('.resource-card').first()).toHaveCSS('border-radius', '16px');
    await expect(page.locator('.resource-card .visit-button').first()).toHaveCSS('background-color', BRAND);

    const columns = await page.locator('.resource-grid').evaluate((element) => getComputedStyle(element).gridTemplateColumns.split(' ').length);
    expect(columns).toBe(3);
  });
  test('论坛使用深绿色社区主视觉', async ({ page }) => {
    await page.setViewportSize({ width: 1440, height: 900 });
    await page.goto('/forum');

    await expect(page.locator('.forum-hero')).toHaveCSS('background-color', 'rgb(23, 63, 56)');
    await expect(page.locator('.forum-hero h1')).toHaveCSS('color', 'rgb(255, 255, 255)');
    await expect(page.locator('.category-nav')).toHaveCSS('background-color', 'rgb(255, 255, 255)');
  });
  test('登录页使用分栏品牌布局', async ({ page }) => {
    await page.setViewportSize({ width: 1440, height: 900 });
    await page.goto('/login');

    await expect(page.locator('.auth-promo')).toBeVisible();
    await expect(page.locator('.auth-card')).toHaveCSS('background-color', 'rgb(255, 255, 255)');
    const columns = await page.locator('.auth-wrap').evaluate((element) => getComputedStyle(element).gridTemplateColumns.split(' ').length);
    expect(columns).toBe(2);
  });
});
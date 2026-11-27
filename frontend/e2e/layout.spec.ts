import { expect, test } from '@playwright/test';
import { expectNoHorizontalOverflow, registerAndLogin } from './helpers';

test.describe('响应式布局', () => {
  test('桌面端无侧栏页面使用可读的主体宽度', async ({ page }) => {
    await page.setViewportSize({ width: 1440, height: 900 });
    await page.goto('/login');

    const authCardWidth = await page.locator('.auth-card').evaluate((element) => element.getBoundingClientRect().width);
    const mainWidth = await page.locator('.app-main').evaluate((element) => element.getBoundingClientRect().width);

    expect(authCardWidth).toBeGreaterThanOrEqual(420);
    expect(mainWidth).toBeGreaterThan(1200);
    await expectNoHorizontalOverflow(page);
  });

  test('移动端保留资源和论坛主导航', async ({ page }) => {
    await page.setViewportSize({ width: 375, height: 667 });
    await page.goto('/');
    const primaryNav = page.getByRole('navigation', { name: '主导航' });

    await expect(page.getByRole('link', { name: '万汇网首页' })).toBeVisible();
    await expect(primaryNav.getByRole('link', { name: '论坛' })).toBeVisible();
    await expectNoHorizontalOverflow(page);
  });

  test('移动端资源分类使用横向标签', async ({ page }) => {
    await page.setViewportSize({ width: 375, height: 667 });
    await page.goto('/');

    const categoryNav = page.getByRole('navigation', { name: '资源分类' });
    await expect(categoryNav).toBeVisible();
    const display = await categoryNav.evaluate((element) => getComputedStyle(element).display);
    expect(display).toBe('flex');
    await expectNoHorizontalOverflow(page);
  });

  test('移动端个人中心导航不占用首屏', async ({ page }) => {
    await page.setViewportSize({ width: 375, height: 667 });
    await registerAndLogin(page, 'layout');
    await page.goto('/user/profile');

    const userNav = page.getByRole('navigation', { name: '个人中心导航' });
    await expect(userNav).toBeVisible();
    const direction = await userNav.evaluate((element) => getComputedStyle(element).flexDirection);
    const headingTop = await page.locator('h1').evaluate((element) => element.getBoundingClientRect().top);

    expect(direction).toBe('row');
    expect(headingTop).toBeLessThan(420);
    await expectNoHorizontalOverflow(page);
  });
  test('注册页用户名提示与允许字符一致', async ({ page }) => {
    await page.goto('/register');
    const username = page.locator('input[autocomplete="username"]');

    await expect(username).toHaveAttribute('placeholder', '请输入用户名');
    await expect(page.locator('.field-hint')).toHaveText('3～32 位字母、数字、_ 或 -');
  });
});
import { expect, test } from '@playwright/test';
import { expectNoHorizontalOverflow, registerAndLogin } from './helpers';

test.describe('用户中心', () => {
  test('未登录访问用户中心跳转登录并保留回跳地址', async ({ page }) => {
    await page.goto('/user/profile');
    await expect(page).toHaveURL(/\/login/);
    await expect(page).toHaveURL(/redirect=/);
  });

  test('资料只读字段与昵称保存', async ({ page }) => {
    const user = await registerAndLogin(page, 'profile');
    await page.goto('/user/profile');

    const emailField = page.getByTestId('profile-email');
    await expect(emailField).toHaveValue(/@example\.com$/);
    await expect(emailField).toHaveAttribute('readonly', '');
    await expect(page.getByTestId('profile-phone')).toHaveAttribute('readonly', '');

    const nickname = page.getByTestId('profile-nickname');
    await nickname.fill('E2E 昵称');
    await page.getByTestId('profile-save').click();
    await expect(page.getByText('个人资料已保存')).toBeVisible();

    await page.reload();
    await expect(page.getByTestId('profile-nickname')).toHaveValue('E2E 昵称');
    await expect(page.getByTestId('profile-username')).toHaveValue(user.username);
    await expectNoHorizontalOverflow(page);
  });

  test('旧密码错误时提示且不退出登录', async ({ page }) => {
    await registerAndLogin(page, 'pwdwrong');
    await page.goto('/user/settings');

    await page.getByTestId('old-password').fill('WrongPass1');
    await page.getByTestId('new-password').fill('NewPassw0rd!');
    await page.getByTestId('confirm-password').fill('NewPassw0rd!');
    await page.getByTestId('password-submit').click();

    await expect(page.getByText('旧密码不正确')).toBeVisible();
    await expect(page).toHaveURL(/\/user\/settings/);
  });

  test('修改密码成功后旧会话失效且新密码可登录', async ({ page }) => {
    const user = await registerAndLogin(page, 'pwdok');
    await page.goto('/user/settings');

    await page.getByTestId('old-password').fill(user.password);
    await page.getByTestId('new-password').fill('NewPassw0rd!');
    await page.getByTestId('confirm-password').fill('NewPassw0rd!');
    await page.getByTestId('password-submit').click();

    await expect(page).toHaveURL(/\/login/);

    await page.locator('input[autocomplete="username"]').fill(user.username);
    await page.locator('input[autocomplete="current-password"]').fill(user.password);
    await page.getByTestId('login-submit').click();
    await expect(page.getByText('账号或密码不正确')).toBeVisible();

    await page.locator('input[autocomplete="username"]').fill(user.username);
    await page.locator('input[autocomplete="current-password"]').fill('NewPassw0rd!');
    await page.getByTestId('login-submit').click();
    await expect(page.getByText('退出')).toBeVisible();
  });

  test('删除自定义网站需要二次确认', async ({ page }) => {
    await registerAndLogin(page, 'site');
    await page.goto('/user/sites');
    await expect(page.getByText('还没有自定义网站')).toBeVisible();

    await page.getByTestId('site-name').fill('E2E 站点');
    await page.getByTestId('site-url').fill('https://e2e-' + Date.now() + '.example.com');
    await page.getByTestId('site-submit').click();
    await expect(page.getByText('E2E 站点')).toBeVisible();

    await page.locator('.delete-button').first().click();
    await expect(page.getByTestId('confirm-dialog')).toBeVisible();
    await page.getByTestId('confirm-cancel').click();
    await expect(page.getByText('E2E 站点')).toBeVisible();

    await page.locator('.delete-button').first().click();
    await page.getByTestId('confirm-ok').click();
    await expect(page.getByText('还没有自定义网站')).toBeVisible();
  });

  test('空收藏与空网站提供可操作提示', async ({ page }) => {
    await registerAndLogin(page, 'emptystate');

    await page.goto('/user/favorites');
    await expect(page.getByText('还没有收藏资源')).toBeVisible();
    await expect(page.getByText('去发现资源')).toBeVisible();

    await page.goto('/user/sites');
    await expect(page.getByText('还没有自定义网站')).toBeVisible();
    await expect(page.getByTestId('site-form')).toBeVisible();
  });
});

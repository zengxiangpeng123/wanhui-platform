import { expect, test } from '@playwright/test';
import { expectNoHorizontalOverflow, loginUser, registerUser, uniqueUser } from './helpers';

test.describe('认证流程', () => {
  test('注册 → 登录 → 刷新保持登录 → 退出', async ({ page }) => {
    const user = uniqueUser('auth');
    await registerUser(page, user);
    await loginUser(page, user);
    await expect(page.locator('.login-link')).toHaveCount(0);

    await page.reload();
    await expect(page.getByText('退出')).toBeVisible();

    await page.getByText('退出').click();
    await expect(page.locator('.login-link')).toBeVisible();
    await expectNoHorizontalOverflow(page);
  });
});

import { expect, test } from '@playwright/test';
import { expectNoHorizontalOverflow, registerAndLogin } from './helpers';

test.describe('用户中心', () => {
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
});



import { expect, test } from '@playwright/test';
import { expectNoHorizontalOverflow, registerAndLogin } from './helpers';

test.describe('资源浏览与收藏', () => {
  test('分类 → 搜索 → 收藏 → 用户中心查看', async ({ page }) => {
    await registerAndLogin(page, 'res');
    await page.goto('/');

    await page.getByRole('button', { name: '设计资源' }).click();
    await expect(page).toHaveURL(/category=design/);

    await page.getByTestId('resource-search').fill('Figma');
    await page.getByTestId('resource-search-submit').click();
    await expect(page).toHaveURL(/q=Figma/);

    const favoriteButton = page.getByTestId('resource-favorite-res-001');
    await expect(favoriteButton).toBeVisible();
    await expect(favoriteButton).toContainText('收藏');
    await favoriteButton.click();
    await expect(favoriteButton).toContainText('已收藏');

    await page.goto('/user/favorites');
    await expect(page.getByText('Figma').first()).toBeVisible();
    await expectNoHorizontalOverflow(page);
  });
});

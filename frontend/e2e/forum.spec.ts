import { expect, test } from '@playwright/test';
import { expectNoHorizontalOverflow, registerAndLogin } from './helpers';

test.describe('论坛发帖与互动', () => {
  test('登录 → 发帖 → 详情 → 评论 → 点赞 → 收藏', async ({ page }) => {
    await registerAndLogin(page, 'forum');
    await page.goto('/forum');
    await expect(page.getByTestId('category-academic')).toBeVisible();

    const title = `E2E 测试帖 ${Date.now()}`;
    await page.getByTestId('open-editor').click();
    await page.getByTestId('post-title').fill(title);
    await page.getByTestId('post-content').fill('这是 E2E 自动化测试创建的帖子正文。');
    await page.getByTestId('post-submit').click();

    await expect(page.getByText(title)).toBeVisible();
    await page.getByText(title).click();
    await expect(page).toHaveURL(/\/forum\/post\/\d+/);

    await page.getByTestId('comment-input').fill('E2E 评论内容');
    await page.getByTestId('comment-submit').click();
    await expect(page.getByText('E2E 评论内容')).toBeVisible();

    await page.getByTestId('toggle-like').click();
    await expect(page.getByTestId('toggle-like')).toContainText('已点赞');

    await page.getByTestId('toggle-favorite').click();
    await expect(page.getByTestId('toggle-favorite')).toContainText('已收藏');
    await expectNoHorizontalOverflow(page);
  });
});

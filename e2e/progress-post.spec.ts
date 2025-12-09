import { test, expect } from '@playwright/test';

test.describe('Progress Post', () => {
  test.beforeEach(async ({ page }) => {
    // Login before each test
    await page.goto('/login');
    await page.fill('input[type="email"]', 'testuser@example.com');
    await page.fill('input[type="password"]', 'password');
    await page.click('button[type="submit"]');
    // ログイン後はホームページにリダイレクトされ、デフォルトで投稿タブが表示される
    await page.waitForURL('/', { timeout: 10000 });
  });

  test('should create a new progress post', async ({ page }) => {
    // 既にホームページにいるので、投稿タブが表示されているはず

    const postButton = page.locator('button:has-text("投稿する")');
    await postButton.click();

    await page.fill('textarea[name="content"]', 'テスト投稿です');
    await page.click('button[type="submit"]');

    await expect(page.locator('text=テスト投稿です')).toBeVisible();
  });

  test('should display progress posts list', async ({ page }) => {
    // 既にホームページにいるので、投稿タブが表示されているはず

    const posts = page.locator('[data-testid="progress-post"]');
    await expect(posts.first()).toBeVisible();
  });

  test('should reply to a progress post', async ({ page }) => {
    // 既にホームページにいるので、投稿タブが表示されているはず

    const firstPost = page.locator('[data-testid="progress-post"]').first();
    await firstPost.locator('button:has-text("返信")').click();

    await page.fill('textarea[name="reply"]', 'テスト返信です');
    await page.click('button[type="submit"]');

    await expect(page.locator('text=テスト返信です')).toBeVisible();
  });
});

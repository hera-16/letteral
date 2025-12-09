import { test, expect } from '@playwright/test';

test.describe('Progress Post', () => {
  test.beforeEach(async ({ page }) => {
    // Login before each test
    await page.goto('/login');
    await page.fill('input[type="email"]', 'test@example.com');
    await page.fill('input[type="password"]', 'password123');
    await page.click('button[type="submit"]');
    await page.waitForURL(/.*progress/);
  });

  test('should create a new progress post', async ({ page }) => {
    await page.goto('/progress');

    const postButton = page.locator('button:has-text("投稿する")');
    await postButton.click();

    await page.fill('textarea[name="content"]', 'テスト投稿です');
    await page.click('button[type="submit"]');

    await expect(page.locator('text=テスト投稿です')).toBeVisible();
  });

  test('should display progress posts list', async ({ page }) => {
    await page.goto('/progress');

    const posts = page.locator('[data-testid="progress-post"]');
    await expect(posts.first()).toBeVisible();
  });

  test('should reply to a progress post', async ({ page }) => {
    await page.goto('/progress');

    const firstPost = page.locator('[data-testid="progress-post"]').first();
    await firstPost.locator('button:has-text("返信")').click();

    await page.fill('textarea[name="reply"]', 'テスト返信です');
    await page.click('button[type="submit"]');

    await expect(page.locator('text=テスト返信です')).toBeVisible();
  });
});

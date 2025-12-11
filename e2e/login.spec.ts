import { test, expect } from '@playwright/test';

test.describe('Login Flow', () => {
  test('should display login page', async ({ page }) => {
    await page.goto('/login');
    await expect(page.locator('h2:has-text("ログイン")')).toBeVisible();
    await expect(page.locator('input[type="email"]')).toBeVisible();
    await expect(page.locator('input[type="password"]')).toBeVisible();
  });

  test('should show error for invalid credentials', async ({ page }) => {
    await page.goto('/login');
    await page.fill('input[type="email"]', 'invalid@example.com');
    await page.fill('input[type="password"]', 'wrongpassword');
    await page.click('button[type="submit"]');

    // APIから返される実際のエラーメッセージを待つ
    await expect(page.locator('text=ユーザー名またはパスワードが正しくありません')).toBeVisible({ timeout: 10000 });
  });

  test('should successfully login with valid credentials', async ({ page }) => {
    await page.goto('/login');
    await page.fill('input[type="email"]', 'testuser@example.com');
    await page.fill('input[type="password"]', 'password');
    await page.click('button[type="submit"]');

    // ログイン後はホームページにリダイレクトされる
    await page.waitForURL('/', { timeout: 10000 });
    await expect(page.locator('text=ログアウト')).toBeVisible();
  });
});

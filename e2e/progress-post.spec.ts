import { test, expect } from '@playwright/test';

test.describe('Progress Post', () => {
  test.beforeEach(async ({ page }) => {
    // Login before each test
    await page.goto('/login');
    await page.fill('input[type="email"]', 'ceo@test-company.com');
    await page.fill('input[type="password"]', 'password');

    // ログインボタンをクリックしてナビゲーションを待つ
    await Promise.all([
      page.waitForURL('/', { timeout: 15000 }),
      page.click('button[type="submit"]')
    ]);

    // ホームページの主要な要素が読み込まれるまで待つ
    await page.waitForSelector('text=🏢 所属グループ', { timeout: 10000 });
  });

  test('should create a new progress post', async ({ page }) => {
    // 投稿タイプを選択（進捗を選択）
    const progressTypeButton = page.locator('button:has-text("進捗")').first();
    await progressTypeButton.click();

    // 内容を入力
    await page.fill('textarea[placeholder*="今日の進捗"]', 'E2Eテスト投稿です');

    // 投稿ボタンをクリック
    const submitButton = page.locator('button:has-text("投稿する")');
    await submitButton.click();

    // 投稿が表示されることを確認
    await expect(page.locator('text=E2Eテスト投稿です')).toBeVisible({ timeout: 10000 });
  });

  test('should display progress posts list', async ({ page }) => {
    // 組織を選択してから投稿一覧を確認
    const firstOrg = page.locator('button').filter({ hasText: /テストカンパニー株式会社|第1営業部|第2営業部/ }).first();
    await firstOrg.click();

    // 投稿一覧が表示されていることを確認
    const postsListHeader = page.locator('text=📋 投稿一覧');
    await expect(postsListHeader).toBeVisible();
  });

  test('should select organization and view posts', async ({ page }) => {
    // 所属グループセクションが表示されていることを確認
    const groupsHeader = page.locator('text=🏢 所属グループ');
    await expect(groupsHeader).toBeVisible();

    // 最初の組織をクリック
    const firstOrg = page.locator('button').filter({ hasText: /テストカンパニー株式会社|第1営業部|第2営業部/ }).first();
    await firstOrg.click();

    // 投稿一覧が表示されることを確認
    await expect(page.locator('text=📋 投稿一覧')).toBeVisible();
  });
});

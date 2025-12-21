-- Add test user for E2E testing
-- Username: testuser
-- Email: testuser@example.com
-- Password: password
-- BCrypt hash for "password": $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy

-- 1. E2Eテスト用のテナントを作成（既に存在する場合はスキップ）
MERGE INTO tenants (id, name, slug, plan_type, status, max_users, max_storage_gb, contact_email, created_at)
KEY(id)
VALUES (
  999,
  'E2E Test Organization',
  'e2e-test',
  'FREE',
  'ACTIVE',
  100,
  10,
  'e2e-test@example.com',
  NOW()
);

-- 2. E2Eテスト用の組織を作成（既に存在する場合はスキップ）
MERGE INTO organizations (id, tenant_id, parent_id, name, organization_type, level, path, created_at)
KEY(id)
VALUES (
  999,
  999,
  NULL,
  'E2E Test Organization',
  'COMPANY',
  1,
  '/999',
  NOW()
);

-- 3. E2Eテスト用のポリシー設定を作成（既に存在する場合はスキップ）
-- V32でdefault_visibilityが削除されたため、それは含めない
MERGE INTO policy_settings (
  tenant_id,
  organization_id,
  default_anonymity_level,
  allow_full_anonymous,
  allow_real_name,
  allow_company_wide,
  require_approval,
  min_post_length,
  max_post_length,
  enable_ng_word_filter,
  enable_auto_moderation,
  created_at
)
KEY(tenant_id, organization_id)
VALUES (
  999,
  999,
  'PSEUDONYM',
  TRUE,
  TRUE,
  FALSE,
  FALSE,
  10,
  5000,
  TRUE,
  TRUE,
  NOW()
);

-- 4. E2Eテスト用のユーザーを作成（既に存在する場合はスキップ）
-- 固定ID 999を使用してH2互換性を確保
MERGE INTO users (id, username, email, password, display_name, tenant_id, primary_organization_id, role, is_active, created_at)
KEY(id)
VALUES (
  999,
  'testuser',
  'testuser@example.com',
  '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
  'Test User',
  999,
  999,
  'USER',
  TRUE,
  NOW()
);

-- 5. 組織メンバーシップを作成（既に存在する場合はスキップ）
MERGE INTO organization_members (tenant_id, organization_id, user_id, role, is_primary, joined_at)
KEY(tenant_id, organization_id, user_id)
VALUES (
  999,
  999,
  999,
  'MEMBER',
  TRUE,
  NOW()
);

-- 6. 匿名プロフィールを作成（既に存在する場合はスキップ）
MERGE INTO anonymous_profiles (tenant_id, organization_id, user_id, anonymous_id, display_name, anonymity_level, created_at)
KEY(tenant_id, organization_id, user_id)
VALUES (
  999,
  999,
  999,
  'anonymous_999999',
  'テストユーザー',
  'PSEUDONYM',
  NOW()
);

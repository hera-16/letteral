-- V9: usersテーブルの拡張
-- テナント管理、組織情報、ロール管理などの追加
-- 作成日: 2025-11-07

-- usersテーブルにカラムを追加
ALTER TABLE users ADD COLUMN tenant_id BIGINT;
ALTER TABLE users ADD COLUMN primary_organization_id BIGINT;
ALTER TABLE users ADD COLUMN role VARCHAR(20) NOT NULL DEFAULT 'USER';
ALTER TABLE users ADD COLUMN is_active BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE users ADD COLUMN employee_id VARCHAR(50);
ALTER TABLE users ADD COLUMN department VARCHAR(100);
ALTER TABLE users ADD COLUMN position VARCHAR(100);
ALTER TABLE users ADD COLUMN hire_date DATE;
ALTER TABLE users ADD COLUMN phone_number VARCHAR(20);
ALTER TABLE users ADD COLUMN settings VARCHAR(4000);
ALTER TABLE users ADD COLUMN updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

-- 外部キー制約を追加
ALTER TABLE users ADD CONSTRAINT fk_users_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE;
ALTER TABLE users ADD CONSTRAINT fk_users_primary_org FOREIGN KEY (primary_organization_id) REFERENCES organizations(id) ON DELETE SET NULL;

-- インデックスを追加
CREATE INDEX idx_users_tenant ON users(tenant_id);
CREATE INDEX idx_users_primary_org ON users(primary_organization_id);
CREATE INDEX idx_users_employee_id ON users(employee_id);
CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_users_is_active ON users(is_active);
CREATE INDEX idx_users_tenant_username ON users(tenant_id, username);
CREATE INDEX idx_users_tenant_email ON users(tenant_id, email);
CREATE INDEX idx_users_tenant_active ON users(tenant_id, is_active);

-- CHECK制約を追加
ALTER TABLE users ADD CONSTRAINT chk_users_role CHECK (role IN ('SUPER_ADMIN', 'TENANT_ADMIN', 'ORG_ADMIN', 'MODERATOR', 'USER'));

-- 既存ユーザーのデータを更新（デフォルトテナントに紐付け）
UPDATE users SET tenant_id = 1, primary_organization_id = 1 WHERE tenant_id IS NULL;

-- tenant_idをNOT NULLに変更
ALTER TABLE users ALTER COLUMN tenant_id SET NOT NULL;

-- V9: usersテーブルの拡張
-- テナント管理、組織情報、ロール管理などの追加
-- 作成日: 2025-11-07

-- usersテーブルにカラムを追加
ALTER TABLE users
ADD COLUMN tenant_id BIGINT AFTER id,
ADD COLUMN primary_organization_id BIGINT AFTER tenant_id,
ADD COLUMN role VARCHAR(20) NOT NULL DEFAULT 'USER' AFTER password,
ADD COLUMN is_active BOOLEAN NOT NULL DEFAULT TRUE AFTER role,
ADD COLUMN employee_id VARCHAR(50) AFTER email,
ADD COLUMN department VARCHAR(100) AFTER display_name,
ADD COLUMN position VARCHAR(100) AFTER department,
ADD COLUMN hire_date DATE AFTER position,
ADD COLUMN phone_number VARCHAR(20) AFTER hire_date,
ADD COLUMN settings JSON AFTER last_login,
ADD COLUMN updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP AFTER created_at;

-- カラムにコメントを追加
ALTER TABLE users
MODIFY COLUMN tenant_id BIGINT COMMENT '所属テナント',
MODIFY COLUMN primary_organization_id BIGINT COMMENT '主所属組織',
MODIFY COLUMN role VARCHAR(20) NOT NULL DEFAULT 'USER' COMMENT 'システムロール',
MODIFY COLUMN is_active BOOLEAN NOT NULL DEFAULT TRUE COMMENT 'アクティブ状態',
MODIFY COLUMN employee_id VARCHAR(50) COMMENT '社員番号',
MODIFY COLUMN department VARCHAR(100) COMMENT '部署名',
MODIFY COLUMN position VARCHAR(100) COMMENT '役職',
MODIFY COLUMN hire_date DATE COMMENT '入社日',
MODIFY COLUMN phone_number VARCHAR(20) COMMENT '電話番号',
MODIFY COLUMN settings JSON COMMENT 'ユーザー設定',
MODIFY COLUMN updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新日時';

-- 外部キー制約を追加
ALTER TABLE users
ADD CONSTRAINT fk_users_tenant
    FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE,
ADD CONSTRAINT fk_users_primary_org
    FOREIGN KEY (primary_organization_id) REFERENCES organizations(id) ON DELETE SET NULL;

-- インデックスを追加
ALTER TABLE users
ADD INDEX idx_tenant (tenant_id),
ADD INDEX idx_primary_org (primary_organization_id),
ADD INDEX idx_employee_id (employee_id),
ADD INDEX idx_role (role),
ADD INDEX idx_is_active (is_active),
ADD INDEX idx_tenant_username (tenant_id, username),
ADD INDEX idx_tenant_email (tenant_id, email),
ADD INDEX idx_tenant_active (tenant_id, is_active);

-- CHECK制約を追加
ALTER TABLE users
ADD CONSTRAINT chk_user_role
    CHECK (role IN ('SUPER_ADMIN', 'TENANT_ADMIN', 'ORG_ADMIN', 'MODERATOR', 'USER'));

-- 既存ユーザーのデータを更新（デフォルトテナントに紐付け）
UPDATE users
SET tenant_id = 1,
    primary_organization_id = 1
WHERE tenant_id IS NULL;

-- tenant_idをNOT NULLに変更
ALTER TABLE users
MODIFY COLUMN tenant_id BIGINT NOT NULL COMMENT '所属テナント';

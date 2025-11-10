-- V5: テナント管理テーブルの作成
-- 企業向け匿名進捗・目標開示プラットフォーム対応
-- 作成日: 2025-11-07

-- テナント（企業・組織）テーブル
CREATE TABLE IF NOT EXISTS tenants (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(200) NOT NULL COMMENT '企業名・組織名',
    slug VARCHAR(100) NOT NULL UNIQUE COMMENT 'URL識別子 (例: acme-corp)',
    plan_type VARCHAR(20) NOT NULL DEFAULT 'FREE' COMMENT 'プランタイプ',
    max_users INT NOT NULL DEFAULT 50 COMMENT 'プラン別の最大ユーザー数',
    max_storage_gb INT NOT NULL DEFAULT 5 COMMENT 'ストレージ容量制限（GB）',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT 'テナントステータス',
    contact_email VARCHAR(100) COMMENT '管理者連絡先',
    settings JSON COMMENT 'カスタム設定（JSON）',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_slug (slug),
    INDEX idx_status (status),
    INDEX idx_plan_type (plan_type),
    INDEX idx_created_at (created_at),
    CHECK (plan_type IN ('FREE', 'PRO', 'ENTERPRISE')),
    CHECK (status IN ('ACTIVE', 'SUSPENDED', 'CLOSED')),
    CHECK (max_users > 0),
    CHECK (max_storage_gb > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='テナント（企業・組織）管理テーブル';

-- デフォルトテナントを作成（既存データの移行用）
INSERT INTO tenants (name, slug, plan_type, status, max_users, max_storage_gb, contact_email)
VALUES ('Default Organization', 'default', 'FREE', 'ACTIVE', 100, 10, 'admin@example.com');

-- テナント招待テーブル（将来の機能用）
CREATE TABLE IF NOT EXISTS tenant_invitations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL COMMENT '招待先テナント',
    email VARCHAR(100) NOT NULL COMMENT '招待先メールアドレス',
    role VARCHAR(20) NOT NULL DEFAULT 'USER' COMMENT '招待ロール',
    invite_code VARCHAR(64) NOT NULL UNIQUE COMMENT '招待コード',
    invited_by BIGINT COMMENT '招待者ユーザーID',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '招待ステータス',
    expires_at TIMESTAMP NOT NULL COMMENT '有効期限',
    accepted_at TIMESTAMP NULL COMMENT '承認日時',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE,
    INDEX idx_tenant (tenant_id),
    INDEX idx_email (email),
    INDEX idx_invite_code (invite_code),
    INDEX idx_status (status),
    INDEX idx_expires_at (expires_at),
    CHECK (role IN ('TENANT_ADMIN', 'ORG_ADMIN', 'MODERATOR', 'USER')),
    CHECK (status IN ('PENDING', 'ACCEPTED', 'EXPIRED', 'CANCELLED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='テナント招待管理テーブル';

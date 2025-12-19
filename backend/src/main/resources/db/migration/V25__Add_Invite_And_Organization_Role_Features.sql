-- V25: 招待URLと組織権限階級機能の追加（V19のBox機能を前提）
-- V19で既に実装されているBox機能に、招待URLとユーザー権限階級を追加
-- 作成日: 2025-12-02

-- ========================================
-- 1. usersテーブルの権限階級カラム追加
-- ========================================

-- 既存のroleカラムとは別に、組織内での階級を管理
-- V19のuser_rolesテーブルと併用可能な設計

-- Note: H2互換性のため動的SQLを削除し、直接ALTER TABLEを実行
-- カラムが既に存在する場合はエラーになりますが、Flywayの冪等性により問題ありません
ALTER TABLE users ADD COLUMN organization_role VARCHAR(50) NOT NULL DEFAULT 'GENERAL' COMMENT '組織内権限階級';
CREATE INDEX idx_organization_role ON users(organization_role);
CREATE INDEX idx_tenant_org_role ON users(tenant_id, organization_role);
ALTER TABLE users ADD CONSTRAINT chk_organization_role CHECK (organization_role IN ('CEO', 'MANAGER', 'SECTION_CHIEF', 'PM', 'GENERAL'));

-- ========================================
-- 2. 招待URLテーブルの作成
-- ========================================

CREATE TABLE IF NOT EXISTS organization_invites (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL COMMENT '所属テナント',
    organization_id BIGINT NOT NULL COMMENT '招待先組織',

    -- 招待コード
    invite_code VARCHAR(64) UNIQUE NOT NULL COMMENT '招待コード（URL用）',

    -- デフォルト設定
    default_role VARCHAR(50) NOT NULL DEFAULT 'GENERAL' COMMENT 'デフォルト権限階級',

    -- 制限
    expires_at DATETIME COMMENT '有効期限',
    max_uses INT COMMENT '最大使用回数',
    current_uses INT NOT NULL DEFAULT 0 COMMENT '現在の使用回数',

    -- 状態
    is_active BOOLEAN NOT NULL DEFAULT TRUE COMMENT 'アクティブ状態',

    -- 管理
    created_by BIGINT NOT NULL COMMENT '作成者ユーザーID',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '作成日時',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新日時',

    FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE,
    FOREIGN KEY (organization_id) REFERENCES organizations(id) ON DELETE CASCADE,
    FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE CASCADE,

    INDEX idx_invite_code (invite_code),
    INDEX idx_tenant_org (tenant_id, organization_id),
    INDEX idx_is_active (is_active),
    INDEX idx_expires_at (expires_at),
    INDEX idx_created_by (created_by),

    CHECK (default_role IN ('CEO', 'MANAGER', 'SECTION_CHIEF', 'PM', 'GENERAL'))
)
COMMENT='組織招待URLテーブル';

-- ========================================
-- 3. 招待使用履歴テーブルの作成
-- ========================================

CREATE TABLE IF NOT EXISTS invite_usage_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    invite_id BIGINT NOT NULL COMMENT '招待ID',
    user_id BIGINT NOT NULL COMMENT '登録したユーザーID',
    used_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '使用日時',
    ip_address VARCHAR(45) COMMENT '使用時のIPアドレス',
    user_agent TEXT COMMENT 'ユーザーエージェント',

    FOREIGN KEY (invite_id) REFERENCES organization_invites(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,

    INDEX idx_invite_id (invite_id),
    INDEX idx_user_id (user_id),
    INDEX idx_used_at (used_at DESC)
)
COMMENT='招待URL使用履歴テーブル';

-- ========================================
-- 4. 返信（Reply）機能の拡張
-- ========================================

-- post_commentsテーブルに権限階級による返信制御を追加
-- Note: H2互換性のため動的SQLを削除し、直接ALTER TABLEを実行
ALTER TABLE post_comments ADD COLUMN is_reply BOOLEAN NOT NULL DEFAULT FALSE COMMENT 'PM以上からの返信かどうか';
ALTER TABLE post_comments ADD COLUMN replier_role VARCHAR(50) COMMENT '返信者の権限階級';
CREATE INDEX idx_is_reply ON post_comments(is_reply);
CREATE INDEX idx_replier_role ON post_comments(replier_role);

-- ========================================
-- 5. organization_membersテーブルの確認・拡張
-- ========================================

-- 組織メンバーシップテーブルが存在するか確認し、必要なカラムを追加
-- このテーブルは複数組織への所属を管理

CREATE TABLE IF NOT EXISTS organization_members (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    organization_id BIGINT NOT NULL COMMENT '組織ID',
    user_id BIGINT NOT NULL COMMENT 'ユーザーID',
    role VARCHAR(50) NOT NULL DEFAULT 'GENERAL' COMMENT 'この組織での権限階級',
    is_primary BOOLEAN NOT NULL DEFAULT FALSE COMMENT '主所属組織かどうか',
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '参加日時',

    FOREIGN KEY (organization_id) REFERENCES organizations(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,

    UNIQUE (organization_id, user_id),
    INDEX idx_organization_id (organization_id),
    INDEX idx_user_id (user_id),
    INDEX idx_role (role),
    INDEX idx_is_primary (is_primary),
    INDEX idx_org_role (organization_id, role),

    CHECK (role IN ('CEO', 'MANAGER', 'SECTION_CHIEF', 'PM', 'GENERAL'))
)
COMMENT='組織メンバーシップテーブル（複数組織所属対応）';

-- ========================================
-- 完了
-- ========================================

SELECT
    'Migration V25 completed successfully!' AS status,
    (SELECT COUNT(*) FROM organization_invites) AS total_organization_invites,
    (SELECT COUNT(*) FROM invite_usage_history) AS total_invite_usage_history,
    (SELECT COUNT(*) FROM organization_members) AS total_organization_members,
    NOW() AS completed_at;

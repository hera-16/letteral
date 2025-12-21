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
ALTER TABLE users ADD COLUMN organization_role VARCHAR(50) NOT NULL DEFAULT 'GENERAL';
CREATE INDEX idx_organization_role ON users(organization_role);
CREATE INDEX idx_tenant_org_role ON users(tenant_id, organization_role);
ALTER TABLE users ADD CONSTRAINT chk_organization_role CHECK (organization_role IN ('CEO', 'MANAGER', 'SECTION_CHIEF', 'PM', 'GENERAL'));

-- ========================================
-- 2. 招待URLテーブルの作成
-- ========================================

CREATE TABLE IF NOT EXISTS organization_invites (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    organization_id BIGINT NOT NULL,
    invite_code VARCHAR(64) UNIQUE NOT NULL,
    default_role VARCHAR(50) NOT NULL DEFAULT 'GENERAL',
    expires_at DATETIME,
    max_uses INT,
    current_uses INT NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_by BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE,
    FOREIGN KEY (organization_id) REFERENCES organizations(id) ON DELETE CASCADE,
    FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE CASCADE,

    CHECK (default_role IN ('CEO', 'MANAGER', 'SECTION_CHIEF', 'PM', 'GENERAL'))
);

CREATE INDEX idx_invite_code ON organization_invites(invite_code);
CREATE INDEX idx_tenant_org ON organization_invites(tenant_id, organization_id);
CREATE INDEX idx_org_invites_is_active ON organization_invites(is_active);
CREATE INDEX idx_org_invites_expires_at ON organization_invites(expires_at);
CREATE INDEX idx_org_invites_created_by ON organization_invites(created_by);

-- ========================================
-- 3. 招待使用履歴テーブルの作成
-- ========================================

CREATE TABLE IF NOT EXISTS invite_usage_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    invite_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    used_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ip_address VARCHAR(45),
    user_agent TEXT,

    FOREIGN KEY (invite_id) REFERENCES organization_invites(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_invite_usage_invite_id ON invite_usage_history(invite_id);
CREATE INDEX idx_invite_usage_user_id ON invite_usage_history(user_id);
CREATE INDEX idx_invite_usage_used_at ON invite_usage_history(used_at DESC);

-- ========================================
-- 4. 返信（Reply）機能の拡張
-- ========================================

-- post_commentsテーブルに権限階級による返信制御を追加
-- Note: H2互換性のため動的SQLを削除し、直接ALTER TABLEを実行
ALTER TABLE post_comments ADD COLUMN is_reply BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE post_comments ADD COLUMN replier_role VARCHAR(50);
CREATE INDEX idx_post_comments_is_reply ON post_comments(is_reply);
CREATE INDEX idx_replier_role ON post_comments(replier_role);

-- ========================================
-- 5. organization_membersテーブルの確認・拡張
-- ========================================

-- 組織メンバーシップテーブルが存在するか確認し、必要なカラムを追加
-- このテーブルは複数組織への所属を管理

CREATE TABLE IF NOT EXISTS organization_members (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    organization_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    role VARCHAR(50) NOT NULL DEFAULT 'GENERAL',
    is_primary BOOLEAN NOT NULL DEFAULT FALSE,
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (organization_id) REFERENCES organizations(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,

    UNIQUE (organization_id, user_id),

    CHECK (role IN ('CEO', 'MANAGER', 'SECTION_CHIEF', 'PM', 'GENERAL'))
);

CREATE INDEX idx_org_members_organization_id ON organization_members(organization_id);
CREATE INDEX idx_org_members_user_id ON organization_members(user_id);
CREATE INDEX idx_org_members_role ON organization_members(role);
CREATE INDEX idx_org_members_is_primary ON organization_members(is_primary);
CREATE INDEX idx_org_members_org_role ON organization_members(organization_id, role);

-- ========================================
-- 完了
-- ========================================

SELECT
    'Migration V25 completed successfully!' AS status,
    (SELECT COUNT(*) FROM organization_invites) AS total_organization_invites,
    (SELECT COUNT(*) FROM invite_usage_history) AS total_invite_usage_history,
    (SELECT COUNT(*) FROM organization_members) AS total_organization_members,
    NOW() AS completed_at;

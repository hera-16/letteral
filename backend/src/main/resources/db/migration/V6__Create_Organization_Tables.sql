-- V6: 組織階層管理テーブルの作成
-- 会社→本部→部→課→チーム のような階層構造をサポート
-- 作成日: 2025-11-07

-- 組織階層テーブル
CREATE TABLE IF NOT EXISTS organizations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL COMMENT '所属テナント',
    parent_id BIGINT COMMENT '親組織（NULL = ルート）',
    name VARCHAR(200) NOT NULL COMMENT '組織名',
    organization_type VARCHAR(50) COMMENT '組織タイプ',
    description TEXT COMMENT '組織説明',
    level INT NOT NULL DEFAULT 1 COMMENT '階層レベル（1=ルート）',
    path VARCHAR(1000) COMMENT '階層パス（例: /1/3/7）',
    display_order INT NOT NULL DEFAULT 0 COMMENT '表示順序',
    is_active BOOLEAN NOT NULL DEFAULT TRUE COMMENT 'アクティブ状態',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE,
    FOREIGN KEY (parent_id) REFERENCES organizations(id) ON DELETE CASCADE,
    INDEX idx_tenant (tenant_id),
    INDEX idx_parent (parent_id),
    INDEX idx_path (path(255)),
    INDEX idx_level (level),
    INDEX idx_is_active (is_active),
    INDEX idx_tenant_active (tenant_id, is_active),
    INDEX idx_tenant_parent (tenant_id, parent_id),
    INDEX idx_display_order (display_order)
)
COMMENT='組織階層テーブル';

-- デフォルトテナントのルート組織を作成
INSERT INTO organizations (tenant_id, parent_id, name, organization_type, level, path)
VALUES (1, NULL, 'Default Organization', 'COMPANY', 1, '/1');

-- 組織メンバーシップテーブル
CREATE TABLE IF NOT EXISTS organization_members (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL COMMENT '所属テナント',
    organization_id BIGINT NOT NULL COMMENT '所属組織',
    user_id BIGINT NOT NULL COMMENT 'ユーザーID',
    role VARCHAR(20) NOT NULL DEFAULT 'MEMBER' COMMENT '組織内ロール',
    is_primary BOOLEAN NOT NULL DEFAULT FALSE COMMENT '主所属かどうか',
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE,
    FOREIGN KEY (organization_id) REFERENCES organizations(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE (organization_id, user_id),
    INDEX idx_tenant (tenant_id),
    INDEX idx_organization (organization_id),
    INDEX idx_user (user_id),
    INDEX idx_role (role),
    INDEX idx_tenant_user (tenant_id, user_id),
    INDEX idx_is_primary (is_primary),
    CHECK (role IN ('OWNER', 'ADMIN', 'MODERATOR', 'MEMBER'))
)
COMMENT='組織メンバーシップテーブル';

-- 匿名プロフィールテーブル
CREATE TABLE IF NOT EXISTS anonymous_profiles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL COMMENT '所属テナント',
    organization_id BIGINT NOT NULL COMMENT '組織ごとに異なる匿名ID',
    user_id BIGINT NOT NULL COMMENT '実ユーザー（暗号化推奨）',
    anonymous_id VARCHAR(100) NOT NULL COMMENT '匿名識別子',
    display_name VARCHAR(100) COMMENT '表示名（仮名モード用）',
    avatar_url VARCHAR(500) COMMENT 'アバター画像URL',
    anonymity_level VARCHAR(20) NOT NULL DEFAULT 'PSEUDONYM' COMMENT '匿名レベル',
    bio TEXT COMMENT '自己紹介（匿名）',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE,
    FOREIGN KEY (organization_id) REFERENCES organizations(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE (organization_id, user_id),
    UNIQUE (tenant_id, anonymous_id),
    INDEX idx_tenant (tenant_id),
    INDEX idx_organization (organization_id),
    INDEX idx_user (user_id),
    INDEX idx_anonymous_id (anonymous_id),
    INDEX idx_anonymity_level (anonymity_level),
    CHECK (anonymity_level IN ('FULL', 'PSEUDONYM', 'REAL'))
)
COMMENT='匿名プロフィールテーブル';

-- ポリシー設定テーブル
CREATE TABLE IF NOT EXISTS policy_settings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL COMMENT '所属テナント',
    organization_id BIGINT NOT NULL COMMENT '対象組織',

    -- 匿名性ポリシー
    default_anonymity_level VARCHAR(20) NOT NULL DEFAULT 'PSEUDONYM' COMMENT 'デフォルト匿名レベル',
    allow_full_anonymous BOOLEAN NOT NULL DEFAULT TRUE COMMENT '完全匿名を許可',
    allow_real_name BOOLEAN NOT NULL DEFAULT TRUE COMMENT '実名を許可',

    -- 公開範囲ポリシー
    default_visibility VARCHAR(20) NOT NULL DEFAULT 'ORGANIZATION' COMMENT 'デフォルト公開範囲',
    allow_company_wide BOOLEAN NOT NULL DEFAULT FALSE COMMENT '全社公開を許可',

    -- 投稿制限
    require_approval BOOLEAN NOT NULL DEFAULT FALSE COMMENT '投稿承認必須',
    min_post_length INT NOT NULL DEFAULT 10 COMMENT '最小投稿文字数',
    max_post_length INT NOT NULL DEFAULT 5000 COMMENT '最大投稿文字数',

    -- モデレーション
    enable_ng_word_filter BOOLEAN NOT NULL DEFAULT TRUE COMMENT 'NGワードフィルタ有効',
    enable_auto_moderation BOOLEAN NOT NULL DEFAULT TRUE COMMENT '自動モデレーション有効',
    ng_words JSON COMMENT 'NGワード辞書',

    -- その他
    settings JSON COMMENT 'カスタム設定',

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE,
    FOREIGN KEY (organization_id) REFERENCES organizations(id) ON DELETE CASCADE,
    UNIQUE (organization_id),
    INDEX idx_tenant (tenant_id),
    INDEX idx_organization (organization_id),
    CHECK (default_anonymity_level IN ('FULL', 'PSEUDONYM', 'REAL')),
    CHECK (default_visibility IN ('PRIVATE', 'TEAM', 'DEPARTMENT', 'ORGANIZATION', 'COMPANY')),
    CHECK (min_post_length > 0),
    CHECK (max_post_length >= min_post_length)
)
COMMENT='組織別ポリシー設定テーブル';

-- デフォルト組織のポリシーを作成
INSERT INTO policy_settings (tenant_id, organization_id, default_anonymity_level, default_visibility)
VALUES (1, 1, 'PSEUDONYM', 'ORGANIZATION');

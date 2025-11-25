-- V7: 進捗投稿関連テーブルの作成
-- 日々の目標・進捗・課題を記録し、リアクション機能をサポート
-- 作成日: 2025-11-07

-- 進捗投稿テーブル
CREATE TABLE IF NOT EXISTS progress_posts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL COMMENT '所属テナント',
    organization_id BIGINT NOT NULL COMMENT '投稿元組織',
    author_id BIGINT NOT NULL COMMENT '投稿者（匿名プロフィールID）',
    is_anonymous BOOLEAN NOT NULL DEFAULT TRUE COMMENT '匿名投稿かどうか',

    -- 投稿内容
    post_type VARCHAR(20) NOT NULL DEFAULT 'PROGRESS' COMMENT '投稿タイプ',
    title VARCHAR(200) COMMENT 'タイトル',
    content TEXT NOT NULL COMMENT '本文',
    achievement_rate INT COMMENT '達成率（0-100）',
    blockers TEXT COMMENT 'ブロッカー（障害・課題）',
    learnings TEXT COMMENT '学び・気づき',
    next_action TEXT COMMENT '次の一手',

    -- 公開範囲
    visibility VARCHAR(20) NOT NULL DEFAULT 'ORGANIZATION' COMMENT '公開範囲',
    target_organization_id BIGINT COMMENT '公開先組織',

    -- メタデータ
    post_date DATE NOT NULL COMMENT '投稿対象日',
    tags JSON COMMENT 'タグ（配列）',
    attachments JSON COMMENT '添付ファイル（配列）',

    -- 統計
    reaction_count INT NOT NULL DEFAULT 0 COMMENT 'リアクション数',
    comment_count INT NOT NULL DEFAULT 0 COMMENT 'コメント数',
    view_count INT NOT NULL DEFAULT 0 COMMENT '閲覧数',

    -- 管理
    is_pinned BOOLEAN NOT NULL DEFAULT FALSE COMMENT 'ピン留め',
    is_archived BOOLEAN NOT NULL DEFAULT FALSE COMMENT 'アーカイブ済み',

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE,
    FOREIGN KEY (organization_id) REFERENCES organizations(id) ON DELETE CASCADE,
    FOREIGN KEY (author_id) REFERENCES anonymous_profiles(id) ON DELETE CASCADE,
    FOREIGN KEY (target_organization_id) REFERENCES organizations(id) ON DELETE SET NULL,
    INDEX idx_tenant (tenant_id),
    INDEX idx_organization (organization_id),
    INDEX idx_author (author_id),
    INDEX idx_post_date (post_date),
    INDEX idx_post_type (post_type),
    INDEX idx_visibility (visibility),
    INDEX idx_is_archived (is_archived),
    INDEX idx_tenant_date (tenant_id, post_date DESC),
    INDEX idx_org_date (organization_id, post_date DESC),
    INDEX idx_tenant_org_date (tenant_id, organization_id, post_date DESC),
    INDEX idx_author_date (author_id, post_date DESC),
    CHECK (post_type IN ('PROGRESS', 'GOAL', 'BLOCKER', 'LEARNING', 'REFLECTION')),
    CHECK (visibility IN ('PRIVATE', 'TEAM', 'DEPARTMENT', 'ORGANIZATION', 'COMPANY')),
    CHECK (achievement_rate IS NULL OR (achievement_rate >= 0 AND achievement_rate <= 100))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='進捗投稿テーブル';

-- 投稿リアクションテーブル
CREATE TABLE IF NOT EXISTS post_reactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL COMMENT '所属テナント',
    post_id BIGINT NOT NULL COMMENT '対象投稿',
    user_id BIGINT NOT NULL COMMENT 'リアクションユーザー',
    is_anonymous BOOLEAN NOT NULL DEFAULT TRUE COMMENT '匿名リアクション',

    reaction_type VARCHAR(20) NOT NULL COMMENT 'リアクションタイプ',
    comment TEXT COMMENT 'コメント（任意）',

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE,
    FOREIGN KEY (post_id) REFERENCES progress_posts(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY uk_post_user_reaction (post_id, user_id, reaction_type),
    INDEX idx_tenant (tenant_id),
    INDEX idx_post (post_id),
    INDEX idx_user (user_id),
    INDEX idx_reaction_type (reaction_type),
    INDEX idx_tenant_post (tenant_id, post_id),
    INDEX idx_created_at (created_at DESC),
    CHECK (reaction_type IN ('PRAISE', 'EMPATHY', 'SUPPORT', 'QUESTION', 'HEART', 'THUMBS_UP', 'FIRE', 'CLAP'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='投稿リアクションテーブル';

-- 投稿コメントテーブル
CREATE TABLE IF NOT EXISTS post_comments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL COMMENT '所属テナント',
    post_id BIGINT NOT NULL COMMENT '対象投稿',
    author_id BIGINT NOT NULL COMMENT 'コメント投稿者（匿名プロフィールID）',
    parent_comment_id BIGINT COMMENT '親コメント（スレッド化）',
    is_anonymous BOOLEAN NOT NULL DEFAULT TRUE COMMENT '匿名コメント',

    content TEXT NOT NULL COMMENT 'コメント内容',

    -- 統計
    reaction_count INT NOT NULL DEFAULT 0 COMMENT 'リアクション数',

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE,
    FOREIGN KEY (post_id) REFERENCES progress_posts(id) ON DELETE CASCADE,
    FOREIGN KEY (author_id) REFERENCES anonymous_profiles(id) ON DELETE CASCADE,
    FOREIGN KEY (parent_comment_id) REFERENCES post_comments(id) ON DELETE CASCADE,
    INDEX idx_tenant (tenant_id),
    INDEX idx_post (post_id),
    INDEX idx_author (author_id),
    INDEX idx_parent (parent_comment_id),
    INDEX idx_post_created (post_id, created_at DESC),
    INDEX idx_created_at (created_at DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='投稿コメントテーブル';

-- 投稿閲覧履歴テーブル（統計用）
CREATE TABLE IF NOT EXISTS post_views (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL COMMENT '所属テナント',
    post_id BIGINT NOT NULL COMMENT '対象投稿',
    user_id BIGINT NOT NULL COMMENT '閲覧ユーザー',
    viewed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE,
    FOREIGN KEY (post_id) REFERENCES progress_posts(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY uk_post_user_view (post_id, user_id),
    INDEX idx_tenant (tenant_id),
    INDEX idx_post (post_id),
    INDEX idx_user (user_id),
    INDEX idx_viewed_at (viewed_at DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='投稿閲覧履歴テーブル';

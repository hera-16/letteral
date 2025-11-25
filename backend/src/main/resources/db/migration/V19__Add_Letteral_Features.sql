-- V19: Letteral固有機能の追加
-- 権限階級、Box種別、権限昇格ルール、Box別閲覧権限制御
-- 作成日: 2025-11-14

-- =====================================================
-- 1. 権限階級（Role Hierarchy）テーブル
-- =====================================================
-- 社長 > 部長 > 課長 > PM > 一般 の階層構造を定義
CREATE TABLE IF NOT EXISTS role_hierarchy (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL COMMENT '所属テナント',
    role_name VARCHAR(50) NOT NULL COMMENT '役職名',
    role_level INT NOT NULL COMMENT '権限レベル（数字が大きいほど上位）',
    display_name VARCHAR(100) NOT NULL COMMENT '表示名',
    description TEXT COMMENT '説明',

    -- 権限昇格条件
    min_posts_required INT NOT NULL DEFAULT 0 COMMENT '昇格に必要な最小投稿数',
    min_days_active INT NOT NULL DEFAULT 0 COMMENT '昇格に必要な最小活動日数',
    requires_approval BOOLEAN NOT NULL DEFAULT FALSE COMMENT '承認が必要かどうか',

    -- Box閲覧権限（下位のレベルまで閲覧可能）
    can_view_down_to_level INT NOT NULL DEFAULT 0 COMMENT '閲覧可能な下位レベル',

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE,
    UNIQUE KEY uk_tenant_role_name (tenant_id, role_name),
    UNIQUE KEY uk_tenant_role_level (tenant_id, role_level),
    INDEX idx_tenant (tenant_id),
    INDEX idx_role_level (role_level),
    INDEX idx_tenant_level (tenant_id, role_level)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='権限階級定義テーブル';

-- =====================================================
-- 2. デフォルト権限階級データの挿入
-- =====================================================
INSERT INTO role_hierarchy (tenant_id, role_name, role_level, display_name, description, min_posts_required, min_days_active, requires_approval, can_view_down_to_level)
VALUES
    (1, 'PRESIDENT', 100, '社長', '全社の最高責任者。全てのBoxを閲覧可能。', 0, 0, TRUE, 0),
    (1, 'DIRECTOR', 80, '部長', '部門の責任者。部門以下のBoxを閲覧可能。', 50, 90, TRUE, 60),
    (1, 'MANAGER', 60, '課長', '課の責任者。課以下のBoxを閲覧可能。', 30, 60, TRUE, 40),
    (1, 'PM', 40, 'プロジェクトマネージャー', 'プロジェクトの責任者。プロジェクト内のBoxを閲覧可能。', 20, 30, FALSE, 20),
    (1, 'MEMBER', 20, '一般メンバー', '一般的なメンバー。自分のBoxのみ閲覧可能。', 0, 0, FALSE, 20);

-- =====================================================
-- 3. ユーザー権限階級テーブル
-- =====================================================
-- ユーザーごとに権限階級を割り当て
CREATE TABLE IF NOT EXISTS user_roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL COMMENT '所属テナント',
    user_id BIGINT NOT NULL COMMENT 'ユーザー',
    organization_id BIGINT NOT NULL COMMENT '所属組織',
    role_name VARCHAR(50) NOT NULL COMMENT '役職名',
    role_level INT NOT NULL COMMENT '権限レベル',

    -- 統計情報
    total_posts INT NOT NULL DEFAULT 0 COMMENT '総投稿数',
    days_active INT NOT NULL DEFAULT 0 COMMENT '活動日数',

    -- 承認状態
    approval_status VARCHAR(20) NOT NULL DEFAULT 'APPROVED' COMMENT '承認状態',
    approved_by BIGINT COMMENT '承認者',
    approved_at TIMESTAMP NULL COMMENT '承認日時',

    assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '割り当て日時',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (organization_id) REFERENCES organizations(id) ON DELETE CASCADE,
    FOREIGN KEY (approved_by) REFERENCES users(id) ON DELETE SET NULL,
    UNIQUE KEY uk_user_org_role (user_id, organization_id),
    INDEX idx_tenant (tenant_id),
    INDEX idx_user (user_id),
    INDEX idx_organization (organization_id),
    INDEX idx_role_level (role_level),
    INDEX idx_approval_status (approval_status),
    INDEX idx_tenant_org (tenant_id, organization_id),
    CHECK (approval_status IN ('PENDING', 'APPROVED', 'REJECTED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='ユーザー権限階級テーブル';

-- =====================================================
-- 4. Box種別（Box Type）テーブル
-- =====================================================
-- 投稿Boxの種類を定義（上司Box、部下Box、同僚Box、PMBox など）
CREATE TABLE IF NOT EXISTS box_types (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL COMMENT '所属テナント',
    box_name VARCHAR(50) NOT NULL COMMENT 'Box名',
    display_name VARCHAR(100) NOT NULL COMMENT '表示名',
    description TEXT COMMENT '説明',

    -- 閲覧権限設定
    min_role_level_to_view INT NOT NULL DEFAULT 20 COMMENT '閲覧可能な最小権限レベル',
    is_hierarchical BOOLEAN NOT NULL DEFAULT TRUE COMMENT '階層的閲覧かどうか',

    -- 投稿権限設定
    min_role_level_to_post INT NOT NULL DEFAULT 20 COMMENT '投稿可能な最小権限レベル',

    -- Box種別フラグ
    is_system_box BOOLEAN NOT NULL DEFAULT FALSE COMMENT 'システム標準Box',
    is_active BOOLEAN NOT NULL DEFAULT TRUE COMMENT 'アクティブ状態',

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE,
    UNIQUE KEY uk_tenant_box_name (tenant_id, box_name),
    INDEX idx_tenant (tenant_id),
    INDEX idx_is_active (is_active),
    INDEX idx_tenant_active (tenant_id, is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Box種別定義テーブル';

-- =====================================================
-- 5. デフォルトBox種別データの挿入
-- =====================================================
INSERT INTO box_types (tenant_id, box_name, display_name, description, min_role_level_to_view, is_hierarchical, min_role_level_to_post, is_system_box, is_active)
VALUES
    (1, 'SUPERIOR_BOX', '上司Box', '上司に報告する内容を投稿するBox。部下の投稿も閲覧可能。', 40, TRUE, 20, TRUE, TRUE),
    (1, 'SUBORDINATE_BOX', '部下Box', '部下からの報告を受けるBox。上司のみ閲覧可能。', 40, TRUE, 20, TRUE, TRUE),
    (1, 'PEER_BOX', '同僚Box', '同じ階層のメンバー間で共有するBox。', 20, FALSE, 20, TRUE, TRUE),
    (1, 'PM_BOX', 'PMBox', 'プロジェクトマネージャーが管理するBox。', 40, TRUE, 20, TRUE, TRUE),
    (1, 'TEAM_BOX', 'チームBox', 'チーム全体で共有するBox。', 20, FALSE, 20, TRUE, TRUE),
    (1, 'PERSONAL_BOX', '個人Box', '個人専用のBox。本人のみ閲覧可能。', 20, FALSE, 20, TRUE, TRUE);

-- =====================================================
-- 6. 進捗投稿にBox種別カラムを追加
-- =====================================================
ALTER TABLE progress_posts
ADD COLUMN box_type_id BIGINT COMMENT 'Box種別' AFTER organization_id,
ADD COLUMN parent_post_id BIGINT COMMENT '親投稿（返信の場合）' AFTER box_type_id,
ADD COLUMN is_reply BOOLEAN NOT NULL DEFAULT FALSE COMMENT '返信かどうか' AFTER parent_post_id,
ADD COLUMN reply_depth INT NOT NULL DEFAULT 0 COMMENT '返信の深さ（0=親投稿）' AFTER is_reply;

ALTER TABLE progress_posts
ADD CONSTRAINT fk_posts_box_type
    FOREIGN KEY (box_type_id) REFERENCES box_types(id) ON DELETE SET NULL,
ADD CONSTRAINT fk_posts_parent
    FOREIGN KEY (parent_post_id) REFERENCES progress_posts(id) ON DELETE CASCADE;

ALTER TABLE progress_posts
ADD INDEX idx_box_type (box_type_id),
ADD INDEX idx_parent_post (parent_post_id),
ADD INDEX idx_is_reply (is_reply),
ADD INDEX idx_box_date (box_type_id, post_date DESC),
ADD INDEX idx_parent_created (parent_post_id, created_at DESC);

-- =====================================================
-- 7. Box閲覧権限テーブル
-- =====================================================
-- ユーザーがどのBoxを閲覧できるかを管理
CREATE TABLE IF NOT EXISTS box_permissions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL COMMENT '所属テナント',
    user_id BIGINT NOT NULL COMMENT 'ユーザー',
    box_type_id BIGINT NOT NULL COMMENT 'Box種別',
    organization_id BIGINT NOT NULL COMMENT '組織',

    -- 権限設定
    can_view BOOLEAN NOT NULL DEFAULT FALSE COMMENT '閲覧可能',
    can_post BOOLEAN NOT NULL DEFAULT FALSE COMMENT '投稿可能',
    can_reply BOOLEAN NOT NULL DEFAULT FALSE COMMENT '返信可能',
    can_moderate BOOLEAN NOT NULL DEFAULT FALSE COMMENT 'モデレート可能',

    -- 条件設定
    granted_by VARCHAR(50) NOT NULL DEFAULT 'AUTO' COMMENT '付与方法（AUTO, MANUAL）',
    granted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '付与日時',
    expires_at TIMESTAMP NULL COMMENT '有効期限',

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (box_type_id) REFERENCES box_types(id) ON DELETE CASCADE,
    FOREIGN KEY (organization_id) REFERENCES organizations(id) ON DELETE CASCADE,
    UNIQUE KEY uk_user_box_org (user_id, box_type_id, organization_id),
    INDEX idx_tenant (tenant_id),
    INDEX idx_user (user_id),
    INDEX idx_box_type (box_type_id),
    INDEX idx_organization (organization_id),
    INDEX idx_tenant_user (tenant_id, user_id),
    INDEX idx_expires_at (expires_at),
    CHECK (granted_by IN ('AUTO', 'MANUAL', 'SYSTEM'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Box閲覧権限テーブル';

-- =====================================================
-- 8. 権限昇格申請テーブル
-- =====================================================
-- ユーザーが上位権限への昇格を申請する
CREATE TABLE IF NOT EXISTS role_promotion_requests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL COMMENT '所属テナント',
    user_id BIGINT NOT NULL COMMENT '申請者',
    organization_id BIGINT NOT NULL COMMENT '対象組織',

    -- 昇格情報
    current_role_level INT NOT NULL COMMENT '現在の権限レベル',
    requested_role_level INT NOT NULL COMMENT '申請する権限レベル',
    requested_role_name VARCHAR(50) NOT NULL COMMENT '申請する役職名',

    -- 申請情報
    reason TEXT COMMENT '申請理由',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '申請状態',

    -- 承認情報
    reviewed_by BIGINT COMMENT '承認者',
    reviewed_at TIMESTAMP NULL COMMENT '承認日時',
    review_comment TEXT COMMENT '承認コメント',

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (organization_id) REFERENCES organizations(id) ON DELETE CASCADE,
    FOREIGN KEY (reviewed_by) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_tenant (tenant_id),
    INDEX idx_user (user_id),
    INDEX idx_organization (organization_id),
    INDEX idx_status (status),
    INDEX idx_tenant_status (tenant_id, status),
    INDEX idx_created_at (created_at DESC),
    CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED', 'CANCELLED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='権限昇格申請テーブル';

-- =====================================================
-- 9. 既存ユーザーにデフォルト権限を割り当て
-- =====================================================
INSERT INTO user_roles (tenant_id, user_id, organization_id, role_name, role_level, total_posts, days_active, approval_status)
SELECT
    u.tenant_id,
    u.id,
    u.primary_organization_id,
    CASE
        WHEN u.role = 'TENANT_ADMIN' THEN 'PRESIDENT'
        WHEN u.role = 'ORG_ADMIN' THEN 'DIRECTOR'
        WHEN u.role = 'MODERATOR' THEN 'MANAGER'
        ELSE 'MEMBER'
    END,
    CASE
        WHEN u.role = 'TENANT_ADMIN' THEN 100
        WHEN u.role = 'ORG_ADMIN' THEN 80
        WHEN u.role = 'MODERATOR' THEN 60
        ELSE 20
    END,
    0,
    0,
    'APPROVED'
FROM users u
WHERE u.tenant_id IS NOT NULL
AND u.primary_organization_id IS NOT NULL
AND NOT EXISTS (
    SELECT 1 FROM user_roles ur
    WHERE ur.user_id = u.id
    AND ur.organization_id = u.primary_organization_id
);

-- =====================================================
-- 10. 既存ユーザーにデフォルトBox権限を付与
-- =====================================================
INSERT INTO box_permissions (tenant_id, user_id, box_type_id, organization_id, can_view, can_post, can_reply, granted_by)
SELECT
    ur.tenant_id,
    ur.user_id,
    bt.id,
    ur.organization_id,
    TRUE,
    TRUE,
    TRUE,
    'SYSTEM'
FROM user_roles ur
CROSS JOIN box_types bt
WHERE bt.tenant_id = ur.tenant_id
AND bt.is_active = TRUE
AND ur.role_level >= bt.min_role_level_to_post
AND NOT EXISTS (
    SELECT 1 FROM box_permissions bp
    WHERE bp.user_id = ur.user_id
    AND bp.box_type_id = bt.id
    AND bp.organization_id = ur.organization_id
);

-- =====================================================
-- 11. 返信数カウント用のカラムを追加
-- =====================================================
ALTER TABLE progress_posts
ADD COLUMN reply_count INT NOT NULL DEFAULT 0 COMMENT '返信数' AFTER comment_count;

-- =====================================================
-- 12. 返信数カウント更新用トリガー
-- =====================================================
DELIMITER //

CREATE TRIGGER after_post_reply_insert
AFTER INSERT ON progress_posts
FOR EACH ROW
BEGIN
    IF NEW.parent_post_id IS NOT NULL THEN
        UPDATE progress_posts
        SET reply_count = reply_count + 1
        WHERE id = NEW.parent_post_id;
    END IF;
END//

CREATE TRIGGER after_post_reply_delete
AFTER DELETE ON progress_posts
FOR EACH ROW
BEGIN
    IF OLD.parent_post_id IS NOT NULL THEN
        UPDATE progress_posts
        SET reply_count = reply_count - 1
        WHERE id = OLD.parent_post_id AND reply_count > 0;
    END IF;
END//

DELIMITER ;

-- =====================================================
-- 13. 権限レベル確認用ビュー
-- =====================================================
CREATE OR REPLACE VIEW v_user_permissions AS
SELECT
    u.id AS user_id,
    u.username,
    u.display_name,
    ur.organization_id,
    o.name AS organization_name,
    ur.role_name,
    ur.role_level,
    rh.display_name AS role_display_name,
    ur.total_posts,
    ur.days_active,
    ur.approval_status,
    COUNT(DISTINCT bp.box_type_id) AS accessible_boxes_count
FROM users u
LEFT JOIN user_roles ur ON u.id = ur.user_id
LEFT JOIN organizations o ON ur.organization_id = o.id
LEFT JOIN role_hierarchy rh ON ur.tenant_id = rh.tenant_id AND ur.role_name = rh.role_name
LEFT JOIN box_permissions bp ON u.id = bp.user_id AND bp.can_view = TRUE
GROUP BY u.id, u.username, u.display_name, ur.organization_id, o.name, ur.role_name, ur.role_level, rh.display_name, ur.total_posts, ur.days_active, ur.approval_status;

-- =====================================================
-- 14. Box閲覧権限確認用ビュー
-- =====================================================
CREATE OR REPLACE VIEW v_box_access_matrix AS
SELECT
    u.id AS user_id,
    u.username,
    ur.role_name,
    ur.role_level,
    bt.box_name,
    bt.display_name AS box_display_name,
    bp.can_view,
    bp.can_post,
    bp.can_reply,
    bp.granted_by
FROM users u
INNER JOIN user_roles ur ON u.id = ur.user_id
LEFT JOIN box_permissions bp ON u.id = bp.user_id
LEFT JOIN box_types bt ON bp.box_type_id = bt.id
ORDER BY u.id, ur.role_level DESC, bt.box_name;

-- =====================================================
-- マイグレーション完了メッセージ
-- =====================================================
SELECT
    'Migration V19 completed successfully!' AS status,
    (SELECT COUNT(*) FROM role_hierarchy) AS total_role_hierarchies,
    (SELECT COUNT(*) FROM user_roles) AS total_user_roles,
    (SELECT COUNT(*) FROM box_types) AS total_box_types,
    (SELECT COUNT(*) FROM box_permissions) AS total_box_permissions,
    NOW() AS completed_at;

-- V12: 既存データの移行とクリーンアップ
-- デフォルトテナント・組織への紐付け、既存機能の互換性確保
-- 作成日: 2025-11-07

-- =====================================================
-- 1. friendsテーブルにtenant_idを追加
-- =====================================================
ALTER TABLE friends
ADD COLUMN tenant_id BIGINT AFTER id,
ADD COLUMN updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP AFTER responded_at;

-- カラムにコメントを追加
ALTER TABLE friends
MODIFY COLUMN tenant_id BIGINT COMMENT '所属テナント',
MODIFY COLUMN updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新日時';

ALTER TABLE friends
ADD CONSTRAINT fk_friends_tenant
    FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE;

ALTER TABLE friends
ADD INDEX idx_tenant (tenant_id),
ADD INDEX idx_tenant_requester (tenant_id, requester_id),
ADD INDEX idx_tenant_addressee (tenant_id, addressee_id);

-- 既存フレンドデータをデフォルトテナントに紐付け
UPDATE friends
SET tenant_id = 1
WHERE tenant_id IS NULL;

ALTER TABLE friends
MODIFY COLUMN tenant_id BIGINT NOT NULL COMMENT '所属テナント';

-- =====================================================
-- 2. group_membersテーブルにtenant_idを追加
-- =====================================================
ALTER TABLE group_members
ADD COLUMN tenant_id BIGINT AFTER id;

-- カラムにコメントを追加
ALTER TABLE group_members
MODIFY COLUMN tenant_id BIGINT COMMENT '所属テナント';

ALTER TABLE group_members
ADD CONSTRAINT fk_group_members_tenant
    FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE;

ALTER TABLE group_members
ADD INDEX idx_tenant (tenant_id);

-- 既存グループメンバーデータをデフォルトテナントに紐付け
UPDATE group_members
SET tenant_id = 1
WHERE tenant_id IS NULL;

ALTER TABLE group_members
MODIFY COLUMN tenant_id BIGINT NOT NULL COMMENT '所属テナント';

-- =====================================================
-- 3. topicsテーブルにtenant_idを追加
-- =====================================================
ALTER TABLE topics
ADD COLUMN tenant_id BIGINT AFTER id,
ADD COLUMN organization_id BIGINT AFTER tenant_id,
ADD COLUMN updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP AFTER created_at;

-- カラムにコメントを追加
ALTER TABLE topics
MODIFY COLUMN tenant_id BIGINT COMMENT '所属テナント',
MODIFY COLUMN organization_id BIGINT COMMENT '所属組織',
MODIFY COLUMN updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新日時';

ALTER TABLE topics
ADD CONSTRAINT fk_topics_tenant
    FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE,
ADD CONSTRAINT fk_topics_organization
    FOREIGN KEY (organization_id) REFERENCES organizations(id) ON DELETE SET NULL;

ALTER TABLE topics
ADD INDEX idx_tenant (tenant_id),
ADD INDEX idx_organization (organization_id),
ADD INDEX idx_tenant_category (tenant_id, category),
ADD INDEX idx_tenant_active (tenant_id, is_active);

-- 既存トピックデータをデフォルトテナントに紐付け
UPDATE topics
SET tenant_id = 1,
    organization_id = 1
WHERE tenant_id IS NULL;

ALTER TABLE topics
MODIFY COLUMN tenant_id BIGINT NOT NULL COMMENT '所属テナント';

-- =====================================================
-- 4. 既存ユーザーの匿名プロフィールを生成
-- =====================================================
INSERT INTO anonymous_profiles (tenant_id, organization_id, user_id, anonymous_id, display_name, anonymity_level)
SELECT
    u.tenant_id,
    u.primary_organization_id,
    u.id,
    CONCAT('anonymous_', LPAD(u.id, 6, '0')),
    CONCAT('匿名ユーザー', LPAD(u.id, 4, '0')),
    'PSEUDONYM'
FROM users u
WHERE NOT EXISTS (
    SELECT 1
    FROM anonymous_profiles ap
    WHERE ap.user_id = u.id AND ap.organization_id = u.primary_organization_id
);

-- =====================================================
-- 5. 既存ユーザーをデフォルト組織のメンバーとして追加
-- =====================================================
INSERT INTO organization_members (tenant_id, organization_id, user_id, role, is_primary)
SELECT
    u.tenant_id,
    u.primary_organization_id,
    u.id,
    CASE
        WHEN u.role = 'TENANT_ADMIN' THEN 'ADMIN'
        WHEN u.role = 'ORG_ADMIN' THEN 'ADMIN'
        WHEN u.role = 'MODERATOR' THEN 'MODERATOR'
        ELSE 'MEMBER'
    END,
    TRUE
FROM users u
WHERE u.primary_organization_id IS NOT NULL
AND NOT EXISTS (
    SELECT 1
    FROM organization_members om
    WHERE om.user_id = u.id AND om.organization_id = u.primary_organization_id
);

-- =====================================================
-- 6. デフォルトテナントの管理者設定
-- =====================================================
-- 最初に作成されたユーザーをテナント管理者に設定
UPDATE users
SET role = 'TENANT_ADMIN'
WHERE id = (SELECT MIN(id) FROM (SELECT id FROM users) AS temp)
AND role = 'USER';

-- =====================================================
-- 7. データ整合性チェック用ビュー作成
-- =====================================================
CREATE OR REPLACE VIEW v_data_consistency_check AS
SELECT
    'Users without tenant' AS check_type,
    COUNT(*) AS count
FROM users
WHERE tenant_id IS NULL

UNION ALL

SELECT
    'Users without organization' AS check_type,
    COUNT(*) AS count
FROM users
WHERE primary_organization_id IS NULL

UNION ALL

SELECT
    'Groups without tenant' AS check_type,
    COUNT(*) AS count
FROM groups_table
WHERE tenant_id IS NULL

UNION ALL

SELECT
    'Messages without tenant' AS check_type,
    COUNT(*) AS count
FROM chat_messages
WHERE tenant_id IS NULL

UNION ALL

SELECT
    'Users without anonymous profile' AS check_type,
    COUNT(*) AS count
FROM users u
WHERE NOT EXISTS (
    SELECT 1 FROM anonymous_profiles ap
    WHERE ap.user_id = u.id
);

-- =====================================================
-- 8. 統計情報の初期化
-- =====================================================
-- 進捗投稿のリアクション数を0で初期化（トリガーで自動更新）
-- 既存チャットメッセージのリアクション数も0で初期化

-- =====================================================
-- 9. マイグレーション完了確認
-- =====================================================
-- データ整合性チェックビューを確認
SELECT * FROM v_data_consistency_check;

-- 期待される結果: すべてのcountが0であること

-- =====================================================
-- 10. 不要なカラムの削除（将来のバージョンで実施）
-- =====================================================
-- デイリーチャレンジ、バッジ関連のテーブルは保持
-- 今後の方針決定後に削除または転用を検討

-- マイグレーション完了メッセージ
SELECT
    'Migration V12 completed successfully!' AS status,
    (SELECT COUNT(*) FROM tenants) AS total_tenants,
    (SELECT COUNT(*) FROM organizations) AS total_organizations,
    (SELECT COUNT(*) FROM users) AS total_users,
    (SELECT COUNT(*) FROM anonymous_profiles) AS total_anonymous_profiles,
    (SELECT COUNT(*) FROM organization_members) AS total_org_members,
    NOW() AS completed_at;

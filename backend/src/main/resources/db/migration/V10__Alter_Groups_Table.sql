-- V10: groups_tableテーブルの拡張
-- テナント管理、組織紐付け、公開範囲、匿名ポリシーなどの追加
-- 作成日: 2025-11-07

-- groups_tableテーブルにカラムを追加
ALTER TABLE groups_table
ADD COLUMN tenant_id BIGINT AFTER id COMMENT '所属テナント',
ADD COLUMN organization_id BIGINT AFTER tenant_id COMMENT '所属組織',
ADD COLUMN visibility VARCHAR(20) NOT NULL DEFAULT 'ORGANIZATION' AFTER group_type COMMENT '公開範囲',
ADD COLUMN anonymity_policy VARCHAR(20) NOT NULL DEFAULT 'PSEUDONYM' AFTER visibility COMMENT '匿名ポリシー',
ADD COLUMN settings JSON AFTER max_members COMMENT 'グループ設定',
ADD COLUMN is_active BOOLEAN NOT NULL DEFAULT TRUE AFTER created_at COMMENT 'アクティブ状態',
ADD COLUMN archived_at TIMESTAMP NULL AFTER is_active COMMENT 'アーカイブ日時',
ADD COLUMN updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP AFTER archived_at COMMENT '更新日時';

-- 外部キー制約を追加
ALTER TABLE groups_table
ADD CONSTRAINT fk_groups_tenant
    FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE,
ADD CONSTRAINT fk_groups_organization
    FOREIGN KEY (organization_id) REFERENCES organizations(id) ON DELETE SET NULL;

-- インデックスを追加
ALTER TABLE groups_table
ADD INDEX idx_tenant (tenant_id),
ADD INDEX idx_organization (organization_id),
ADD INDEX idx_visibility (visibility),
ADD INDEX idx_anonymity_policy (anonymity_policy),
ADD INDEX idx_is_active (is_active),
ADD INDEX idx_tenant_type (tenant_id, group_type),
ADD INDEX idx_tenant_active (tenant_id, is_active),
ADD INDEX idx_org_active (organization_id, is_active);

-- CHECK制約を追加
ALTER TABLE groups_table
ADD CONSTRAINT chk_group_visibility
    CHECK (visibility IN ('PRIVATE', 'TEAM', 'DEPARTMENT', 'ORGANIZATION', 'COMPANY', 'PUBLIC')),
ADD CONSTRAINT chk_group_anonymity_policy
    CHECK (anonymity_policy IN ('FULL', 'PSEUDONYM', 'REAL', 'FLEXIBLE'));

-- 既存グループのデータを更新（デフォルトテナントに紐付け）
UPDATE groups_table
SET tenant_id = 1,
    organization_id = 1
WHERE tenant_id IS NULL;

-- tenant_idをNOT NULLに変更
ALTER TABLE groups_table
MODIFY COLUMN tenant_id BIGINT NOT NULL COMMENT '所属テナント';

-- V10: groups_tableテーブルの拡張
-- テナント管理、組織紐付け、公開範囲、匿名ポリシーなどの追加
-- 作成日: 2025-11-07

-- groups_tableテーブルにカラムを追加
-- H2ではAFTER句は使用できないため、順番に追加
ALTER TABLE groups_table ADD COLUMN tenant_id BIGINT;
ALTER TABLE groups_table ADD COLUMN organization_id BIGINT;
ALTER TABLE groups_table ADD COLUMN visibility VARCHAR(20) NOT NULL DEFAULT 'ORGANIZATION';
ALTER TABLE groups_table ADD COLUMN anonymity_policy VARCHAR(20) NOT NULL DEFAULT 'PSEUDONYM';
ALTER TABLE groups_table ADD COLUMN settings VARCHAR(4000);
ALTER TABLE groups_table ADD COLUMN is_active BOOLEAN NOT NULL DEFAULT TRUE;
ALTER TABLE groups_table ADD COLUMN archived_at TIMESTAMP NULL;
ALTER TABLE groups_table ADD COLUMN updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

-- 外部キー制約を追加
ALTER TABLE groups_table
ADD CONSTRAINT fk_groups_tenant
    FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE;

ALTER TABLE groups_table
ADD CONSTRAINT fk_groups_organization
    FOREIGN KEY (organization_id) REFERENCES organizations(id) ON DELETE SET NULL;

-- インデックスを追加
CREATE INDEX idx_tenant ON groups_table(tenant_id);
CREATE INDEX idx_organization ON groups_table(organization_id);
CREATE INDEX idx_visibility ON groups_table(visibility);
CREATE INDEX idx_anonymity_policy ON groups_table(anonymity_policy);
CREATE INDEX idx_is_active ON groups_table(is_active);
CREATE INDEX idx_tenant_type ON groups_table(tenant_id, group_type);
CREATE INDEX idx_tenant_active ON groups_table(tenant_id, is_active);
CREATE INDEX idx_org_active ON groups_table(organization_id, is_active);

-- CHECK制約を追加
ALTER TABLE groups_table
ADD CONSTRAINT chk_group_visibility
    CHECK (visibility IN ('PRIVATE', 'TEAM', 'DEPARTMENT', 'ORGANIZATION', 'COMPANY', 'PUBLIC'));

ALTER TABLE groups_table
ADD CONSTRAINT chk_group_anonymity_policy
    CHECK (anonymity_policy IN ('FULL', 'PSEUDONYM', 'REAL', 'FLEXIBLE'));

-- 既存グループのデータを更新（デフォルトテナントに紐付け）
UPDATE groups_table
SET tenant_id = 1,
    organization_id = 1
WHERE tenant_id IS NULL;

-- tenant_idをNOT NULLに変更
ALTER TABLE groups_table ALTER COLUMN tenant_id SET NOT NULL;

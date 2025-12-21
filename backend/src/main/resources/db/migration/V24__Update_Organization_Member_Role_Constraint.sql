-- V24: organization_membersテーブルのroleカラムCHECK制約を更新
-- OrganizationRole enumに合わせて拡張された役割を許可する
-- 作成日: 2025-11-21

-- H2互換の制約削除構文
-- 注意: 制約名はinformation_schemaから取得して動的に削除する必要があるが、
-- シンプルにするため、既知の制約名を直接指定する

-- 既存のCHECK制約を削除
ALTER TABLE organization_members DROP CONSTRAINT IF EXISTS organization_members_chk_1;

-- 新しいCHECK制約を追加（OrganizationRole enumの全値を許可）
ALTER TABLE organization_members
ADD CONSTRAINT chk_org_member_role
    CHECK (role IN ('OWNER', 'ADMIN_ROOT', 'ADMIN_CORE', 'ADMIN_LEAD', 'ADMIN_SUPER', 'MEMBER', 'ADMIN', 'MODERATOR'));

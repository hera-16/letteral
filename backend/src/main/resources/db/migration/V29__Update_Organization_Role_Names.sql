-- 組織ロール名を新しい命名規則に更新
-- 実際のデータベースの値から新しいEnum値へマッピング
-- PM -> ADMIN_SUPER (PM - チーム管理)
-- SECTION_CHIEF -> ADMIN_LEAD (課長 - 課管理)
-- GENERAL -> MEMBER (メンバー - 閲覧・投稿のみ)

UPDATE organization_members SET role = 'ADMIN_SUPER' WHERE role = 'PM';
UPDATE organization_members SET role = 'ADMIN_LEAD' WHERE role = 'SECTION_CHIEF';
UPDATE organization_members SET role = 'MEMBER' WHERE role = 'GENERAL';

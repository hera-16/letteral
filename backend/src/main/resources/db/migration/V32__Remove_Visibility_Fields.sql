-- V32: Visibility関連フィールドの削除
-- 組織階層ベースのアクセス制御に移行するため、Visibilityフィールドを削除

-- progress_postsテーブルからvisibilityカラムを削除
ALTER TABLE progress_posts DROP COLUMN visibility;

-- policy_settingsテーブルからdefault_visibilityカラムを削除
ALTER TABLE policy_settings DROP COLUMN default_visibility;

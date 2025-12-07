-- V31: 進捗投稿のauthor_idをusersテーブルに参照変更
-- anonymous_profilesテーブルからusersテーブルへの外部キー変更
-- 作成日: 2025-12-02

-- 1. 既存の外部キー制約を削除
ALTER TABLE progress_posts
DROP FOREIGN KEY progress_posts_ibfk_3;

-- 2. 新しい外部キー制約を追加（usersテーブルを参照）
ALTER TABLE progress_posts
ADD CONSTRAINT fk_posts_author
    FOREIGN KEY (author_id) REFERENCES users(id) ON DELETE CASCADE;

-- 3. インデックスの確認（既存のINDEX idx_authorが存在するため、新たに作成する必要はない）

-- マイグレーション完了メッセージ
SELECT
    'Migration V31 completed successfully!' AS status,
    'Progress posts now reference users table for author_id' AS description,
    NOW() AS completed_at;

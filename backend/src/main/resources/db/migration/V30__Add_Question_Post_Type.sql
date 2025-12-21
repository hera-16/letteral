-- V30: QUESTION投稿タイプを追加
-- 作成日: 2025-12-02
-- 目的: chk_progress_posts_type制約にQUESTION投稿タイプを追加

-- ========================================
-- 1. 既存のCHECK制約を削除
-- ========================================

ALTER TABLE progress_posts DROP CONSTRAINT chk_progress_posts_type;

-- ========================================
-- 2. 新しいCHECK制約を追加（QUESTIONを含む）
-- ========================================

ALTER TABLE progress_posts ADD CONSTRAINT chk_progress_posts_type
    CHECK (post_type IN ('PROGRESS', 'GOAL', 'BLOCKER', 'LEARNING', 'REFLECTION', 'QUESTION'));

-- ========================================
-- 完了
-- ========================================

SELECT
    'Migration V30 completed successfully!' AS status,
    'Added QUESTION post type to chk_progress_posts_type constraint' AS description,
    NOW() AS completed_at;

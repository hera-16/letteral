-- V30: QUESTION投稿タイプを追加
-- 作成日: 2025-12-02
-- 目的: progress_posts_chk_1制約にQUESTION投稿タイプを追加

-- ========================================
-- 1. 既存のCHECK制約を削除
-- ========================================

ALTER TABLE progress_posts DROP CHECK progress_posts_chk_1;

-- ========================================
-- 2. 新しいCHECK制約を追加（QUESTIONを含む）
-- ========================================

ALTER TABLE progress_posts ADD CONSTRAINT progress_posts_chk_1
    CHECK (post_type IN ('PROGRESS', 'GOAL', 'BLOCKER', 'LEARNING', 'REFLECTION', 'QUESTION'));

-- ========================================
-- 完了
-- ========================================

SELECT
    'Migration V30 completed successfully!' AS status,
    'Added QUESTION post type to progress_posts_chk_1 constraint' AS description,
    NOW() AS completed_at;

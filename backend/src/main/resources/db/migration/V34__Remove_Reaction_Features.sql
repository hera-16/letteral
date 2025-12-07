-- V34: 投稿のリアクション機能を削除

-- post_reactionsテーブルを削除
DROP TABLE IF EXISTS post_reactions;

-- progress_postsテーブルからreaction_countカラムを削除(既に削除済み)
-- ALTER TABLE progress_posts DROP COLUMN reaction_count;

-- chat_messagesテーブルからreaction_countカラムを削除(既に削除済み)
-- ALTER TABLE chat_messages DROP COLUMN reaction_count;

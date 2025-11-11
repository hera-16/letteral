-- デイリーチャレンジシステムの削除: チャレンジシェア関連テーブル
-- 企業向けプラットフォームへの移行のため、個人向けチャレンジ機能を削除

-- 外部キー制約を持つテーブルから削除
DROP TABLE IF EXISTS challenge_share_read_status;
DROP TABLE IF EXISTS challenge_share_reactions;
DROP TABLE IF EXISTS challenge_shares;

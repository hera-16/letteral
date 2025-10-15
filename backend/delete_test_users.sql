-- 既存のユーザーデータを削除するSQLスクリプト
-- 実行日: 2025年10月15日
-- 目的: 新規登録機能のエンドポイント修正前のテストデータを削除

-- 1. まず関連データを削除(外部キー制約のため)

-- フレンドシップを削除
DELETE FROM friendships;

-- グループメンバーを削除
DELETE FROM group_members;

-- メッセージを削除
DELETE FROM messages;

-- チャレンジ完了履歴を削除
DELETE FROM challenge_completions;

-- ユーザーバッジを削除
DELETE FROM user_badges;

-- グループを削除
DELETE FROM chat_groups;

-- 2. 最後にユーザーを削除
DELETE FROM users;

-- 3. AUTO_INCREMENTをリセット
ALTER TABLE users AUTO_INCREMENT = 1;
ALTER TABLE friendships AUTO_INCREMENT = 1;
ALTER TABLE chat_groups AUTO_INCREMENT = 1;
ALTER TABLE messages AUTO_INCREMENT = 1;
ALTER TABLE challenge_completions AUTO_INCREMENT = 1;
ALTER TABLE user_badges AUTO_INCREMENT = 1;

-- 確認用クエリ
SELECT 'Users deleted successfully!' AS Status;
SELECT COUNT(*) AS remaining_users FROM users;

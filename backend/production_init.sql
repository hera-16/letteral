-- ======================================
-- 本番環境用データベース初期化スクリプト
-- ======================================
-- 作成日: 2025年10月15日
-- 用途: Railway/PlanetScale/RDS用の初期セットアップ
-- 実行タイミング: 本番デプロイ前の1回のみ

-- ======================================
-- 1. データベースとユーザーの作成 (RDSの場合)
-- ======================================
-- Railway/PlanetScaleの場合はスキップ (自動作成)

-- CREATE DATABASE IF NOT EXISTS chatapp CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
-- CREATE USER IF NOT EXISTS 'chatapp_user'@'%' IDENTIFIED BY 'CHANGE_THIS_PASSWORD';
-- GRANT ALL PRIVILEGES ON chatapp.* TO 'chatapp_user'@'%';
-- FLUSH PRIVILEGES;
-- USE chatapp;

-- ======================================
-- 2. テーブル作成は Spring Boot が自動実行
-- ======================================
-- spring.jpa.hibernate.ddl-auto=update により
-- 以下のテーブルが自動作成されます:
-- - users
-- - friendships
-- - chat_groups
-- - group_members
-- - messages
-- - challenges
-- - challenge_completions
-- - badges
-- - user_badges

-- ======================================
-- 3. 初期データの投入 (オプション)
-- ======================================

-- デフォルトチャレンジの作成
INSERT INTO challenges (title, description, category, difficulty, points, is_active, created_at) VALUES
('朝の瞑想', '5分間の瞑想をする', 'HEALTH', 'EASY', 10, true, NOW()),
('読書タイム', '本を30ページ読む', 'LEARNING', 'EASY', 10, true, NOW()),
('水分補給', '水を2リットル飲む', 'HEALTH', 'EASY', 10, true, NOW()),
('運動', '30分間のウォーキング', 'HEALTH', 'MEDIUM', 20, true, NOW()),
('新しいスキル', '新しいことを1つ学ぶ', 'LEARNING', 'MEDIUM', 20, true, NOW()),
('感謝日記', '今日の感謝を3つ書く', 'MINDFULNESS', 'EASY', 10, true, NOW()),
('スクワット', 'スクワット50回', 'HEALTH', 'HARD', 30, true, NOW()),
('早起き', '朝6時に起きる', 'LIFESTYLE', 'MEDIUM', 20, true, NOW()),
('デジタルデトックス', 'SNSを1時間見ない', 'MINDFULNESS', 'MEDIUM', 20, true, NOW()),
('健康的な食事', '野菜を5種類食べる', 'HEALTH', 'MEDIUM', 20, true, NOW())
ON DUPLICATE KEY UPDATE title=VALUES(title);

-- デフォルトバッジの作成
INSERT INTO badges (name, description, icon_url, requirement_type, requirement_count, points, created_at) VALUES
('初心者', '最初のチャレンジ達成', '🎯', 'CHALLENGES_COMPLETED', 1, 10, NOW()),
('継続の力', '3日連続でチャレンジ達成', '🔥', 'CONSECUTIVE_DAYS', 3, 30, NOW()),
('週末戦士', '1週間で10チャレンジ達成', '⚔️', 'CHALLENGES_COMPLETED', 10, 50, NOW()),
('健康マスター', '健康系チャレンジ20回達成', '💪', 'HEALTH_CHALLENGES', 20, 100, NOW()),
('学習者', '学習系チャレンジ15回達成', '📚', 'LEARNING_CHALLENGES', 15, 80, NOW()),
('マインドフル', 'マインドフルネス系10回達成', '🧘', 'MINDFULNESS_CHALLENGES', 10, 60, NOW()),
('チャレンジャー', '合計50チャレンジ達成', '🏆', 'CHALLENGES_COMPLETED', 50, 200, NOW()),
('伝説', '合計100チャレンジ達成', '👑', 'CHALLENGES_COMPLETED', 100, 500, NOW()),
('完璧主義者', '1日に5チャレンジ達成', '✨', 'DAILY_CHALLENGES', 5, 100, NOW()),
('早起き達人', '早起きチャレンジ30回達成', '🌅', 'LIFESTYLE_CHALLENGES', 30, 150, NOW())
ON DUPLICATE KEY UPDATE name=VALUES(name);

-- ======================================
-- 4. インデックスの最適化
-- ======================================
-- Spring Bootが作成するインデックスに加えて、パフォーマンス最適化用

-- ユーザー検索の高速化
CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_created_at ON users(created_at);

-- フレンドシップ検索の高速化
CREATE INDEX IF NOT EXISTS idx_friendships_status ON friendships(status);
CREATE INDEX IF NOT EXISTS idx_friendships_requester ON friendships(requester_id);
CREATE INDEX IF NOT EXISTS idx_friendships_addressee ON friendships(addressee_id);

-- メッセージ検索の高速化
CREATE INDEX IF NOT EXISTS idx_messages_room_timestamp ON messages(room_id, timestamp);
CREATE INDEX IF NOT EXISTS idx_messages_sender ON messages(sender_id);

-- チャレンジ完了履歴の高速化
CREATE INDEX IF NOT EXISTS idx_completions_user_date ON challenge_completions(user_id, completed_at);
CREATE INDEX IF NOT EXISTS idx_completions_challenge ON challenge_completions(challenge_id);

-- ======================================
-- 5. 確認クエリ
-- ======================================
SELECT 'Database initialized successfully!' AS Status;
SELECT COUNT(*) AS challenge_count FROM challenges;
SELECT COUNT(*) AS badge_count FROM badges;
SELECT VERSION() AS mysql_version;

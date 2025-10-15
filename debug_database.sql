-- デバッグ: データベースの状態を確認するSQLスクリプト

USE chatapp;

-- 1. チャレンジマスターデータの確認
SELECT COUNT(*) as total_challenges FROM daily_challenges WHERE is_active = true;
SELECT id, title, challenge_type FROM daily_challenges WHERE is_active = true;

-- 2. ユーザー情報の確認
SELECT id, username, email FROM users;

-- 3. 今日の達成記録を確認（DATE関数を使用）
SELECT 
    cc.id,
    cc.user_id,
    u.username,
    dc.title as challenge_title,
    cc.completed_at,
    DATE(cc.completed_at) as completed_date,
    CURDATE() as today
FROM challenge_completions cc
JOIN users u ON cc.user_id = u.id
JOIN daily_challenges dc ON cc.challenge_id = dc.id
ORDER BY cc.completed_at DESC
LIMIT 20;

-- 4. 今日の達成数を確認
SELECT 
    u.username,
    COUNT(*) as today_completed
FROM challenge_completions cc
JOIN users u ON cc.user_id = u.id
WHERE DATE(cc.completed_at) = CURDATE()
GROUP BY u.id, u.username;

-- 5. 全達成記録の確認
SELECT COUNT(*) as total_completions FROM challenge_completions;

-- ============================================
-- 問題解決: データをリセットする場合
-- ============================================

-- オプション1: 今日の達成記録のみを削除
-- DELETE FROM challenge_completions WHERE DATE(completed_at) = CURDATE();

-- オプション2: 全ての達成記録を削除（初回テスト用）
-- DELETE FROM challenge_completions;
-- DELETE FROM user_badges;
-- DELETE FROM user_progress;

-- オプション3: バッジのみリセット（達成記録は残す）
-- DELETE FROM user_badges;

-- 確認: データが削除されたか確認
-- SELECT COUNT(*) FROM challenge_completions;
-- SELECT COUNT(*) FROM user_badges;
-- SELECT COUNT(*) FROM user_progress;

-- data.sql: Spring Bootが起動時に自動実行するSQL
-- 初期チャレンジデータを投入（テーブルが既に存在する場合のみ）

-- 既存データがあれば更新、なければ挿入
INSERT INTO daily_challenges (id, title, description, points, challenge_type, difficulty_level, is_active) VALUES
-- 感謝系チャレンジ
(1, '今日の良かったこと3つ', '今日あった良いことを3つ書き出してみましょう。小さなことでもOK！', 10, 'GRATITUDE', 'EASY', true),
(2, '感謝の気持ちを伝える', '誰か1人に「ありがとう」と伝えてみましょう', 15, 'GRATITUDE', 'MEDIUM', true),
(3, '今週のベストモーメント', '今週で一番良かった瞬間を振り返ってみましょう', 15, 'GRATITUDE', 'MEDIUM', true),

-- 優しさ系チャレンジ
(4, '誰かを褒める', 'グループで誰かを褒めるメッセージを送ってみましょう', 15, 'KINDNESS', 'EASY', true),
(5, '励ましメッセージを送る', '頑張っている人に励ましの言葉をかけてみましょう', 15, 'KINDNESS', 'MEDIUM', true),
(6, '共感コメント', '誰かの投稿に共感するコメントを残しましょう', 10, 'KINDNESS', 'EASY', true),

-- セルフケア系チャレンジ
(7, '深呼吸を5回', 'ゆっくり深呼吸を5回して、リラックスしましょう', 10, 'SELF_CARE', 'EASY', true),
(8, '好きな音楽を聴く', '好きな曲を1曲聴いて気分転換しましょう', 10, 'SELF_CARE', 'EASY', true),
(9, '5分間休憩', 'スマホを置いて、5分間目を閉じて休憩しましょう', 10, 'SELF_CARE', 'EASY', true),
(10, '自分を褒める', '今日頑張った自分を1つ褒めてあげましょう', 15, 'SELF_CARE', 'MEDIUM', true),

-- 創造性系チャレンジ
(11, '3行日記', '今日の出来事を3行で書いてみましょう', 10, 'CREATIVITY', 'EASY', true),
(12, '好きなものを描く', '簡単なイラストや落書きをしてみましょう', 15, 'CREATIVITY', 'MEDIUM', true),
(13, '今日の空の写真', '空を見上げて、写真を撮ってみましょう', 10, 'CREATIVITY', 'EASY', true),

-- つながり系チャレンジ
(14, 'グループで挨拶', 'グループチャットで「おはよう」や「おやすみ」を送ってみましょう', 10, 'CONNECTION', 'EASY', true),
(15, '質問を投げかける', 'グループで質問を1つ投げかけて、会話のきっかけを作りましょう', 15, 'CONNECTION', 'MEDIUM', true),
(16, '誰かの話を聞く', 'グループで誰かの話にしっかり耳を傾けてみましょう', 15, 'CONNECTION', 'MEDIUM', true)
ON DUPLICATE KEY UPDATE
    title = VALUES(title),
    description = VALUES(description),
    points = VALUES(points),
    challenge_type = VALUES(challenge_type),
    difficulty_level = VALUES(difficulty_level),
    is_active = VALUES(is_active);

-- バッジマスターテーブル
CREATE TABLE IF NOT EXISTS badges (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT NOT NULL,
    badge_type VARCHAR(50) NOT NULL,
    icon VARCHAR(10) NOT NULL,
    requirement_value INT NOT NULL DEFAULT 1,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_badge_type (badge_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ユーザーバッジ獲得記録テーブル
CREATE TABLE IF NOT EXISTS user_badges (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    badge_id BIGINT NOT NULL,
    earned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_new BOOLEAN DEFAULT TRUE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (badge_id) REFERENCES badges(id) ON DELETE CASCADE,
    UNIQUE KEY uk_user_badge (user_id, badge_id),
    INDEX idx_user_earned (user_id, earned_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 初期バッジデータの投入
INSERT INTO badges (name, description, badge_type, icon, requirement_value) VALUES
('初めての一歩', '最初のチャレンジを達成しました！すごい！', 'FIRST_STEP', '🌱', 1),
('3日連続', '3日連続でチャレンジ達成！継続は力なり！', 'STREAK_3', '🔥', 3),
('1週間連続', '7日連続達成！素晴らしい習慣です！', 'STREAK_7', '⭐', 7),
('10回達成', '合計10回のチャレンジ達成！成長していますね！', 'TOTAL_10', '🎯', 10),
('30回達成', '合計30回達成！もう習慣になっていますね！', 'TOTAL_30', '🏆', 30),
('50回達成', '合計50回達成！驚異的な継続力です！', 'TOTAL_50', '👑', 50),
('感謝マスター', '感謝系チャレンジを10回達成！感謝の心が育っています！', 'GRATITUDE_10', '💝', 10),
('優しさの達人', '優しさ系チャレンジを10回達成！あなたの優しさが世界を変えます！', 'KINDNESS_10', '🤝', 10),
('セルフケア上手', 'セルフケア系チャレンジを10回達成！自分を大切にしていますね！', 'SELF_CARE_10', '🧘', 10),
('クリエイター', '創造性チャレンジを10回達成！あなたの創造力が光っています！', 'CREATIVITY_10', '🎨', 10),
('コミュニケーター', 'つながり系チャレンジを10回達成！素敵な関係を築いていますね！', 'CONNECTION_10', '🌈', 10),
('花レベル3', '花レベル3到達！着実に成長していますね！', 'LEVEL_3', '🌺', 3),
('花レベル5', '花レベル5到達！半分まで来ました！', 'LEVEL_5', '🌻', 5),
('花レベル7', '花レベル7到達！もうすぐ最高レベルです！', 'LEVEL_7', '🌹', 7),
('花レベル10', '花レベル10到達！最高の花が咲きました！', 'LEVEL_10', '🏵️', 10)
ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    description = VALUES(description),
    icon = VALUES(icon),
    requirement_value = VALUES(requirement_value);


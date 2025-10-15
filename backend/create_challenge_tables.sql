-- デイリーチャレンジシステムのテーブルを手動作成

-- チャレンジマスターテーブル
CREATE TABLE IF NOT EXISTS daily_challenges (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    description TEXT NOT NULL,
    points INT NOT NULL DEFAULT 10,
    challenge_type VARCHAR(50) NOT NULL,
    difficulty_level VARCHAR(20) NOT NULL DEFAULT 'EASY',
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_challenge_type (challenge_type),
    INDEX idx_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ユーザー進捗テーブル
CREATE TABLE IF NOT EXISTS user_progress (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    total_points INT NOT NULL DEFAULT 0,
    flower_level INT NOT NULL DEFAULT 1,
    current_streak INT NOT NULL DEFAULT 0,
    longest_streak INT NOT NULL DEFAULT 0,
    last_challenge_date DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY uk_user_progress (user_id),
    INDEX idx_flower_level (flower_level),
    INDEX idx_streak (current_streak)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- チャレンジ達成記録テーブル
CREATE TABLE IF NOT EXISTS challenge_completions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    challenge_id BIGINT NOT NULL,
    completed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    points_earned INT NOT NULL DEFAULT 10,
    note TEXT,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (challenge_id) REFERENCES daily_challenges(id) ON DELETE CASCADE,
    INDEX idx_user_completed (user_id, completed_at),
    INDEX idx_challenge (challenge_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 初期チャレンジデータの投入
INSERT INTO daily_challenges (title, description, points, challenge_type, difficulty_level) VALUES
-- 感謝系チャレンジ
('今日の良かったこと3つ', '今日あった良いことを3つ書き出してみましょう。小さなことでもOK！', 10, 'GRATITUDE', 'EASY'),
('感謝の気持ちを伝える', '誰か1人に「ありがとう」と伝えてみましょう', 15, 'GRATITUDE', 'MEDIUM'),
('今週のベストモーメント', '今週で一番良かった瞬間を振り返ってみましょう', 15, 'GRATITUDE', 'MEDIUM'),

-- 優しさ系チャレンジ
('誰かを褒める', 'グループで誰かを褒めるメッセージを送ってみましょう', 15, 'KINDNESS', 'EASY'),
('励ましメッセージを送る', '頑張っている人に励ましの言葉をかけてみましょう', 15, 'KINDNESS', 'MEDIUM'),
('共感コメント', '誰かの投稿に共感するコメントを残しましょう', 10, 'KINDNESS', 'EASY'),

-- セルフケア系チャレンジ
('深呼吸を5回', 'ゆっくり深呼吸を5回して、リラックスしましょう', 10, 'SELF_CARE', 'EASY'),
('好きな音楽を聴く', '好きな曲を1曲聴いて気分転換しましょう', 10, 'SELF_CARE', 'EASY'),
('5分間休憩', 'スマホを置いて、5分間目を閉じて休憩しましょう', 10, 'SELF_CARE', 'EASY'),
('自分を褒める', '今日頑張った自分を1つ褒めてあげましょう', 15, 'SELF_CARE', 'MEDIUM'),

-- 創造性系チャレンジ
('3行日記', '今日の出来事を3行で書いてみましょう', 10, 'CREATIVITY', 'EASY'),
('好きなものを描く', '簡単なイラストや落書きをしてみましょう', 15, 'CREATIVITY', 'MEDIUM'),
('今日の空の写真', '空を見上げて、写真を撮ってみましょう', 10, 'CREATIVITY', 'EASY'),

-- つながり系チャレンジ
('グループで挨拶', 'グループチャットで「おはよう」や「おやすみ」を送ってみましょう', 10, 'CONNECTION', 'EASY'),
('質問を投げかける', 'グループで質問を1つ投げかけて、会話のきっかけを作りましょう', 15, 'CONNECTION', 'MEDIUM'),
('誰かの話を聞く', 'グループで誰かの話にしっかり耳を傾けてみましょう', 15, 'CONNECTION', 'MEDIUM');

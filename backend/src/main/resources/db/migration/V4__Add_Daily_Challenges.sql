-- デイリーチャレンジシステムのテーブル

-- チャレンジマスターテーブル
CREATE TABLE IF NOT EXISTS daily_challenges (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    description TEXT NOT NULL,
    points INT NOT NULL DEFAULT 10,
    challenge_type VARCHAR(50) NOT NULL, -- GRATITUDE, KINDNESS, SELF_CARE, CREATIVITY, CONNECTION
    difficulty_level VARCHAR(20) NOT NULL DEFAULT 'EASY', -- EASY, MEDIUM, HARD
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
    flower_level INT NOT NULL DEFAULT 1, -- 花の成長レベル（1-10）
    current_streak INT NOT NULL DEFAULT 0, -- 連続達成日数
    longest_streak INT NOT NULL DEFAULT 0, -- 最長連続日数
    last_challenge_date DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY unique_user_progress (user_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_flower_level (flower_level)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- チャレンジ達成記録テーブル
CREATE TABLE IF NOT EXISTS challenge_completions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    challenge_id BIGINT NOT NULL,
    completed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    points_earned INT NOT NULL,
    note TEXT, -- ユーザーのメモ（任意）
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (challenge_id) REFERENCES daily_challenges(id) ON DELETE CASCADE,
    INDEX idx_user_completions (user_id, completed_at),
    INDEX idx_challenge_completions (challenge_id, completed_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 初期チャレンジデータを投入
INSERT INTO daily_challenges (title, description, points, challenge_type, difficulty_level) VALUES
-- 感謝系チャレンジ
('今日の良かったこと3つ', '今日あった良かったことを3つ書き出してみましょう。小さなことでもOKです！', 10, 'GRATITUDE', 'EASY'),
('感謝の気持ちを伝える', '誰か一人に「ありがとう」を伝えてみましょう', 15, 'GRATITUDE', 'EASY'),
('今週のベストモーメント', '今週一番心に残った出来事を振り返りましょう', 20, 'GRATITUDE', 'MEDIUM'),

-- 優しさ系チャレンジ
('誰かを褒める', 'グループで誰かの良いところを見つけて褒めてみましょう', 15, 'KINDNESS', 'EASY'),
('励ましメッセージを送る', '落ち込んでいる人に励ましのメッセージを送りましょう', 20, 'KINDNESS', 'MEDIUM'),
('共感コメント', '誰かの投稿に共感のコメントをしてみましょう', 10, 'KINDNESS', 'EASY'),

-- セルフケア系チャレンジ
('深呼吸5回', 'ゆっくり深呼吸を5回してリラックスしましょう', 10, 'SELF_CARE', 'EASY'),
('好きな音楽を聴く', '好きな曲を一曲聴いて気分転換しましょう', 10, 'SELF_CARE', 'EASY'),
('5分間休憩', '目を閉じて5分間何も考えずに休憩しましょう', 15, 'SELF_CARE', 'EASY'),
('自分を褒める', '今日頑張った自分を3つ褒めてあげましょう', 15, 'SELF_CARE', 'MEDIUM'),

-- 創造性系チャレンジ
('3行日記', '今日の出来事を3行で書いてみましょう', 10, 'CREATIVITY', 'EASY'),
('好きなものを描く', '好きなものを簡単に描いてみましょう（下手でもOK！）', 20, 'CREATIVITY', 'MEDIUM'),
('今日の空の写真', '空を見上げて写真を撮ってみましょう', 15, 'CREATIVITY', 'EASY'),

-- つながり系チャレンジ
('グループで挨拶', 'グループで「おはよう」や「おやすみ」を言ってみましょう', 10, 'CONNECTION', 'EASY'),
('質問を投げかける', 'グループで簡単な質問をしてみましょう（好きな食べ物は？など）', 15, 'CONNECTION', 'EASY'),
('誰かの話を聞く', 'グループで誰かの話をじっくり聞いてあげましょう', 20, 'CONNECTION', 'MEDIUM');

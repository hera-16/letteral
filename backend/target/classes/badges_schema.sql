-- バッジシステムのテーブル追加

-- バッジマスターテーブル
CREATE TABLE IF NOT EXISTS badges (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT NOT NULL,
    badge_type VARCHAR(50) NOT NULL, -- FIRST_STEP, STREAK_3, TOTAL_10, GRATITUDE_10, KINDNESS_10, LEVEL_5, etc.
    icon VARCHAR(10) NOT NULL, -- 絵文字アイコン
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
    is_new BOOLEAN DEFAULT TRUE, -- 新しく獲得したバッジかどうか
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
('花レベル10', '花レベル10到達！最高の花が咲きました！', 'LEVEL_10', '🏵️', 10);

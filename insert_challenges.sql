-- このSQLをMySQL Workbenchで実行してください

USE chatapp;

-- 既存データを確認
SELECT COUNT(*), is_active FROM daily_challenges GROUP BY is_active;

-- 既存データを更新（is_activeをtrueに）
UPDATE daily_challenges SET is_active = true WHERE is_active IS NULL OR is_active = false;

-- データが0件の場合は挿入
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

-- 確認
SELECT id, title, is_active FROM daily_challenges ORDER BY id;
SELECT COUNT(*) as total_count FROM daily_challenges WHERE is_active = true;

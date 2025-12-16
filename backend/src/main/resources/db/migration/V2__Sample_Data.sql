-- Sample Data for Testing
-- Version 2.0

-- サンプルユーザーの追加
-- パスワードはすべて "password123" (BCrypt ハッシュ化済み)
-- BCrypt hash for "password123": $2a$10$YourHashHere (実際のハッシュを生成する必要があります)

INSERT INTO users (username, email, password, display_name, created_at) VALUES
('alice', 'alice@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Alice Wonderland', NOW()),
('bob', 'bob@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Bob Builder', NOW()),
('charlie', 'charlie@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Charlie Chaplin', NOW()),
('diana', 'diana@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Diana Prince', NOW()),
('eve', 'eve@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'Eve Online', NOW());

-- フレンド関係の追加
INSERT INTO friends (requester_id, addressee_id, status, requested_at, responded_at) VALUES
(1, 2, 'ACCEPTED', DATEADD('DAY', -7, NOW()), DATEADD('DAY', -7, NOW())),
(1, 3, 'ACCEPTED', DATEADD('DAY', -5, NOW()), DATEADD('DAY', -5, NOW())),
(2, 3, 'ACCEPTED', DATEADD('DAY', -3, NOW()), DATEADD('DAY', -3, NOW())),
(1, 4, 'PENDING', DATEADD('DAY', -1, NOW()), NULL),
(5, 1, 'PENDING', DATEADD('DAY', -2, NOW()), NULL);

-- 招待制グループの追加
INSERT INTO groups_table (name, description, group_type, invite_code, max_members, creator_id, created_at) VALUES
('Study Group', 'A group for studying together', 'INVITE_ONLY', 'STUDY123', 20, 1, DATEADD('DAY', -10, NOW())),
('Gaming Clan', 'For gaming enthusiasts', 'INVITE_ONLY', 'GAME456', 30, 2, DATEADD('DAY', -8, NOW())),
('Book Club', 'Monthly book discussions', 'INVITE_ONLY', 'BOOK789', 15, 3, DATEADD('DAY', -5, NOW()));

-- パブリックトピックグループの追加
INSERT INTO groups_table (name, description, group_type, invite_code, max_members, creator_id, created_at) VALUES
('General Chat', 'General discussions for everyone', 'PUBLIC_TOPIC', NULL, 100, 1, DATEADD('DAY', -15, NOW())),
('Tech Talk', 'Technology and programming discussions', 'PUBLIC_TOPIC', NULL, 100, 2, DATEADD('DAY', -12, NOW()));

-- グループメンバーの追加
-- Study Group members
INSERT INTO group_members (group_id, user_id, role, joined_at) VALUES
(1, 1, 'ADMIN', DATEADD('DAY', -10, NOW())),
(1, 2, 'MEMBER', DATEADD('DAY', -9, NOW())),
(1, 3, 'MEMBER', DATEADD('DAY', -8, NOW()));

-- Gaming Clan members
INSERT INTO group_members (group_id, user_id, role, joined_at) VALUES
(2, 2, 'ADMIN', DATEADD('DAY', -8, NOW())),
(2, 1, 'MEMBER', DATEADD('DAY', -7, NOW())),
(2, 4, 'MEMBER', DATEADD('DAY', -6, NOW()));

-- Book Club members
INSERT INTO group_members (group_id, user_id, role, joined_at) VALUES
(3, 3, 'ADMIN', DATEADD('DAY', -5, NOW())),
(3, 1, 'MEMBER', DATEADD('DAY', -4, NOW())),
(3, 5, 'MEMBER', DATEADD('DAY', -3, NOW()));

-- General Chat members
INSERT INTO group_members (group_id, user_id, role, joined_at) VALUES
(4, 1, 'ADMIN', DATEADD('DAY', -15, NOW())),
(4, 2, 'MEMBER', DATEADD('DAY', -14, NOW())),
(4, 3, 'MEMBER', DATEADD('DAY', -13, NOW())),
(4, 4, 'MEMBER', DATEADD('DAY', -12, NOW())),
(4, 5, 'MEMBER', DATEADD('DAY', -11, NOW()));

-- Tech Talk members
INSERT INTO group_members (group_id, user_id, role, joined_at) VALUES
(5, 2, 'ADMIN', DATEADD('DAY', -12, NOW())),
(5, 1, 'MEMBER', DATEADD('DAY', -11, NOW())),
(5, 3, 'MEMBER', DATEADD('DAY', -10, NOW()));

-- トピックの追加
INSERT INTO topics (name, description, category, creator_id, created_at, is_active) VALUES
('Spring Boot Tips', 'Share your Spring Boot development tips', 'Programming', 1, DATEADD('DAY', -20, NOW()), TRUE),
('React Best Practices', 'Discuss React development patterns', 'Programming', 2, DATEADD('DAY', -18, NOW()), TRUE),
('Database Design', 'Database architecture and optimization', 'Programming', 3, DATEADD('DAY', -15, NOW()), TRUE),
('Movie Recommendations', 'Share and discuss movies', 'Entertainment', 4, DATEADD('DAY', -12, NOW()), TRUE),
('Fitness Goals', 'Track and share fitness achievements', 'Health', 5, DATEADD('DAY', -10, NOW()), TRUE),
('Travel Stories', 'Share your travel experiences', 'Travel', 1, DATEADD('DAY', -8, NOW()), TRUE),
('Cooking Recipes', 'Share and discover new recipes', 'Food', 2, DATEADD('DAY', -6, NOW()), TRUE),
('Music Discovery', 'Discover new music and artists', 'Entertainment', 3, DATEADD('DAY', -4, NOW()), TRUE);

-- チャットメッセージの追加
-- Study Group messages (group-1)
INSERT INTO chat_messages (content, sender_id, room_id, message_type, created_at) VALUES
('Welcome to the Study Group!', 1, 'group-1', 'JOIN', DATEADD('DAY', -10, NOW())),
('Hello everyone!', 2, 'group-1', 'CHAT', DATEADD('DAY', -9, NOW())),
('Anyone want to study together today?', 3, 'group-1', 'CHAT', DATEADD('DAY', -8, NOW())),
('I''m available after 3 PM', 1, 'group-1', 'CHAT', DATEADD('DAY', -8, NOW())),
('Count me in!', 2, 'group-1', 'CHAT', DATEADD('DAY', -8, NOW()));

-- Gaming Clan messages (group-2)
INSERT INTO chat_messages (content, sender_id, room_id, message_type, created_at) VALUES
('Gaming Clan is now open!', 2, 'group-2', 'JOIN', DATEADD('DAY', -8, NOW())),
('Who wants to play tonight?', 1, 'group-2', 'CHAT', DATEADD('DAY', -7, NOW())),
('I''m in! What game?', 4, 'group-2', 'CHAT', DATEADD('DAY', -7, NOW())),
('Let''s play some team games', 2, 'group-2', 'CHAT', DATEADD('DAY', -7, NOW()));

-- Friend chat messages (friend-1: Alice & Bob)
INSERT INTO chat_messages (content, sender_id, room_id, message_type, created_at) VALUES
('Hey Bob, how are you?', 1, 'friend-1', 'CHAT', DATEADD('DAY', -6, NOW())),
('I''m doing great! Thanks for asking.', 2, 'friend-1', 'CHAT', DATEADD('DAY', -6, NOW())),
('Want to grab coffee sometime?', 1, 'friend-1', 'CHAT', DATEADD('DAY', -5, NOW())),
('Sure! How about tomorrow?', 2, 'friend-1', 'CHAT', DATEADD('DAY', -5, NOW()));

-- Topic messages (topic-1: Spring Boot Tips)
INSERT INTO chat_messages (content, sender_id, room_id, message_type, created_at) VALUES
('Welcome to Spring Boot Tips!', 1, 'topic-1', 'JOIN', DATEADD('DAY', -20, NOW())),
('Use @ConfigurationProperties for type-safe configuration', 1, 'topic-1', 'CHAT', DATEADD('DAY', -19, NOW())),
('Great tip! Also consider using @Validated with it', 2, 'topic-1', 'CHAT', DATEADD('DAY', -18, NOW())),
('Don''t forget about actuator endpoints for monitoring', 3, 'topic-1', 'CHAT', DATEADD('DAY', -17, NOW()));

-- Topic messages (topic-2: React Best Practices)
INSERT INTO chat_messages (content, sender_id, room_id, message_type, created_at) VALUES
('Let''s discuss React patterns!', 2, 'topic-2', 'JOIN', DATEADD('DAY', -18, NOW())),
('Always use functional components with hooks', 1, 'topic-2', 'CHAT', DATEADD('DAY', -17, NOW())),
('useCallback and useMemo are your friends for optimization', 2, 'topic-2', 'CHAT', DATEADD('DAY', -16, NOW())),
('Context API is great for avoiding prop drilling', 3, 'topic-2', 'CHAT', DATEADD('DAY', -15, NOW()));

-- General Chat messages (group-4)
INSERT INTO chat_messages (content, sender_id, room_id, message_type, created_at) VALUES
('General Chat is open to everyone!', 1, 'group-4', 'JOIN', DATEADD('DAY', -15, NOW())),
('Hello everyone! Happy to be here.', 2, 'group-4', 'CHAT', DATEADD('DAY', -14, NOW())),
('This is a great community!', 3, 'group-4', 'CHAT', DATEADD('DAY', -13, NOW())),
('Welcome all new members!', 1, 'group-4', 'CHAT', DATEADD('DAY', -12, NOW())),
('Thanks for the warm welcome!', 4, 'group-4', 'CHAT', DATEADD('DAY', -11, NOW()));

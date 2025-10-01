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
(1, 2, 'ACCEPTED', DATE_SUB(NOW(), INTERVAL 7 DAY), DATE_SUB(NOW(), INTERVAL 7 DAY)),
(1, 3, 'ACCEPTED', DATE_SUB(NOW(), INTERVAL 5 DAY), DATE_SUB(NOW(), INTERVAL 5 DAY)),
(2, 3, 'ACCEPTED', DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_SUB(NOW(), INTERVAL 3 DAY)),
(1, 4, 'PENDING', DATE_SUB(NOW(), INTERVAL 1 DAY), NULL),
(5, 1, 'PENDING', DATE_SUB(NOW(), INTERVAL 2 DAY), NULL);

-- 招待制グループの追加
INSERT INTO groups_table (name, description, group_type, invite_code, max_members, creator_id, created_at) VALUES
('Study Group', 'A group for studying together', 'INVITE_ONLY', 'STUDY123', 20, 1, DATE_SUB(NOW(), INTERVAL 10 DAY)),
('Gaming Clan', 'For gaming enthusiasts', 'INVITE_ONLY', 'GAME456', 30, 2, DATE_SUB(NOW(), INTERVAL 8 DAY)),
('Book Club', 'Monthly book discussions', 'INVITE_ONLY', 'BOOK789', 15, 3, DATE_SUB(NOW(), INTERVAL 5 DAY));

-- パブリックトピックグループの追加
INSERT INTO groups_table (name, description, group_type, invite_code, max_members, creator_id, created_at) VALUES
('General Chat', 'General discussions for everyone', 'PUBLIC_TOPIC', NULL, 100, 1, DATE_SUB(NOW(), INTERVAL 15 DAY)),
('Tech Talk', 'Technology and programming discussions', 'PUBLIC_TOPIC', NULL, 100, 2, DATE_SUB(NOW(), INTERVAL 12 DAY));

-- グループメンバーの追加
-- Study Group members
INSERT INTO group_members (group_id, user_id, role, joined_at) VALUES
(1, 1, 'ADMIN', DATE_SUB(NOW(), INTERVAL 10 DAY)),
(1, 2, 'MEMBER', DATE_SUB(NOW(), INTERVAL 9 DAY)),
(1, 3, 'MEMBER', DATE_SUB(NOW(), INTERVAL 8 DAY));

-- Gaming Clan members
INSERT INTO group_members (group_id, user_id, role, joined_at) VALUES
(2, 2, 'ADMIN', DATE_SUB(NOW(), INTERVAL 8 DAY)),
(2, 1, 'MEMBER', DATE_SUB(NOW(), INTERVAL 7 DAY)),
(2, 4, 'MEMBER', DATE_SUB(NOW(), INTERVAL 6 DAY));

-- Book Club members
INSERT INTO group_members (group_id, user_id, role, joined_at) VALUES
(3, 3, 'ADMIN', DATE_SUB(NOW(), INTERVAL 5 DAY)),
(3, 1, 'MEMBER', DATE_SUB(NOW(), INTERVAL 4 DAY)),
(3, 5, 'MEMBER', DATE_SUB(NOW(), INTERVAL 3 DAY));

-- General Chat members
INSERT INTO group_members (group_id, user_id, role, joined_at) VALUES
(4, 1, 'ADMIN', DATE_SUB(NOW(), INTERVAL 15 DAY)),
(4, 2, 'MEMBER', DATE_SUB(NOW(), INTERVAL 14 DAY)),
(4, 3, 'MEMBER', DATE_SUB(NOW(), INTERVAL 13 DAY)),
(4, 4, 'MEMBER', DATE_SUB(NOW(), INTERVAL 12 DAY)),
(4, 5, 'MEMBER', DATE_SUB(NOW(), INTERVAL 11 DAY));

-- Tech Talk members
INSERT INTO group_members (group_id, user_id, role, joined_at) VALUES
(5, 2, 'ADMIN', DATE_SUB(NOW(), INTERVAL 12 DAY)),
(5, 1, 'MEMBER', DATE_SUB(NOW(), INTERVAL 11 DAY)),
(5, 3, 'MEMBER', DATE_SUB(NOW(), INTERVAL 10 DAY));

-- トピックの追加
INSERT INTO topics (name, description, category, creator_id, created_at, is_active) VALUES
('Spring Boot Tips', 'Share your Spring Boot development tips', 'Programming', 1, DATE_SUB(NOW(), INTERVAL 20 DAY), TRUE),
('React Best Practices', 'Discuss React development patterns', 'Programming', 2, DATE_SUB(NOW(), INTERVAL 18 DAY), TRUE),
('Database Design', 'Database architecture and optimization', 'Programming', 3, DATE_SUB(NOW(), INTERVAL 15 DAY), TRUE),
('Movie Recommendations', 'Share and discuss movies', 'Entertainment', 4, DATE_SUB(NOW(), INTERVAL 12 DAY), TRUE),
('Fitness Goals', 'Track and share fitness achievements', 'Health', 5, DATE_SUB(NOW(), INTERVAL 10 DAY), TRUE),
('Travel Stories', 'Share your travel experiences', 'Travel', 1, DATE_SUB(NOW(), INTERVAL 8 DAY), TRUE),
('Cooking Recipes', 'Share and discover new recipes', 'Food', 2, DATE_SUB(NOW(), INTERVAL 6 DAY), TRUE),
('Music Discovery', 'Discover new music and artists', 'Entertainment', 3, DATE_SUB(NOW(), INTERVAL 4 DAY), TRUE);

-- チャットメッセージの追加
-- Study Group messages (group-1)
INSERT INTO chat_messages (content, sender_id, room_id, message_type, created_at) VALUES
('Welcome to the Study Group!', 1, 'group-1', 'JOIN', DATE_SUB(NOW(), INTERVAL 10 DAY)),
('Hello everyone!', 2, 'group-1', 'CHAT', DATE_SUB(NOW(), INTERVAL 9 DAY)),
('Anyone want to study together today?', 3, 'group-1', 'CHAT', DATE_SUB(NOW(), INTERVAL 8 DAY)),
('I''m available after 3 PM', 1, 'group-1', 'CHAT', DATE_SUB(NOW(), INTERVAL 8 DAY)),
('Count me in!', 2, 'group-1', 'CHAT', DATE_SUB(NOW(), INTERVAL 8 DAY));

-- Gaming Clan messages (group-2)
INSERT INTO chat_messages (content, sender_id, room_id, message_type, created_at) VALUES
('Gaming Clan is now open!', 2, 'group-2', 'JOIN', DATE_SUB(NOW(), INTERVAL 8 DAY)),
('Who wants to play tonight?', 1, 'group-2', 'CHAT', DATE_SUB(NOW(), INTERVAL 7 DAY)),
('I''m in! What game?', 4, 'group-2', 'CHAT', DATE_SUB(NOW(), INTERVAL 7 DAY)),
('Let''s play some team games', 2, 'group-2', 'CHAT', DATE_SUB(NOW(), INTERVAL 7 DAY));

-- Friend chat messages (friend-1: Alice & Bob)
INSERT INTO chat_messages (content, sender_id, room_id, message_type, created_at) VALUES
('Hey Bob, how are you?', 1, 'friend-1', 'CHAT', DATE_SUB(NOW(), INTERVAL 6 DAY)),
('I''m doing great! Thanks for asking.', 2, 'friend-1', 'CHAT', DATE_SUB(NOW(), INTERVAL 6 DAY)),
('Want to grab coffee sometime?', 1, 'friend-1', 'CHAT', DATE_SUB(NOW(), INTERVAL 5 DAY)),
('Sure! How about tomorrow?', 2, 'friend-1', 'CHAT', DATE_SUB(NOW(), INTERVAL 5 DAY));

-- Topic messages (topic-1: Spring Boot Tips)
INSERT INTO chat_messages (content, sender_id, room_id, message_type, created_at) VALUES
('Welcome to Spring Boot Tips!', 1, 'topic-1', 'JOIN', DATE_SUB(NOW(), INTERVAL 20 DAY)),
('Use @ConfigurationProperties for type-safe configuration', 1, 'topic-1', 'CHAT', DATE_SUB(NOW(), INTERVAL 19 DAY)),
('Great tip! Also consider using @Validated with it', 2, 'topic-1', 'CHAT', DATE_SUB(NOW(), INTERVAL 18 DAY)),
('Don''t forget about actuator endpoints for monitoring', 3, 'topic-1', 'CHAT', DATE_SUB(NOW(), INTERVAL 17 DAY));

-- Topic messages (topic-2: React Best Practices)
INSERT INTO chat_messages (content, sender_id, room_id, message_type, created_at) VALUES
('Let''s discuss React patterns!', 2, 'topic-2', 'JOIN', DATE_SUB(NOW(), INTERVAL 18 DAY)),
('Always use functional components with hooks', 1, 'topic-2', 'CHAT', DATE_SUB(NOW(), INTERVAL 17 DAY)),
('useCallback and useMemo are your friends for optimization', 2, 'topic-2', 'CHAT', DATE_SUB(NOW(), INTERVAL 16 DAY)),
('Context API is great for avoiding prop drilling', 3, 'topic-2', 'CHAT', DATE_SUB(NOW(), INTERVAL 15 DAY));

-- General Chat messages (group-4)
INSERT INTO chat_messages (content, sender_id, room_id, message_type, created_at) VALUES
('General Chat is open to everyone!', 1, 'group-4', 'JOIN', DATE_SUB(NOW(), INTERVAL 15 DAY)),
('Hello everyone! Happy to be here.', 2, 'group-4', 'CHAT', DATE_SUB(NOW(), INTERVAL 14 DAY)),
('This is a great community!', 3, 'group-4', 'CHAT', DATE_SUB(NOW(), INTERVAL 13 DAY)),
('Welcome all new members!', 1, 'group-4', 'CHAT', DATE_SUB(NOW(), INTERVAL 12 DAY)),
('Thanks for the warm welcome!', 4, 'group-4', 'CHAT', DATE_SUB(NOW(), INTERVAL 11 DAY));

-- Initial Database Schema for Chat Application
-- Version 1.0

-- ユーザーテーブル
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    display_name VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP NULL
);

CREATE INDEX idx_username ON users(username);
CREATE INDEX idx_email ON users(email);

-- フレンド関係テーブル
CREATE TABLE IF NOT EXISTS friends (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    requester_id BIGINT NOT NULL,
    addressee_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    requested_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    responded_at TIMESTAMP NULL,
    FOREIGN KEY (requester_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (addressee_id) REFERENCES users(id) ON DELETE CASCADE,
    CHECK (status IN ('PENDING', 'ACCEPTED', 'REJECTED', 'BLOCKED')),
    CHECK (requester_id != addressee_id)
);

CREATE UNIQUE INDEX uk_friendship ON friends(requester_id, addressee_id);
CREATE INDEX idx_requester ON friends(requester_id);
CREATE INDEX idx_addressee ON friends(addressee_id);
CREATE INDEX idx_status ON friends(status);

-- グループテーブル
CREATE TABLE IF NOT EXISTS groups_table (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    group_type VARCHAR(20) NOT NULL DEFAULT 'INVITE_ONLY',
    invite_code VARCHAR(8),
    max_members INT NOT NULL DEFAULT 50,
    creator_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (creator_id) REFERENCES users(id) ON DELETE CASCADE,
    CHECK (group_type IN ('INVITE_ONLY', 'PUBLIC_TOPIC')),
    CHECK (max_members > 0)
);

CREATE UNIQUE INDEX uk_invite_code ON groups_table(invite_code);
CREATE INDEX idx_group_type ON groups_table(group_type);
CREATE INDEX idx_groups_creator ON groups_table(creator_id);
CREATE INDEX idx_groups_created_at ON groups_table(created_at);

-- グループメンバーテーブル
CREATE TABLE IF NOT EXISTS group_members (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    group_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'MEMBER',
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (group_id) REFERENCES groups_table(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CHECK (role IN ('ADMIN', 'MEMBER'))
);

CREATE UNIQUE INDEX uk_group_member ON group_members(group_id, user_id);
CREATE INDEX idx_group ON group_members(group_id);
CREATE INDEX idx_user ON group_members(user_id);
CREATE INDEX idx_role ON group_members(role);

-- トピックテーブル
CREATE TABLE IF NOT EXISTS topics (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    category VARCHAR(50) NOT NULL,
    creator_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    FOREIGN KEY (creator_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE INDEX idx_topics_category ON topics(category);
CREATE INDEX idx_topics_creator ON topics(creator_id);
CREATE INDEX idx_topics_is_active ON topics(is_active);
CREATE INDEX idx_topics_created_at ON topics(created_at);
CREATE INDEX idx_topics_category_active ON topics(category, is_active);

-- チャットメッセージテーブル
CREATE TABLE IF NOT EXISTS chat_messages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    content TEXT NOT NULL,
    sender_id BIGINT NOT NULL,
    room_id VARCHAR(255) NOT NULL,
    message_type VARCHAR(20) NOT NULL DEFAULT 'CHAT',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (sender_id) REFERENCES users(id) ON DELETE CASCADE,
    CHECK (message_type IN ('CHAT', 'JOIN', 'LEAVE'))
);

CREATE INDEX idx_chat_messages_room ON chat_messages(room_id);
CREATE INDEX idx_chat_messages_sender ON chat_messages(sender_id);
CREATE INDEX idx_chat_messages_created_at ON chat_messages(created_at);
CREATE INDEX idx_chat_messages_room_created ON chat_messages(room_id, created_at);

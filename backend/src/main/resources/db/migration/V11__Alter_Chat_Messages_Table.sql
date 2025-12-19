-- V11: chat_messagesテーブルの拡張
-- テナント管理、匿名送信者、OKRタグ、添付ファイルなどの追加
-- 作成日: 2025-11-07

-- chat_messagesテーブルにカラムを追加
-- H2ではAFTER句は使用できないため、順番に追加
ALTER TABLE chat_messages ADD COLUMN tenant_id BIGINT;
ALTER TABLE chat_messages ADD COLUMN is_anonymous BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE chat_messages ADD COLUMN anonymous_sender_id BIGINT;
ALTER TABLE chat_messages ADD COLUMN parent_message_id BIGINT;
ALTER TABLE chat_messages ADD COLUMN post_template VARCHAR(20);
ALTER TABLE chat_messages ADD COLUMN okr_tags VARCHAR(4000);
ALTER TABLE chat_messages ADD COLUMN attachments VARCHAR(4000);
ALTER TABLE chat_messages ADD COLUMN metadata VARCHAR(4000);
ALTER TABLE chat_messages ADD COLUMN reaction_count INT NOT NULL DEFAULT 0;
ALTER TABLE chat_messages ADD COLUMN is_edited BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE chat_messages ADD COLUMN edited_at TIMESTAMP NULL;
ALTER TABLE chat_messages ADD COLUMN is_deleted BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE chat_messages ADD COLUMN deleted_at TIMESTAMP NULL;
ALTER TABLE chat_messages ADD COLUMN updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

-- 外部キー制約を追加
ALTER TABLE chat_messages
ADD CONSTRAINT fk_messages_tenant
    FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE;

ALTER TABLE chat_messages
ADD CONSTRAINT fk_messages_anonymous_sender
    FOREIGN KEY (anonymous_sender_id) REFERENCES anonymous_profiles(id) ON DELETE SET NULL;

ALTER TABLE chat_messages
ADD CONSTRAINT fk_messages_parent
    FOREIGN KEY (parent_message_id) REFERENCES chat_messages(id) ON DELETE CASCADE;

-- インデックスを追加
CREATE INDEX idx_messages_tenant ON chat_messages(tenant_id);
CREATE INDEX idx_messages_is_anonymous ON chat_messages(is_anonymous);
CREATE INDEX idx_messages_anonymous_sender ON chat_messages(anonymous_sender_id);
CREATE INDEX idx_messages_parent ON chat_messages(parent_message_id);
CREATE INDEX idx_messages_post_template ON chat_messages(post_template);
CREATE INDEX idx_messages_is_deleted ON chat_messages(is_deleted);
CREATE INDEX idx_messages_tenant_room ON chat_messages(tenant_id, room_id);
CREATE INDEX idx_messages_tenant_room_created ON chat_messages(tenant_id, room_id, created_at DESC);
CREATE INDEX idx_messages_room_created_deleted ON chat_messages(room_id, created_at DESC, is_deleted);
CREATE INDEX idx_messages_sender_created ON chat_messages(sender_id, created_at DESC);

-- CHECK制約を追加
ALTER TABLE chat_messages
ADD CONSTRAINT chk_message_post_template
    CHECK (post_template IS NULL OR post_template IN ('DAILY', 'BLOCKER', 'LEARNING', 'REFLECTION', 'GOAL'));

-- 既存メッセージのデータを更新（デフォルトテナントに紐付け）
UPDATE chat_messages
SET tenant_id = 1
WHERE tenant_id IS NULL;

-- tenant_idをNOT NULLに変更
ALTER TABLE chat_messages ALTER COLUMN tenant_id SET NOT NULL;

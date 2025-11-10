-- V11: chat_messagesテーブルの拡張
-- テナント管理、匿名送信者、OKRタグ、添付ファイルなどの追加
-- 作成日: 2025-11-07

-- chat_messagesテーブルにカラムを追加
ALTER TABLE chat_messages
ADD COLUMN tenant_id BIGINT AFTER id COMMENT '所属テナント',
ADD COLUMN is_anonymous BOOLEAN NOT NULL DEFAULT FALSE AFTER sender_id COMMENT '匿名送信',
ADD COLUMN anonymous_sender_id BIGINT AFTER is_anonymous COMMENT '匿名送信者ID',
ADD COLUMN parent_message_id BIGINT AFTER room_id COMMENT '親メッセージ（スレッド化）',
ADD COLUMN post_template VARCHAR(20) AFTER message_type COMMENT '投稿テンプレート',
ADD COLUMN okr_tags JSON AFTER content COMMENT 'OKRタグ',
ADD COLUMN attachments JSON AFTER okr_tags COMMENT '添付ファイル',
ADD COLUMN metadata JSON AFTER attachments COMMENT 'メタデータ',
ADD COLUMN reaction_count INT NOT NULL DEFAULT 0 AFTER created_at COMMENT 'リアクション数',
ADD COLUMN is_edited BOOLEAN NOT NULL DEFAULT FALSE AFTER reaction_count COMMENT '編集済み',
ADD COLUMN edited_at TIMESTAMP NULL AFTER is_edited COMMENT '編集日時',
ADD COLUMN is_deleted BOOLEAN NOT NULL DEFAULT FALSE AFTER edited_at COMMENT '削除済み',
ADD COLUMN deleted_at TIMESTAMP NULL AFTER is_deleted COMMENT '削除日時',
ADD COLUMN updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP AFTER deleted_at COMMENT '更新日時';

-- 外部キー制約を追加
ALTER TABLE chat_messages
ADD CONSTRAINT fk_messages_tenant
    FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE,
ADD CONSTRAINT fk_messages_anonymous_sender
    FOREIGN KEY (anonymous_sender_id) REFERENCES anonymous_profiles(id) ON DELETE SET NULL,
ADD CONSTRAINT fk_messages_parent
    FOREIGN KEY (parent_message_id) REFERENCES chat_messages(id) ON DELETE CASCADE;

-- インデックスを追加
ALTER TABLE chat_messages
ADD INDEX idx_tenant (tenant_id),
ADD INDEX idx_is_anonymous (is_anonymous),
ADD INDEX idx_anonymous_sender (anonymous_sender_id),
ADD INDEX idx_parent (parent_message_id),
ADD INDEX idx_post_template (post_template),
ADD INDEX idx_is_deleted (is_deleted),
ADD INDEX idx_tenant_room (tenant_id, room_id),
ADD INDEX idx_tenant_room_created (tenant_id, room_id, created_at DESC),
ADD INDEX idx_room_created_deleted (room_id, created_at DESC, is_deleted),
ADD INDEX idx_sender_created (sender_id, created_at DESC);

-- CHECK制約を追加
ALTER TABLE chat_messages
ADD CONSTRAINT chk_message_post_template
    CHECK (post_template IS NULL OR post_template IN ('DAILY', 'BLOCKER', 'LEARNING', 'REFLECTION', 'GOAL'));

-- 既存メッセージのデータを更新（デフォルトテナントに紐付け）
UPDATE chat_messages
SET tenant_id = 1
WHERE tenant_id IS NULL;

-- tenant_idをNOT NULLに変更
ALTER TABLE chat_messages
MODIFY COLUMN tenant_id BIGINT NOT NULL COMMENT '所属テナント';

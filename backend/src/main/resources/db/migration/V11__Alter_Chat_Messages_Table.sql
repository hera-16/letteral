-- V11: chat_messagesテーブルの拡張
-- テナント管理、匿名送信者、OKRタグ、添付ファイルなどの追加
-- 作成日: 2025-11-07

-- chat_messagesテーブルにカラムを追加
ALTER TABLE chat_messages
ADD COLUMN tenant_id BIGINT AFTER id,
ADD COLUMN is_anonymous BOOLEAN NOT NULL DEFAULT FALSE AFTER sender_id,
ADD COLUMN anonymous_sender_id BIGINT AFTER is_anonymous,
ADD COLUMN parent_message_id BIGINT AFTER room_id,
ADD COLUMN post_template VARCHAR(20) AFTER message_type,
ADD COLUMN okr_tags JSON AFTER content,
ADD COLUMN attachments JSON AFTER okr_tags,
ADD COLUMN metadata JSON AFTER attachments,
ADD COLUMN reaction_count INT NOT NULL DEFAULT 0 AFTER created_at,
ADD COLUMN is_edited BOOLEAN NOT NULL DEFAULT FALSE AFTER reaction_count,
ADD COLUMN edited_at TIMESTAMP NULL AFTER is_edited,
ADD COLUMN is_deleted BOOLEAN NOT NULL DEFAULT FALSE AFTER edited_at,
ADD COLUMN deleted_at TIMESTAMP NULL AFTER is_deleted,
ADD COLUMN updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP AFTER deleted_at;

-- カラムにコメントを追加
ALTER TABLE chat_messages
MODIFY COLUMN tenant_id BIGINT COMMENT '所属テナント',
MODIFY COLUMN is_anonymous BOOLEAN NOT NULL DEFAULT FALSE COMMENT '匿名送信',
MODIFY COLUMN anonymous_sender_id BIGINT COMMENT '匿名送信者ID',
MODIFY COLUMN parent_message_id BIGINT COMMENT '親メッセージ（スレッド化）',
MODIFY COLUMN post_template VARCHAR(20) COMMENT '投稿テンプレート',
MODIFY COLUMN okr_tags JSON COMMENT 'OKRタグ',
MODIFY COLUMN attachments JSON COMMENT '添付ファイル',
MODIFY COLUMN metadata JSON COMMENT 'メタデータ',
MODIFY COLUMN reaction_count INT NOT NULL DEFAULT 0 COMMENT 'リアクション数',
MODIFY COLUMN is_edited BOOLEAN NOT NULL DEFAULT FALSE COMMENT '編集済み',
MODIFY COLUMN edited_at TIMESTAMP NULL COMMENT '編集日時',
MODIFY COLUMN is_deleted BOOLEAN NOT NULL DEFAULT FALSE COMMENT '削除済み',
MODIFY COLUMN deleted_at TIMESTAMP NULL COMMENT '削除日時',
MODIFY COLUMN updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新日時';

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

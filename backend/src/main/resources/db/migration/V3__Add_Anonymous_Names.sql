-- グループ内の匿名名マッピングテーブル
-- 全員が同じ匿名名を見るように、viewer_idは削除し、target_user_idとgroup_idのみで一意に決定
CREATE TABLE IF NOT EXISTS group_member_aliases (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    target_user_id BIGINT NOT NULL,
    group_id BIGINT NOT NULL,
    anonymous_name VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    last_rotation_date DATE NOT NULL,
    UNIQUE KEY unique_alias (target_user_id, group_id),
    FOREIGN KEY (target_user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (group_id) REFERENCES groups(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- インデックスを追加してクエリを高速化
CREATE INDEX idx_target_group ON group_member_aliases(target_user_id, group_id);
CREATE INDEX idx_rotation_date ON group_member_aliases(last_rotation_date);

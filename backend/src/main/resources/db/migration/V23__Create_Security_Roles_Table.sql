-- V23: Spring Security用のrolesテーブル作成
-- Role.javaエンティティ用のシンプルなロールテーブル

-- rolesテーブル
CREATE TABLE IF NOT EXISTS roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    INDEX idx_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Spring Securityロールテーブル';

-- user_role_mappingsジョインテーブル（User.javaのManyToMany関係用）
CREATE TABLE IF NOT EXISTS user_role_mappings (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='ユーザーとロールの関連テーブル';

-- デフォルトロールの挿入
INSERT INTO roles (name, description) VALUES
('ROLE_USER', '一般ユーザー'),
('ROLE_ADMIN', 'システム管理者'),
('ROLE_MODERATOR', 'モデレーター');

-- 既存ユーザーにROLE_USERを付与
INSERT INTO user_role_mappings (user_id, role_id)
SELECT u.id, r.id
FROM users u, roles r
WHERE r.name = 'ROLE_USER'
AND NOT EXISTS (
    SELECT 1 FROM user_role_mappings ur WHERE ur.user_id = u.id AND ur.role_id = r.id
);

SELECT 'V23 Security Roles - Created roles table and assigned default roles' AS status;

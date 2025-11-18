-- V20: ダイジェストと1on1ミーティングテーブルを作成

-- 進捗ダイジェストテーブル
CREATE TABLE IF NOT EXISTS progress_digests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    tenant_id BIGINT NOT NULL,
    digest_type VARCHAR(20) NOT NULL COMMENT 'WEEKLY, MONTHLY, QUARTERLY',
    period_start DATETIME NOT NULL,
    period_end DATETIME NOT NULL,
    summary TEXT,
    total_posts INT NOT NULL DEFAULT 0,
    completed_goals INT NOT NULL DEFAULT 0,
    ongoing_goals INT NOT NULL DEFAULT 0,
    challenges INT NOT NULL DEFAULT 0,
    top_achievements TEXT,
    top_challenges TEXT,
    key_learnings TEXT,
    next_steps TEXT,
    generated_at DATETIME NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE,
    INDEX idx_user_tenant (user_id, tenant_id),
    INDEX idx_digest_type (digest_type),
    INDEX idx_period (period_start, period_end),
    UNIQUE KEY unique_digest (user_id, tenant_id, digest_type, period_start, period_end)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 1on1ミーティングテーブル
CREATE TABLE IF NOT EXISTS one_on_one_meetings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_id BIGINT NOT NULL,
    manager_id BIGINT NOT NULL,
    tenant_id BIGINT NOT NULL,
    scheduled_at DATETIME NOT NULL,
    auto_generated_agenda TEXT,
    custom_agenda TEXT,
    discussion_topics TEXT,
    action_items TEXT,
    notes TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED' COMMENT 'SCHEDULED, COMPLETED, CANCELLED',
    completed_at DATETIME,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (employee_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (manager_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE,
    INDEX idx_employee_tenant (employee_id, tenant_id),
    INDEX idx_manager_tenant (manager_id, tenant_id),
    INDEX idx_scheduled_at (scheduled_at),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- V20: ダイジェストと1on1ミーティングテーブルを作成

-- 進捗ダイジェストテーブル
CREATE TABLE IF NOT EXISTS progress_digests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    tenant_id BIGINT NOT NULL,
    digest_type VARCHAR(20) NOT NULL,
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
    UNIQUE (user_id, tenant_id, digest_type, period_start, period_end)
);

CREATE INDEX idx_user_tenant ON progress_digests(user_id, tenant_id);
CREATE INDEX idx_digest_type ON progress_digests(digest_type);
CREATE INDEX idx_period ON progress_digests(period_start, period_end);

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
    status VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED',
    completed_at DATETIME,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (employee_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (manager_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE
);

CREATE INDEX idx_employee_tenant ON one_on_one_meetings(employee_id, tenant_id);
CREATE INDEX idx_manager_tenant ON one_on_one_meetings(manager_id, tenant_id);
CREATE INDEX idx_scheduled_at ON one_on_one_meetings(scheduled_at);
CREATE INDEX idx_status ON one_on_one_meetings(status);

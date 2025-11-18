-- 通報テーブル
CREATE TABLE reports (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    reporter_id BIGINT NOT NULL,
    reported_user_id BIGINT,
    reported_post_id BIGINT,
    reported_message_id BIGINT,
    report_type VARCHAR(50) NOT NULL,
    reason TEXT NOT NULL,
    status VARCHAR(50) DEFAULT 'PENDING',
    resolution TEXT,
    resolved_by BIGINT,
    resolved_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (tenant_id) REFERENCES tenants(id),
    FOREIGN KEY (reporter_id) REFERENCES users(id),
    FOREIGN KEY (reported_user_id) REFERENCES users(id),
    FOREIGN KEY (reported_post_id) REFERENCES progress_posts(id),
    FOREIGN KEY (reported_message_id) REFERENCES chat_messages(id),
    FOREIGN KEY (resolved_by) REFERENCES users(id),
    INDEX idx_tenant_status (tenant_id, status),
    INDEX idx_created_at (created_at)
);

-- Slack連携設定テーブル
CREATE TABLE slack_integrations (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    workspace_id VARCHAR(255) NOT NULL,
    bot_token VARCHAR(500) NOT NULL,
    webhook_url VARCHAR(500),
    default_channel VARCHAR(255),
    is_active BOOLEAN DEFAULT TRUE,
    settings JSON,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (tenant_id) REFERENCES tenants(id),
    UNIQUE KEY uk_tenant_workspace (tenant_id, workspace_id)
);

-- Slack通知ルールテーブル
CREATE TABLE slack_notification_rules (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    integration_id BIGINT NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    channel_id VARCHAR(255) NOT NULL,
    message_template TEXT,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (tenant_id) REFERENCES tenants(id),
    FOREIGN KEY (integration_id) REFERENCES slack_integrations(id) ON DELETE CASCADE,
    INDEX idx_tenant_event (tenant_id, event_type)
);

-- 管理者アクションログテーブル
CREATE TABLE admin_action_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    admin_user_id BIGINT NOT NULL,
    action_type VARCHAR(100) NOT NULL,
    target_type VARCHAR(100),
    target_id BIGINT,
    description TEXT,
    metadata JSON,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (tenant_id) REFERENCES tenants(id),
    FOREIGN KEY (admin_user_id) REFERENCES users(id),
    INDEX idx_tenant_admin (tenant_id, admin_user_id),
    INDEX idx_created_at (created_at)
);

-- テナント統計キャッシュテーブル（パフォーマンス向上のため）
CREATE TABLE tenant_statistics_cache (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    tenant_id BIGINT NOT NULL,
    stat_date DATE NOT NULL,
    total_users INT DEFAULT 0,
    active_users INT DEFAULT 0,
    total_posts INT DEFAULT 0,
    total_messages INT DEFAULT 0,
    total_okrs INT DEFAULT 0,
    completed_okrs INT DEFAULT 0,
    metadata JSON,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (tenant_id) REFERENCES tenants(id),
    UNIQUE KEY uk_tenant_date (tenant_id, stat_date)
);

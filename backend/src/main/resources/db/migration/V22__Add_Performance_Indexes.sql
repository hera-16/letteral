-- パフォーマンス最適化のためのインデックス追加

-- Users テーブル
CREATE INDEX idx_users_tenant_id ON users(tenant_id);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_username ON users(username);

-- Progress Posts テーブル
CREATE INDEX idx_progress_posts_author_id ON progress_posts(author_id);
CREATE INDEX idx_progress_posts_tenant_id ON progress_posts(tenant_id);
CREATE INDEX idx_progress_posts_created_at ON progress_posts(created_at);
CREATE INDEX idx_progress_posts_author_created ON progress_posts(author_id, created_at);
CREATE INDEX idx_progress_posts_tenant_created ON progress_posts(tenant_id, created_at);

-- Progress Digests テーブル
CREATE INDEX idx_progress_digests_user_id ON progress_digests(user_id);
CREATE INDEX idx_progress_digests_tenant_id ON progress_digests(tenant_id);
CREATE INDEX idx_progress_digests_period_start ON progress_digests(period_start);
CREATE INDEX idx_progress_digests_digest_type ON progress_digests(digest_type);
CREATE INDEX idx_progress_digests_user_type ON progress_digests(user_id, digest_type);
CREATE INDEX idx_progress_digests_user_period ON progress_digests(user_id, period_start, period_end);

-- One-on-One Meetings テーブル
CREATE INDEX idx_one_on_one_employee_id ON one_on_one_meetings(employee_id);
CREATE INDEX idx_one_on_one_manager_id ON one_on_one_meetings(manager_id);
CREATE INDEX idx_one_on_one_tenant_id ON one_on_one_meetings(tenant_id);
CREATE INDEX idx_one_on_one_scheduled_at ON one_on_one_meetings(scheduled_at);
CREATE INDEX idx_one_on_one_status ON one_on_one_meetings(status);
CREATE INDEX idx_one_on_one_employee_scheduled ON one_on_one_meetings(employee_id, scheduled_at);
CREATE INDEX idx_one_on_one_manager_scheduled ON one_on_one_meetings(manager_id, scheduled_at);
CREATE INDEX idx_one_on_one_employee_status ON one_on_one_meetings(employee_id, status, scheduled_at);

-- OKRs テーブル
CREATE INDEX idx_okrs_owner_id ON okrs(owner_id);
CREATE INDEX idx_okrs_tenant_id ON okrs(tenant_id);
CREATE INDEX idx_okrs_parent_id ON okrs(parent_okr_id);
CREATE INDEX idx_okrs_status ON okrs(status);
CREATE INDEX idx_okrs_due_date ON okrs(due_date);

-- Key Results テーブル
CREATE INDEX idx_key_results_okr_id ON key_results(okr_id);
CREATE INDEX idx_key_results_status ON key_results(status);

-- Organizations テーブル
CREATE INDEX idx_organizations_tenant_id ON organizations(tenant_id);
CREATE INDEX idx_organizations_parent_id ON organizations(parent_organization_id);

-- Groups テーブル
CREATE INDEX idx_groups_tenant_id ON groups(tenant_id);
CREATE INDEX idx_groups_organization_id ON groups(organization_id);

-- Chat Messages テーブル
CREATE INDEX idx_chat_messages_group_id ON chat_messages(group_id);
CREATE INDEX idx_chat_messages_user_id ON chat_messages(user_id);
CREATE INDEX idx_chat_messages_created_at ON chat_messages(created_at);
CREATE INDEX idx_chat_messages_group_created ON chat_messages(group_id, created_at);

-- Reports テーブル (Phase 4で作成)
CREATE INDEX IF NOT EXISTS idx_reports_tenant_id ON reports(tenant_id);
CREATE INDEX IF NOT EXISTS idx_reports_generated_by ON reports(generated_by);
CREATE INDEX IF NOT EXISTS idx_reports_report_type ON reports(report_type);
CREATE INDEX IF NOT EXISTS idx_reports_created_at ON reports(created_at);

-- Admin Action Logs テーブル (Phase 4で作成)
CREATE INDEX IF NOT EXISTS idx_admin_action_logs_tenant_id ON admin_action_logs(tenant_id);
CREATE INDEX IF NOT EXISTS idx_admin_action_logs_admin_id ON admin_action_logs(admin_id);
CREATE INDEX IF NOT EXISTS idx_admin_action_logs_action_type ON admin_action_logs(action_type);
CREATE INDEX IF NOT EXISTS idx_admin_action_logs_created_at ON admin_action_logs(created_at);

-- Slack Integrations テーブル (Phase 4で作成)
CREATE INDEX IF NOT EXISTS idx_slack_integrations_tenant_id ON slack_integrations(tenant_id);
CREATE INDEX IF NOT EXISTS idx_slack_integrations_user_id ON slack_integrations(user_id);

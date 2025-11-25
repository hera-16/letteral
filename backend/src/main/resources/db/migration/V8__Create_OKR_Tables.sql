-- V8: OKR（Objectives and Key Results）管理テーブルの作成
-- 目標管理と進捗投稿を紐付けるためのテーブル
-- 作成日: 2025-11-07

-- OKR目標テーブル
CREATE TABLE IF NOT EXISTS okr_objectives (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL COMMENT '所属テナント',
    organization_id BIGINT COMMENT '組織目標（NULLは個人目標）',
    owner_id BIGINT COMMENT '目標オーナー（個人目標の場合）',
    parent_objective_id BIGINT COMMENT '親目標（階層化）',

    title VARCHAR(300) NOT NULL COMMENT '目標タイトル',
    description TEXT COMMENT '目標説明',
    target_quarter VARCHAR(10) COMMENT '対象四半期（例: 2025-Q1）',
    start_date DATE COMMENT '開始日',
    end_date DATE COMMENT '終了日',

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT 'ステータス',
    progress_rate INT NOT NULL DEFAULT 0 COMMENT '進捗率（0-100）',
    confidence_level VARCHAR(20) COMMENT '達成見込み',

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    completed_at TIMESTAMP NULL COMMENT '完了日時',

    FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE,
    FOREIGN KEY (organization_id) REFERENCES organizations(id) ON DELETE CASCADE,
    FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (parent_objective_id) REFERENCES okr_objectives(id) ON DELETE CASCADE,
    INDEX idx_tenant (tenant_id),
    INDEX idx_organization (organization_id),
    INDEX idx_owner (owner_id),
    INDEX idx_parent (parent_objective_id),
    INDEX idx_quarter (target_quarter),
    INDEX idx_status (status),
    INDEX idx_tenant_quarter (tenant_id, target_quarter),
    INDEX idx_tenant_status (tenant_id, status),
    INDEX idx_org_quarter (organization_id, target_quarter),
    CHECK (status IN ('DRAFT', 'ACTIVE', 'COMPLETED', 'CANCELLED', 'ON_HOLD')),
    CHECK (progress_rate >= 0 AND progress_rate <= 100),
    CHECK (confidence_level IS NULL OR confidence_level IN ('HIGH', 'MEDIUM', 'LOW', 'AT_RISK'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='OKR目標テーブル';

-- OKR主要結果テーブル
CREATE TABLE IF NOT EXISTS okr_key_results (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL COMMENT '所属テナント',
    objective_id BIGINT NOT NULL COMMENT '所属目標',

    title VARCHAR(300) NOT NULL COMMENT '主要結果タイトル',
    description TEXT COMMENT '説明',
    metric_type VARCHAR(20) NOT NULL COMMENT '指標タイプ',
    baseline_value DECIMAL(15,2) COMMENT '開始値',
    target_value DECIMAL(15,2) NOT NULL COMMENT '目標値',
    current_value DECIMAL(15,2) NOT NULL DEFAULT 0 COMMENT '現在値',
    unit VARCHAR(50) COMMENT '単位（例: 件、%、円）',

    progress_rate INT NOT NULL DEFAULT 0 COMMENT '進捗率（0-100）',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT 'ステータス',

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    completed_at TIMESTAMP NULL COMMENT '完了日時',

    FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE,
    FOREIGN KEY (objective_id) REFERENCES okr_objectives(id) ON DELETE CASCADE,
    INDEX idx_tenant (tenant_id),
    INDEX idx_objective (objective_id),
    INDEX idx_status (status),
    INDEX idx_tenant_objective (tenant_id, objective_id),
    CHECK (metric_type IN ('NUMBER', 'PERCENTAGE', 'BOOLEAN', 'CURRENCY')),
    CHECK (status IN ('ACTIVE', 'COMPLETED', 'CANCELLED', 'ON_HOLD')),
    CHECK (progress_rate >= 0 AND progress_rate <= 100)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='OKR主要結果テーブル';

-- 投稿とOKRの紐付けテーブル
CREATE TABLE IF NOT EXISTS post_okr_links (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL COMMENT '所属テナント',
    post_id BIGINT NOT NULL COMMENT '進捗投稿',
    objective_id BIGINT COMMENT '関連目標',
    key_result_id BIGINT COMMENT '関連主要結果',
    contribution_note TEXT COMMENT 'この投稿がOKRにどう貢献したか',
    impact_score INT COMMENT '貢献度スコア（1-5）',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE,
    FOREIGN KEY (post_id) REFERENCES progress_posts(id) ON DELETE CASCADE,
    FOREIGN KEY (objective_id) REFERENCES okr_objectives(id) ON DELETE CASCADE,
    FOREIGN KEY (key_result_id) REFERENCES okr_key_results(id) ON DELETE CASCADE,
    INDEX idx_tenant (tenant_id),
    INDEX idx_post (post_id),
    INDEX idx_objective (objective_id),
    INDEX idx_key_result (key_result_id),
    INDEX idx_tenant_post (tenant_id, post_id),
    INDEX idx_tenant_objective (tenant_id, objective_id),
    CHECK (objective_id IS NOT NULL OR key_result_id IS NOT NULL),
    CHECK (impact_score IS NULL OR (impact_score >= 1 AND impact_score <= 5))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='投稿とOKRの紐付けテーブル';

-- OKR更新履歴テーブル（進捗の変遷を記録）
CREATE TABLE IF NOT EXISTS okr_update_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL COMMENT '所属テナント',
    objective_id BIGINT COMMENT '目標ID',
    key_result_id BIGINT COMMENT '主要結果ID',

    field_name VARCHAR(50) NOT NULL COMMENT '更新フィールド',
    old_value TEXT COMMENT '旧値',
    new_value TEXT COMMENT '新値',
    updated_by BIGINT NULL COMMENT '更新者',
    update_note TEXT COMMENT '更新メモ',

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE,
    FOREIGN KEY (objective_id) REFERENCES okr_objectives(id) ON DELETE CASCADE,
    FOREIGN KEY (key_result_id) REFERENCES okr_key_results(id) ON DELETE CASCADE,
    FOREIGN KEY (updated_by) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_tenant (tenant_id),
    INDEX idx_objective (objective_id),
    INDEX idx_key_result (key_result_id),
    INDEX idx_created_at (created_at DESC),
    CHECK (objective_id IS NOT NULL OR key_result_id IS NOT NULL)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='OKR更新履歴テーブル';

-- 評価スナップショットテーブル
CREATE TABLE IF NOT EXISTS evaluation_snapshots (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL COMMENT '所属テナント',
    user_id BIGINT NOT NULL COMMENT '対象ユーザー',
    organization_id BIGINT NOT NULL COMMENT '所属組織',

    period_start DATE NOT NULL COMMENT '期間開始日',
    period_end DATE NOT NULL COMMENT '期間終了日',
    period_label VARCHAR(100) COMMENT '期間ラベル（例: 2025年Q1）',

    -- 集計データ
    total_posts INT NOT NULL DEFAULT 0 COMMENT '投稿総数',
    total_achievements INT NOT NULL DEFAULT 0 COMMENT '達成総数',
    avg_achievement_rate DECIMAL(5,2) COMMENT '平均達成率',
    total_reactions_received INT NOT NULL DEFAULT 0 COMMENT '受け取ったリアクション総数',
    total_okr_contributions INT NOT NULL DEFAULT 0 COMMENT 'OKR貢献数',
    completed_objectives INT NOT NULL DEFAULT 0 COMMENT '完了した目標数',
    completed_key_results INT NOT NULL DEFAULT 0 COMMENT '完了した主要結果数',

    -- エクスポート情報
    snapshot_data JSON COMMENT '詳細データ（JSON）',
    exported_at TIMESTAMP NULL COMMENT 'エクスポート日時',
    export_format VARCHAR(20) COMMENT 'エクスポート形式',
    export_file_url VARCHAR(500) COMMENT 'エクスポートファイルURL',

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (organization_id) REFERENCES organizations(id) ON DELETE CASCADE,
    INDEX idx_tenant (tenant_id),
    INDEX idx_user (user_id),
    INDEX idx_organization (organization_id),
    INDEX idx_period (period_start, period_end),
    INDEX idx_tenant_user_period (tenant_id, user_id, period_start),
    INDEX idx_period_label (period_label),
    CHECK (export_format IS NULL OR export_format IN ('PDF', 'CSV', 'JSON'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='評価期間スナップショットテーブル';

-- 監査ログテーブル
CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL COMMENT '所属テナント',
    user_id BIGINT COMMENT '操作ユーザー',

    action VARCHAR(100) NOT NULL COMMENT 'アクション',
    entity_type VARCHAR(50) COMMENT 'エンティティタイプ',
    entity_id BIGINT COMMENT 'エンティティID',

    details JSON COMMENT '詳細情報',
    ip_address VARCHAR(45) COMMENT 'IPアドレス',
    user_agent TEXT COMMENT 'ユーザーエージェント',

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_tenant (tenant_id),
    INDEX idx_user (user_id),
    INDEX idx_action (action),
    INDEX idx_entity (entity_type, entity_id),
    INDEX idx_created_at (created_at DESC),
    INDEX idx_tenant_action (tenant_id, action),
    INDEX idx_tenant_created (tenant_id, created_at DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='監査ログテーブル';

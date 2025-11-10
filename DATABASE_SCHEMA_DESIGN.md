# データベーススキーマ設計書
**Letteral - 企業向け匿名進捗・目標開示プラットフォーム**

作成日: 2025-11-07
バージョン: 1.0

---

## 📋 目次

1. [設計方針](#設計方針)
2. [新規テーブル](#新規テーブル)
3. [既存テーブルの拡張](#既存テーブルの拡張)
4. [ER図](#er図)
5. [インデックス戦略](#インデックス戦略)
6. [マイグレーション戦略](#マイグレーション戦略)

---

## 設計方針

### 基本原則

1. **テナント分離**: すべてのテーブルに`tenant_id`を追加し、データを完全に分離
2. **匿名性の保護**: 匿名IDと実名IDの紐付けは暗号化して管理
3. **スケーラビリティ**: 適切なインデックスとパーティショニング戦略
4. **監査証跡**: 重要な操作は監査ログに記録
5. **既存データの保持**: 既存機能は維持しつつ、新機能を追加

### 命名規則

- テーブル名: `snake_case` (複数形)
- カラム名: `snake_case`
- 外部キー: `{参照テーブル名}_id`
- インデックス: `idx_{テーブル名}_{カラム名}`
- ユニーク制約: `uk_{テーブル名}_{カラム名}`

---

## 新規テーブル

### 1. tenants（テナント管理）

組織・企業の基本情報を管理。すべてのデータはテナントに紐付く。

```sql
CREATE TABLE tenants (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(200) NOT NULL,                    -- 企業名
    slug VARCHAR(100) NOT NULL UNIQUE,              -- URL識別子 (例: acme-corp)
    plan_type VARCHAR(20) NOT NULL DEFAULT 'FREE',  -- FREE, PRO, ENTERPRISE
    max_users INT NOT NULL DEFAULT 50,              -- プラン別の最大ユーザー数
    max_storage_gb INT NOT NULL DEFAULT 5,          -- ストレージ容量
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',   -- ACTIVE, SUSPENDED, CLOSED
    contact_email VARCHAR(100),                     -- 管理者連絡先
    settings JSON,                                  -- カスタム設定（JSON）
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_slug (slug),
    INDEX idx_status (status),
    INDEX idx_plan_type (plan_type),
    CHECK (plan_type IN ('FREE', 'PRO', 'ENTERPRISE')),
    CHECK (status IN ('ACTIVE', 'SUSPENDED', 'CLOSED')),
    CHECK (max_users > 0),
    CHECK (max_storage_gb > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

### 2. organizations（組織階層）

会社 → 本部 → 部 → 課 → チーム のような階層構造を表現。

```sql
CREATE TABLE organizations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,                     -- 所属テナント
    parent_id BIGINT,                              -- 親組織（NULL = ルート）
    name VARCHAR(200) NOT NULL,                    -- 組織名
    organization_type VARCHAR(50),                 -- COMPANY, DIVISION, DEPARTMENT, TEAM, etc.
    description TEXT,                              -- 説明
    level INT NOT NULL DEFAULT 1,                  -- 階層レベル（1=ルート）
    path VARCHAR(1000),                            -- 階層パス（例: /1/3/7）
    display_order INT NOT NULL DEFAULT 0,          -- 表示順序
    is_active BOOLEAN NOT NULL DEFAULT TRUE,       -- アクティブ状態
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE,
    FOREIGN KEY (parent_id) REFERENCES organizations(id) ON DELETE CASCADE,
    INDEX idx_tenant (tenant_id),
    INDEX idx_parent (parent_id),
    INDEX idx_path (path),
    INDEX idx_level (level),
    INDEX idx_tenant_active (tenant_id, is_active),
    INDEX idx_tenant_parent (tenant_id, parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

### 3. organization_members（組織メンバーシップ）

ユーザーと組織の紐付け。1人が複数の組織に所属可能。

```sql
CREATE TABLE organization_members (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    organization_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'MEMBER',    -- OWNER, ADMIN, MODERATOR, MEMBER
    is_primary BOOLEAN NOT NULL DEFAULT FALSE,     -- 主所属かどうか
    joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE,
    FOREIGN KEY (organization_id) REFERENCES organizations(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY uk_org_user (organization_id, user_id),
    INDEX idx_tenant (tenant_id),
    INDEX idx_organization (organization_id),
    INDEX idx_user (user_id),
    INDEX idx_tenant_user (tenant_id, user_id),
    CHECK (role IN ('OWNER', 'ADMIN', 'MODERATOR', 'MEMBER'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

### 4. anonymous_profiles（匿名プロフィール）

組織ごとに異なる匿名IDを割り当て。実名との紐付けは暗号化。

```sql
CREATE TABLE anonymous_profiles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    organization_id BIGINT NOT NULL,               -- 組織ごとに異なる匿名ID
    user_id BIGINT NOT NULL,                       -- 実ユーザー（暗号化推奨）
    anonymous_id VARCHAR(100) NOT NULL,            -- 匿名識別子（例: anonymous_a7f3）
    display_name VARCHAR(100),                     -- 表示名（仮名モード用）
    avatar_url VARCHAR(500),                       -- アバター画像
    anonymity_level VARCHAR(20) NOT NULL DEFAULT 'FULL',  -- FULL, PSEUDONYM, REAL
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE,
    FOREIGN KEY (organization_id) REFERENCES organizations(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY uk_org_user_anonymous (organization_id, user_id),
    UNIQUE KEY uk_tenant_anonymous_id (tenant_id, anonymous_id),
    INDEX idx_tenant (tenant_id),
    INDEX idx_organization (organization_id),
    INDEX idx_user (user_id),
    INDEX idx_anonymous_id (anonymous_id),
    CHECK (anonymity_level IN ('FULL', 'PSEUDONYM', 'REAL'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

### 5. progress_posts（進捗投稿）

日々の目標・進捗・課題を記録。OKRとタグ付け可能。

```sql
CREATE TABLE progress_posts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    organization_id BIGINT NOT NULL,               -- 投稿元組織
    author_id BIGINT NOT NULL,                     -- 投稿者（匿名プロフィールID）
    is_anonymous BOOLEAN NOT NULL DEFAULT TRUE,    -- 匿名投稿かどうか

    -- 投稿内容
    post_type VARCHAR(20) NOT NULL DEFAULT 'PROGRESS',  -- PROGRESS, GOAL, BLOCKER, LEARNING
    title VARCHAR(200),
    content TEXT NOT NULL,
    achievement_rate INT,                          -- 達成率（0-100）
    blockers TEXT,                                 -- ブロッカー
    learnings TEXT,                                -- 学び
    next_action TEXT,                              -- 次の一手

    -- 公開範囲
    visibility VARCHAR(20) NOT NULL DEFAULT 'ORGANIZATION',  -- PRIVATE, TEAM, DEPARTMENT, ORGANIZATION, COMPANY
    target_organization_id BIGINT,                 -- 公開先組織

    -- メタデータ
    post_date DATE NOT NULL,                       -- 投稿対象日
    tags JSON,                                     -- タグ（配列）
    attachments JSON,                              -- 添付ファイル（配列）

    -- 統計
    reaction_count INT NOT NULL DEFAULT 0,
    comment_count INT NOT NULL DEFAULT 0,
    view_count INT NOT NULL DEFAULT 0,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE,
    FOREIGN KEY (organization_id) REFERENCES organizations(id) ON DELETE CASCADE,
    FOREIGN KEY (author_id) REFERENCES anonymous_profiles(id) ON DELETE CASCADE,
    FOREIGN KEY (target_organization_id) REFERENCES organizations(id) ON DELETE SET NULL,
    INDEX idx_tenant (tenant_id),
    INDEX idx_organization (organization_id),
    INDEX idx_author (author_id),
    INDEX idx_post_date (post_date),
    INDEX idx_post_type (post_type),
    INDEX idx_visibility (visibility),
    INDEX idx_tenant_date (tenant_id, post_date),
    INDEX idx_org_date (organization_id, post_date),
    CHECK (post_type IN ('PROGRESS', 'GOAL', 'BLOCKER', 'LEARNING', 'REFLECTION')),
    CHECK (visibility IN ('PRIVATE', 'TEAM', 'DEPARTMENT', 'ORGANIZATION', 'COMPANY')),
    CHECK (achievement_rate IS NULL OR (achievement_rate >= 0 AND achievement_rate <= 100))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

### 6. okr_objectives（OKR目標）

組織・チーム・個人のObjectiveを管理。

```sql
CREATE TABLE okr_objectives (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    organization_id BIGINT,                        -- 組織目標（NULLは個人目標）
    owner_id BIGINT,                               -- 目標オーナー（個人目標の場合）
    parent_objective_id BIGINT,                    -- 親目標（階層化）

    title VARCHAR(300) NOT NULL,
    description TEXT,
    target_quarter VARCHAR(10),                    -- 例: 2025-Q1
    start_date DATE,
    end_date DATE,

    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',  -- DRAFT, ACTIVE, COMPLETED, CANCELLED
    progress_rate INT NOT NULL DEFAULT 0,          -- 進捗率（0-100）

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE,
    FOREIGN KEY (organization_id) REFERENCES organizations(id) ON DELETE CASCADE,
    FOREIGN KEY (owner_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (parent_objective_id) REFERENCES okr_objectives(id) ON DELETE CASCADE,
    INDEX idx_tenant (tenant_id),
    INDEX idx_organization (organization_id),
    INDEX idx_owner (owner_id),
    INDEX idx_quarter (target_quarter),
    INDEX idx_status (status),
    INDEX idx_tenant_quarter (tenant_id, target_quarter),
    CHECK (status IN ('DRAFT', 'ACTIVE', 'COMPLETED', 'CANCELLED')),
    CHECK (progress_rate >= 0 AND progress_rate <= 100)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

### 7. okr_key_results（OKR主要結果）

Objectiveに対するKey Resultsを管理。

```sql
CREATE TABLE okr_key_results (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    objective_id BIGINT NOT NULL,

    title VARCHAR(300) NOT NULL,
    description TEXT,
    metric_type VARCHAR(20) NOT NULL,              -- NUMBER, PERCENTAGE, BOOLEAN
    baseline_value DECIMAL(15,2),                  -- 開始値
    target_value DECIMAL(15,2) NOT NULL,           -- 目標値
    current_value DECIMAL(15,2) NOT NULL DEFAULT 0,-- 現在値
    unit VARCHAR(50),                              -- 単位（例: 件、%、円）

    progress_rate INT NOT NULL DEFAULT 0,          -- 進捗率（0-100）
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE,
    FOREIGN KEY (objective_id) REFERENCES okr_objectives(id) ON DELETE CASCADE,
    INDEX idx_tenant (tenant_id),
    INDEX idx_objective (objective_id),
    INDEX idx_status (status),
    CHECK (metric_type IN ('NUMBER', 'PERCENTAGE', 'BOOLEAN')),
    CHECK (status IN ('ACTIVE', 'COMPLETED', 'CANCELLED')),
    CHECK (progress_rate >= 0 AND progress_rate <= 100)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

### 8. post_okr_links（投稿とOKRの紐付け）

進捗投稿とOKRを関連付ける中間テーブル。

```sql
CREATE TABLE post_okr_links (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    post_id BIGINT NOT NULL,
    objective_id BIGINT,
    key_result_id BIGINT,
    contribution_note TEXT,                        -- この投稿がOKRにどう貢献したか
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE,
    FOREIGN KEY (post_id) REFERENCES progress_posts(id) ON DELETE CASCADE,
    FOREIGN KEY (objective_id) REFERENCES okr_objectives(id) ON DELETE CASCADE,
    FOREIGN KEY (key_result_id) REFERENCES okr_key_results(id) ON DELETE CASCADE,
    INDEX idx_tenant (tenant_id),
    INDEX idx_post (post_id),
    INDEX idx_objective (objective_id),
    INDEX idx_key_result (key_result_id),
    CHECK (objective_id IS NOT NULL OR key_result_id IS NOT NULL)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

### 9. post_reactions（投稿リアクション）

投稿への称賛・共感・サポートなどのリアクション。

```sql
CREATE TABLE post_reactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    post_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    is_anonymous BOOLEAN NOT NULL DEFAULT TRUE,

    reaction_type VARCHAR(20) NOT NULL,            -- PRAISE, EMPATHY, SUPPORT, QUESTION
    comment TEXT,                                  -- コメント（任意）

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE,
    FOREIGN KEY (post_id) REFERENCES progress_posts(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY uk_post_user_reaction (post_id, user_id, reaction_type),
    INDEX idx_tenant (tenant_id),
    INDEX idx_post (post_id),
    INDEX idx_user (user_id),
    INDEX idx_reaction_type (reaction_type),
    CHECK (reaction_type IN ('PRAISE', 'EMPATHY', 'SUPPORT', 'QUESTION', 'HEART', 'THUMBS_UP'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

### 10. policy_settings（ポリシー設定）

組織ごとの匿名度・公開範囲・モデレーションポリシー。

```sql
CREATE TABLE policy_settings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    organization_id BIGINT NOT NULL,

    -- 匿名性ポリシー
    default_anonymity_level VARCHAR(20) NOT NULL DEFAULT 'PSEUDONYM',
    allow_full_anonymous BOOLEAN NOT NULL DEFAULT TRUE,
    allow_real_name BOOLEAN NOT NULL DEFAULT TRUE,

    -- 公開範囲ポリシー
    default_visibility VARCHAR(20) NOT NULL DEFAULT 'ORGANIZATION',
    allow_company_wide BOOLEAN NOT NULL DEFAULT FALSE,

    -- 投稿制限
    require_approval BOOLEAN NOT NULL DEFAULT FALSE,
    min_post_length INT NOT NULL DEFAULT 10,
    max_post_length INT NOT NULL DEFAULT 5000,

    -- モデレーション
    enable_ng_word_filter BOOLEAN NOT NULL DEFAULT TRUE,
    enable_auto_moderation BOOLEAN NOT NULL DEFAULT TRUE,
    ng_words JSON,                                 -- NGワード辞書

    -- その他
    settings JSON,                                 -- カスタム設定

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE,
    FOREIGN KEY (organization_id) REFERENCES organizations(id) ON DELETE CASCADE,
    UNIQUE KEY uk_org_policy (organization_id),
    INDEX idx_tenant (tenant_id),
    INDEX idx_organization (organization_id),
    CHECK (default_anonymity_level IN ('FULL', 'PSEUDONYM', 'REAL')),
    CHECK (default_visibility IN ('PRIVATE', 'TEAM', 'DEPARTMENT', 'ORGANIZATION', 'COMPANY'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

### 11. evaluation_snapshots（評価スナップショット）

評価期間ごとの成果スナップショット。

```sql
CREATE TABLE evaluation_snapshots (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    organization_id BIGINT NOT NULL,

    period_start DATE NOT NULL,
    period_end DATE NOT NULL,
    period_label VARCHAR(100),                     -- 例: 2025年Q1

    -- 集計データ
    total_posts INT NOT NULL DEFAULT 0,
    total_achievements INT NOT NULL DEFAULT 0,
    avg_achievement_rate DECIMAL(5,2),
    total_reactions_received INT NOT NULL DEFAULT 0,
    total_okr_contributions INT NOT NULL DEFAULT 0,

    -- エクスポート情報
    snapshot_data JSON,                            -- 詳細データ（JSON）
    exported_at TIMESTAMP NULL,
    export_format VARCHAR(20),                     -- PDF, CSV

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (organization_id) REFERENCES organizations(id) ON DELETE CASCADE,
    INDEX idx_tenant (tenant_id),
    INDEX idx_user (user_id),
    INDEX idx_period (period_start, period_end),
    INDEX idx_tenant_user_period (tenant_id, user_id, period_start),
    CHECK (export_format IS NULL OR export_format IN ('PDF', 'CSV'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

### 12. audit_logs（監査ログ）

重要な操作を記録。

```sql
CREATE TABLE audit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    user_id BIGINT,

    action VARCHAR(100) NOT NULL,                  -- CREATE_POST, DELETE_POST, EXPORT_DATA, etc.
    entity_type VARCHAR(50),                       -- POST, USER, ORGANIZATION, etc.
    entity_id BIGINT,

    details JSON,                                  -- 詳細情報
    ip_address VARCHAR(45),
    user_agent TEXT,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL,
    INDEX idx_tenant (tenant_id),
    INDEX idx_user (user_id),
    INDEX idx_action (action),
    INDEX idx_entity (entity_type, entity_id),
    INDEX idx_created_at (created_at),
    INDEX idx_tenant_action (tenant_id, action)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

---

## 既存テーブルの拡張

### users テーブルの拡張

```sql
-- 既存のusersテーブルに以下のカラムを追加
ALTER TABLE users
ADD COLUMN tenant_id BIGINT AFTER id,
ADD COLUMN primary_organization_id BIGINT AFTER tenant_id,
ADD COLUMN role VARCHAR(20) NOT NULL DEFAULT 'USER' AFTER password,
ADD COLUMN is_active BOOLEAN NOT NULL DEFAULT TRUE AFTER role,
ADD COLUMN employee_id VARCHAR(50) AFTER email,
ADD COLUMN department VARCHAR(100) AFTER display_name,
ADD COLUMN position VARCHAR(100) AFTER department,
ADD COLUMN hire_date DATE AFTER position,
ADD COLUMN settings JSON AFTER last_login,
ADD COLUMN updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP AFTER created_at,

ADD FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE,
ADD FOREIGN KEY (primary_organization_id) REFERENCES organizations(id) ON DELETE SET NULL,
ADD INDEX idx_tenant (tenant_id),
ADD INDEX idx_primary_org (primary_organization_id),
ADD INDEX idx_employee_id (employee_id),
ADD INDEX idx_tenant_username (tenant_id, username),
ADD CONSTRAINT CHECK (role IN ('SUPER_ADMIN', 'TENANT_ADMIN', 'ORG_ADMIN', 'MODERATOR', 'USER'));
```

### groups_table テーブルの拡張

```sql
-- 既存のgroups_tableに以下のカラムを追加
ALTER TABLE groups_table
ADD COLUMN tenant_id BIGINT AFTER id,
ADD COLUMN organization_id BIGINT AFTER tenant_id,
ADD COLUMN visibility VARCHAR(20) NOT NULL DEFAULT 'ORGANIZATION' AFTER group_type,
ADD COLUMN anonymity_policy VARCHAR(20) NOT NULL DEFAULT 'PSEUDONYM' AFTER visibility,
ADD COLUMN settings JSON AFTER max_members,
ADD COLUMN is_active BOOLEAN NOT NULL DEFAULT TRUE AFTER created_at,
ADD COLUMN updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP AFTER created_at,

ADD FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE,
ADD FOREIGN KEY (organization_id) REFERENCES organizations(id) ON DELETE SET NULL,
ADD INDEX idx_tenant (tenant_id),
ADD INDEX idx_organization (organization_id),
ADD INDEX idx_visibility (visibility),
ADD INDEX idx_tenant_type (tenant_id, group_type),
ADD CONSTRAINT CHECK (visibility IN ('PRIVATE', 'TEAM', 'ORGANIZATION', 'COMPANY')),
ADD CONSTRAINT CHECK (anonymity_policy IN ('FULL', 'PSEUDONYM', 'REAL'));
```

### chat_messages テーブルの拡張

```sql
-- 既存のchat_messagesに以下のカラムを追加
ALTER TABLE chat_messages
ADD COLUMN tenant_id BIGINT AFTER id,
ADD COLUMN is_anonymous BOOLEAN NOT NULL DEFAULT FALSE AFTER sender_id,
ADD COLUMN anonymous_sender_id BIGINT AFTER is_anonymous,
ADD COLUMN parent_message_id BIGINT AFTER room_id,
ADD COLUMN post_template VARCHAR(20) AFTER message_type,
ADD COLUMN okr_tags JSON AFTER content,
ADD COLUMN attachments JSON AFTER okr_tags,
ADD COLUMN reaction_count INT NOT NULL DEFAULT 0 AFTER created_at,
ADD COLUMN is_edited BOOLEAN NOT NULL DEFAULT FALSE AFTER reaction_count,
ADD COLUMN edited_at TIMESTAMP NULL AFTER is_edited,
ADD COLUMN updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP AFTER created_at,

ADD FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE,
ADD FOREIGN KEY (anonymous_sender_id) REFERENCES anonymous_profiles(id) ON DELETE SET NULL,
ADD FOREIGN KEY (parent_message_id) REFERENCES chat_messages(id) ON DELETE CASCADE,
ADD INDEX idx_tenant (tenant_id),
ADD INDEX idx_anonymous_sender (anonymous_sender_id),
ADD INDEX idx_parent (parent_message_id),
ADD INDEX idx_tenant_room (tenant_id, room_id),
ADD CONSTRAINT CHECK (post_template IS NULL OR post_template IN ('DAILY', 'BLOCKER', 'LEARNING', 'REFLECTION'));
```

### friends テーブルの拡張

```sql
-- 既存のfriendsテーブルに以下のカラムを追加
ALTER TABLE friends
ADD COLUMN tenant_id BIGINT AFTER id,
ADD COLUMN updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP AFTER responded_at,

ADD FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE,
ADD INDEX idx_tenant (tenant_id);
```

---

## ER図

```
┌─────────────┐
│  tenants    │
└──────┬──────┘
       │
       ├──────────────┬──────────────┬──────────────┬──────────────┐
       │              │              │              │              │
┌──────▼──────┐ ┌────▼─────┐ ┌─────▼──────┐ ┌────▼─────┐ ┌─────▼──────┐
│    users    │ │ organiz- │ │  progress_ │ │   okr_   │ │   policy_  │
│             │ │  ations  │ │   posts    │ │objectives│ │  settings  │
└──────┬──────┘ └────┬─────┘ └─────┬──────┘ └────┬─────┘ └────────────┘
       │             │              │             │
       │             ├──────────────┼─────────────┤
       │             │              │             │
       │      ┌──────▼──────┐ ┌────▼─────┐ ┌─────▼──────┐
       │      │ anonymous_  │ │   post_  │ │    okr_    │
       │      │  profiles   │ │reactions │ │key_results │
       │      └─────────────┘ └──────────┘ └────────────┘
       │
       ├──────────────┬──────────────┐
       │              │              │
┌──────▼──────┐ ┌────▼─────┐ ┌─────▼──────┐
│   friends   │ │  groups_ │ │   chat_    │
│             │ │  table   │ │  messages  │
└─────────────┘ └──────────┘ └────────────┘
```

---

## インデックス戦略

### パフォーマンス重視のインデックス

1. **テナント分離**: すべてのクエリに`tenant_id`を含めるため、複合インデックスの先頭に配置
2. **時系列検索**: `created_at`, `post_date`などは必ずインデックス化
3. **複合インデックス**: 頻繁に一緒に検索される条件は複合インデックス化
4. **JSON カラム**: 頻繁に検索するJSON内のフィールドは仮想カラム+インデックス化を検討

### 推奨される追加インデックス例

```sql
-- 進捗投稿: テナント + 組織 + 日付での検索
CREATE INDEX idx_posts_tenant_org_date
ON progress_posts(tenant_id, organization_id, post_date DESC);

-- OKR: テナント + 四半期 + ステータス
CREATE INDEX idx_okr_tenant_quarter_status
ON okr_objectives(tenant_id, target_quarter, status);

-- リアクション: 投稿 + リアクションタイプ
CREATE INDEX idx_reactions_post_type
ON post_reactions(post_id, reaction_type);
```

---

## マイグレーション戦略

### Phase 1: 新規テーブル作成（Week 2）

1. `tenants` テーブル作成
2. `organizations` テーブル作成
3. `anonymous_profiles` テーブル作成
4. `progress_posts` テーブル作成
5. `okr_objectives` / `okr_key_results` テーブル作成
6. 関連テーブル（reactions, links, policy）作成

### Phase 2: 既存テーブル拡張（Week 2-3）

1. `users` テーブルにカラム追加
   - デフォルトテナントを作成
   - 既存ユーザーをデフォルトテナントに紐付け
2. `groups_table` テーブルにカラム追加
3. `chat_messages` テーブルにカラム追加
4. `friends` テーブルにカラム追加

### Phase 3: データマイグレーション（Week 3）

1. 既存ユーザーデータの移行
2. 既存グループデータの移行
3. 既存メッセージデータの移行

### マイグレーションスクリプト命名規則

```
V3__Create_Tenant_Tables.sql
V4__Create_Organization_Tables.sql
V5__Create_Progress_Post_Tables.sql
V6__Create_OKR_Tables.sql
V7__Alter_Users_Table.sql
V8__Alter_Groups_Table.sql
V9__Alter_Chat_Messages_Table.sql
V10__Migrate_Existing_Data.sql
```

---

## 次のステップ

1. [ ] マイグレーションSQLスクリプトの作成
2. [ ] Javaエンティティクラスの作成
3. [ ] Repositoryインターフェースの作成
4. [ ] テストデータの投入スクリプト作成
5. [ ] パフォーマンステスト

---

**作成者**: Claude
**最終更新**: 2025-11-07
**バージョン**: 1.0

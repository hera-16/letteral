# Letteral 新仕様 設計書

## 概要
この設計書は、Letteralプラットフォームの新仕様を実装するための詳細設計をまとめたものです。

---

## 1. 権限体系の整理

### 1.1 権限階級（Role）
権限表PDFに基づく階級定義：

```
CEO（社長） > 部長 > 課長 > PM > 一般メンバー
```

### 1.2 権限昇格ルール
- 自分の階級と同じ階級までは権限を付与できる
- 自分より上位権限への昇格は不可

| 自分の階級 | 昇格可能範囲 |
|-----------|-------------|
| CEO（社長）| すべて |
| 部長 | 部長 / 課長 / PM / 一般 |
| 課長 | 課長 / PM / 一般 |
| PM | PM / 一般 |
| 一般 | 権限編集不可 |

### 1.3 Box（箱）の種類と権限

#### 全社箱（Company Box）
- **投稿**: 全員可能
- **閲覧**: PM以上のみ全投稿閲覧可能、一般メンバーは自分の投稿のみ閲覧可能
- **分析**: 社長のみ
- **返信**: PM以上（署名表示）

#### 部署箱（Department Box）
- **閲覧**: 部長 + 所属部署のメンバー
- **分析**: 部長
- **返信**: PM以上

#### 課箱（Section Box）
- **閲覧**: 課長 + 所属課メンバー
- **分析**: 課長
- **返信**: PM以上

#### プロジェクト箱（Project Box）
- **閲覧**: PM + プロジェクトメンバー
- **分析**: PM
- **返信**: PM以上

### 1.4 返信ルール
- 返信可能: PM以上（社長 / 部長 / 課長 / PM）
- 返信は匿名ではない（署名表示）
- 閲覧可能:
  - 投稿者本人
  - PM以上
  - 一般メンバーは他者の返信は見えない

---

## 2. データベーススキーマ変更

### 2.1 新規テーブル: `organization_invites`（招待URL管理）

```sql
CREATE TABLE organization_invites (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    tenant_id BIGINT NOT NULL,
    organization_id BIGINT NOT NULL,
    invite_code VARCHAR(64) UNIQUE NOT NULL,
    default_role VARCHAR(50) NOT NULL DEFAULT 'GENERAL',
    expires_at DATETIME,
    max_uses INT DEFAULT NULL,
    current_uses INT DEFAULT 0,
    is_active BOOLEAN DEFAULT TRUE,
    created_by BIGINT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (tenant_id) REFERENCES tenants(id),
    FOREIGN KEY (organization_id) REFERENCES organizations(id),
    FOREIGN KEY (created_by) REFERENCES users(id),
    INDEX idx_invite_code (invite_code),
    INDEX idx_tenant_org (tenant_id, organization_id)
);
```

### 2.2 `progress_posts` テーブルの拡張

既存の `progress_posts` テーブルに以下のカラムを追加：

```sql
ALTER TABLE progress_posts
ADD COLUMN box_type VARCHAR(50) DEFAULT 'COMPANY',  -- COMPANY, DEPARTMENT, SECTION, PROJECT
ADD COLUMN organization_id BIGINT,                   -- 投稿先組織
ADD INDEX idx_box_type (box_type),
ADD INDEX idx_organization_id (organization_id),
ADD FOREIGN KEY (organization_id) REFERENCES organizations(id);
```

**移行戦略**:
- 既存の投稿は `box_type = 'COMPANY'` として扱う
- `organization_id` は NULL 許容（既存データとの互換性）
- 新規投稿は必ず `organization_id` を指定

### 2.3 `users` テーブルの権限フィールド確認

既存のユーザーテーブルに権限階級を格納するフィールドが必要：

```sql
-- すでに存在するか確認が必要
ALTER TABLE users
ADD COLUMN role VARCHAR(50) DEFAULT 'GENERAL';  -- CEO, MANAGER, SECTION_CHIEF, PM, GENERAL
```

---

## 3. 組織管理画面の整理

### 3.1 子会社一覧表示

**提案**: ツリー構造表示

理由:
- 親会社 → 子会社 → 孫会社の階層関係を視覚的に理解しやすい
- 組織の全体像を一目で把握できる
- クリックで展開/折りたたみ可能

**UI構成**:
```
組織管理画面
├── 組織ツリー表示
│   ├── 親会社A
│   │   ├── 子会社A-1
│   │   └── 子会社A-2
│   └── 親会社B
│       └── 子会社B-1
└── 操作ボタン
    ├── 新規組織追加
    └── 組織編集
```

**現在の画面からの変更点**:
- ❌ メンバー一覧は表示しない
- ✅ 組織一覧・子会社一覧に絞る
- ✅ ツリー構造で階層を表現

---

## 4. 投稿一覧の表示方式

### 4.1 組織ツリー構造での表示

```
投稿一覧画面
├── 左サイドバー: 組織ツリー
│   └── クリックで組織を選択
└── 右メインエリア: 選択された組織の投稿タイムライン
    ├── 投稿1（新しい）
    ├── 投稿2
    └── 投稿3（古い）
```

### 4.2 各組織ページの構成

```
[組織名ヘッダー]
├── メンバー一覧ボタン → モーダルまたは別ページで表示
└── 投稿一覧（タイムライン形式）
    ├── 投稿A（2025-11-26 10:00）
    ├── 投稿B（2025-11-25 15:30）
    └── 投稿C（2025-11-24 09:00）
```

### 4.3 メンバー一覧のソートルール

1. **第1ソート**: 権限階級が高い順
   - CEO → 部長 → 課長 → PM → 一般メンバー

2. **第2ソート**: 同じ権限階級内では「あいうえお順」
   - 日本語の display_name でソート

**実装方針**:
```java
// 権限階級の優先度マップ
Map<String, Integer> rolePriority = Map.of(
    "CEO", 1,
    "MANAGER", 2,
    "SECTION_CHIEF", 3,
    "PM", 4,
    "GENERAL", 5
);

// ソート: rolePriority → displayName（あいうえお順）
users.sort(Comparator
    .comparing(user -> rolePriority.get(user.getRole()))
    .thenComparing(User::getDisplayName, Collator.getInstance(Locale.JAPANESE))
);
```

---

## 5. 投稿機能の変更

### 5.1 投稿時の組織指定

**UI フロー**:
1. 投稿作成画面を開く
2. 投稿先選択:
   - Box タイプ選択: 全社箱 / 部署箱 / 課箱 / プロジェクト箱
   - 組織ツリーから対象組織を選択
3. 本文入力
4. 投稿

**バックエンド API**:
```json
POST /api/progress-posts
{
  "content": "今週の進捗です...",
  "boxType": "DEPARTMENT",
  "organizationId": 42,
  "isAnonymous": true
}
```

### 5.2 既存投稿との互換性

**移行案**:
1. 既存の投稿は `box_type = 'COMPANY'` に設定
2. `organization_id` は NULL を許容
3. NULL の場合は「全社」として扱う
4. フロントエンドでは「全社箱」として表示

---

## 6. 権限問題の実装

### 6.1 アクセス制御の実装方針

#### 6.1.1 投稿の閲覧権限チェック

```java
public boolean canViewPost(User user, ProgressPost post) {
    String boxType = post.getBoxType();

    switch (boxType) {
        case "COMPANY":
            // PM以上は全投稿閲覧可能
            if (user.getRole().isPMOrAbove()) {
                return true;
            }
            // 一般メンバーは自分の投稿のみ
            return post.getAuthorId().equals(user.getId());

        case "DEPARTMENT":
            // 部長 または 所属部署のメンバー
            return user.getRole().isManagerOrAbove()
                || user.belongsToOrganization(post.getOrganizationId());

        case "SECTION":
            // 課長 または 所属課メンバー
            return user.getRole().isSectionChiefOrAbove()
                || user.belongsToOrganization(post.getOrganizationId());

        case "PROJECT":
            // PM または プロジェクトメンバー
            return user.getRole().isPMOrAbove()
                || user.belongsToOrganization(post.getOrganizationId());

        default:
            return false;
    }
}
```

#### 6.1.2 返信の閲覧権限チェック

```java
public boolean canViewReply(User user, Reply reply) {
    ProgressPost post = reply.getPost();

    // 投稿者本人
    if (post.getAuthorId().equals(user.getId())) {
        return true;
    }

    // PM以上
    if (user.getRole().isPMOrAbove()) {
        return true;
    }

    // 一般メンバーは他者の返信は見えない
    return false;
}
```

### 6.2 権限Enumの定義

```java
public enum Role {
    CEO(1, "社長"),
    MANAGER(2, "部長"),
    SECTION_CHIEF(3, "課長"),
    PM(4, "PM"),
    GENERAL(5, "一般メンバー");

    private final int level;
    private final String displayName;

    public boolean isPMOrAbove() {
        return this.level <= PM.level;
    }

    public boolean isSectionChiefOrAbove() {
        return this.level <= SECTION_CHIEF.level;
    }

    public boolean isManagerOrAbove() {
        return this.level <= MANAGER.level;
    }

    public boolean canPromoteTo(Role targetRole) {
        return this.level <= targetRole.level;
    }
}
```

---

## 7. 招待URL機能

### 7.1 招待URLの仕組み

**フロー**:
1. 管理者が招待URLを発行（組織・デフォルト権限・有効期限を指定）
2. システムがユニークな招待コードを生成
3. 招待URL: `https://letteral.example.com/invite/{inviteCode}`
4. 社員がURLにアクセス
5. 初回登録フォーム表示（ユーザー名・パスワード・メールアドレス）
6. 登録完了 → 指定された組織に自動参加

### 7.2 API設計

#### 招待URL発行
```
POST /api/invites
Request:
{
  "organizationId": 42,
  "defaultRole": "GENERAL",
  "expiresAt": "2025-12-31T23:59:59",
  "maxUses": 100
}

Response:
{
  "inviteCode": "abc123xyz789",
  "inviteUrl": "https://letteral.example.com/invite/abc123xyz789",
  "expiresAt": "2025-12-31T23:59:59"
}
```

#### 招待URLから登録
```
POST /api/auth/register-with-invite
Request:
{
  "inviteCode": "abc123xyz789",
  "username": "tanaka_taro",
  "email": "tanaka@example.com",
  "password": "securePassword123",
  "displayName": "田中太郎"
}

Response:
{
  "message": "登録が完了しました",
  "userId": 123,
  "organizationId": 42,
  "role": "GENERAL"
}
```

### 7.3 認証フロー

**提案**: トークンベース認証（現在のJWT方式を拡張）

1. 招待コードの検証
   - 有効期限チェック
   - 使用回数上限チェック
   - アクティブ状態チェック

2. ユーザー登録
   - 通常の登録処理
   - 組織への自動参加
   - デフォルト権限の付与

3. 使用回数のインクリメント

---

## 8. 実装の優先順位

### Phase 1: データベース・バックエンド基盤
1. データベーススキーマ変更（マイグレーション）
2. Role Enum の実装
3. 招待URL機能のバックエンド実装
4. アクセス制御ロジックの実装

### Phase 2: API拡張
1. 投稿API の組織紐付け対応
2. メンバー一覧API のソート機能実装
3. 組織ツリーAPI の整備

### Phase 3: フロントエンド実装
1. 組織管理画面の整理（子会社一覧）
2. 組織ツリー構造での投稿一覧表示
3. 投稿作成時の組織選択UI
4. 招待URL機能のUI

### Phase 4: テスト・検証
1. 権限チェックのテスト
2. 招待URL機能のテスト
3. エンドツーエンドテスト

---

## 9. 既存機能との互換性

### 9.1 既存投稿の扱い
- 既存投稿は `box_type = 'COMPANY'`, `organization_id = NULL` として扱う
- 閲覧権限は全社箱のルールを適用

### 9.2 既存ユーザーの権限
- デフォルトで `role = 'GENERAL'` を設定
- 管理者が手動で権限を昇格

---

## まとめ

この設計書に基づいて実装を進めることで、Letteralプラットフォームに以下の機能が追加されます：

1. ✅ 子会社一覧表示（ツリー構造）
2. ✅ 組織ツリー構造での投稿一覧表示
3. ✅ メンバー一覧のソート（権限階級順→あいうえお順）
4. ✅ 投稿時の組織指定機能
5. ✅ Box（箱）ベースの詳細な権限制御
6. ✅ URL招待機能（初期導入の手間削減）

次のステップ: データベースマイグレーションファイルの作成から開始します。

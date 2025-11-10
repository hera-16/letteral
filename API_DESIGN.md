# Letteral API設計書 v1.0

## 目次
- [概要](#概要)
- [共通仕様](#共通仕様)
- [認証・テナント管理API](#認証テナント管理api)
- [匿名プロファイル管理API](#匿名プロファイル管理api)
- [進捗投稿API](#進捗投稿api)
- [OKR管理API](#okr管理api)
- [評価・分析API](#評価分析api)
- [監査ログAPI](#監査ログapi)
- [エラーレスポンス](#エラーレスポンス)

---

## 概要

### ベースURL
```
https://api.letteral.com/v1
```

### 対象システム
企業向け匿名進捗共有プラットフォーム「Letteral」のバックエンドAPI

### 主要機能
1. マルチテナント対応（企業・組織単位の完全分離）
2. 匿名プロファイル管理（組織ごとに異なる匿名ID）
3. 進捗投稿・タイムライン
4. OKR管理・進捗リンク
5. 評価スナップショット・分析
6. 監査ログ記録

---

## 共通仕様

### 認証方式
- **JWT (JSON Web Token)** を使用
- Authorization Header形式: `Authorization: Bearer <token>`

### リクエストヘッダー
```http
Content-Type: application/json
Authorization: Bearer <JWT_TOKEN>
X-Tenant-ID: <TENANT_ID>  # テナント分離用（必須）
```

### ページネーション
リスト取得系APIは以下のクエリパラメータに対応：
```
?page=0          # ページ番号（0始まり）
?size=20         # 1ページあたりの件数（デフォルト: 20, 最大: 100）
?sort=createdAt,desc  # ソート順（フィールド名,方向）
```

**レスポンス形式:**
```json
{
  "content": [...],
  "page": {
    "number": 0,
    "size": 20,
    "totalElements": 150,
    "totalPages": 8
  }
}
```

### 日時フォーマット
ISO 8601形式を使用
```
2024-01-15T10:30:00Z
```

### 権限チェック
各エンドポイントは以下の権限レベルを要求：
- `SYSTEM_ADMIN`: システム管理者
- `TENANT_ADMIN`: テナント管理者
- `ORG_ADMIN`: 組織管理者
- `MEMBER`: 一般メンバー

---

## 認証・テナント管理API

### 1. テナント登録

#### `POST /tenants`

新しいテナント（企業）を登録します。

**権限:** `SYSTEM_ADMIN`

**リクエストボディ:**
```json
{
  "name": "株式会社サンプル",
  "slug": "sample-corp",
  "plan": "ENTERPRISE",
  "maxUsers": 500,
  "adminEmail": "admin@sample.com",
  "adminName": "山田太郎"
}
```

**レスポンス:** `201 Created`
```json
{
  "id": "01H8X3Z...",
  "name": "株式会社サンプル",
  "slug": "sample-corp",
  "plan": "ENTERPRISE",
  "status": "ACTIVE",
  "maxUsers": 500,
  "createdAt": "2024-01-15T10:30:00Z",
  "adminUser": {
    "id": "user_123",
    "email": "admin@sample.com",
    "name": "山田太郎"
  }
}
```

---

### 2. テナント情報取得

#### `GET /tenants/{tenantId}`

**権限:** `TENANT_ADMIN`, `ORG_ADMIN`, `MEMBER`

**レスポンス:** `200 OK`
```json
{
  "id": "01H8X3Z...",
  "name": "株式会社サンプル",
  "slug": "sample-corp",
  "plan": "ENTERPRISE",
  "status": "ACTIVE",
  "maxUsers": 500,
  "currentUsers": 234,
  "createdAt": "2024-01-15T10:30:00Z",
  "updatedAt": "2024-01-20T15:45:00Z"
}
```

---

### 3. テナント設定更新

#### `PATCH /tenants/{tenantId}`

**権限:** `TENANT_ADMIN`

**リクエストボディ:**
```json
{
  "name": "株式会社サンプル（新社名）",
  "maxUsers": 1000
}
```

**レスポンス:** `200 OK`
```json
{
  "id": "01H8X3Z...",
  "name": "株式会社サンプル（新社名）",
  "maxUsers": 1000,
  "updatedAt": "2024-01-22T09:00:00Z"
}
```

---

### 4. 組織作成

#### `POST /organizations`

テナント内に組織（部署）を作成します。階層構造に対応。

**権限:** `TENANT_ADMIN`, `ORG_ADMIN`

**リクエストボディ:**
```json
{
  "name": "開発部",
  "slug": "dev",
  "parentId": null,
  "description": "プロダクト開発を担当する部署",
  "anonymityEnabled": true
}
```

**レスポンス:** `201 Created`
```json
{
  "id": "org_456",
  "tenantId": "01H8X3Z...",
  "name": "開発部",
  "slug": "dev",
  "fullPath": "/dev",
  "level": 1,
  "parentId": null,
  "description": "プロダクト開発を担当する部署",
  "anonymityEnabled": true,
  "createdAt": "2024-01-15T11:00:00Z"
}
```

---

### 5. 組織階層取得

#### `GET /organizations/tree`

テナント内の組織を階層構造で取得します。

**権限:** `MEMBER`以上

**クエリパラメータ:**
- `rootId` (optional): ルート組織ID（指定しない場合は全階層）
- `maxDepth` (optional): 最大階層深度

**レスポンス:** `200 OK`
```json
{
  "organizations": [
    {
      "id": "org_001",
      "name": "本社",
      "slug": "hq",
      "level": 0,
      "children": [
        {
          "id": "org_002",
          "name": "開発部",
          "slug": "dev",
          "level": 1,
          "children": [
            {
              "id": "org_003",
              "name": "フロントエンドチーム",
              "slug": "frontend",
              "level": 2,
              "children": []
            }
          ]
        }
      ]
    }
  ]
}
```

---

### 6. メンバー招待

#### `POST /organizations/{orgId}/members/invite`

組織にメンバーを招待します。

**権限:** `ORG_ADMIN`

**リクエストボディ:**
```json
{
  "email": "newuser@sample.com",
  "role": "MEMBER",
  "isPrimary": true
}
```

**レスポンス:** `201 Created`
```json
{
  "id": "member_789",
  "userId": "user_456",
  "organizationId": "org_456",
  "role": "MEMBER",
  "isPrimary": true,
  "joinedAt": "2024-01-15T12:00:00Z",
  "invitationToken": "inv_xyz123",
  "invitationExpiresAt": "2024-01-22T12:00:00Z"
}
```

---

### 7. メンバー一覧取得

#### `GET /organizations/{orgId}/members`

**権限:** `MEMBER`以上

**クエリパラメータ:**
- `role` (optional): 役割フィルタ（`ADMIN`, `MEMBER`）
- `page`, `size`, `sort`: ページネーション

**レスポンス:** `200 OK`
```json
{
  "content": [
    {
      "id": "member_001",
      "user": {
        "id": "user_123",
        "email": "user@sample.com",
        "displayName": "田中花子"
      },
      "organizationId": "org_456",
      "role": "ADMIN",
      "isPrimary": true,
      "joinedAt": "2024-01-10T09:00:00Z"
    }
  ],
  "page": {
    "number": 0,
    "size": 20,
    "totalElements": 45,
    "totalPages": 3
  }
}
```

---

## 匿名プロファイル管理API

### 8. 匿名プロファイル取得

#### `GET /anonymous-profiles/me`

現在のユーザーの全組織における匿名プロファイルを取得します。

**権限:** `MEMBER`以上

**クエリパラメータ:**
- `organizationId` (optional): 特定組織のプロファイルのみ取得

**レスポンス:** `200 OK`
```json
{
  "profiles": [
    {
      "id": "anon_001",
      "organizationId": "org_456",
      "organizationName": "開発部",
      "anonymousId": "匿名ユーザー#A7F3",
      "anonymousName": "匿名ユーザー#A7F3",
      "isActive": true,
      "createdAt": "2024-01-15T10:00:00Z"
    },
    {
      "id": "anon_002",
      "organizationId": "org_789",
      "organizationName": "営業部",
      "anonymousId": "匿名ユーザー#B2K9",
      "anonymousName": "匿名ユーザー#B2K9",
      "isActive": false,
      "createdAt": "2024-01-16T11:00:00Z"
    }
  ]
}
```

---

### 9. 匿名プロファイル切り替え

#### `POST /anonymous-profiles/{profileId}/activate`

特定組織での匿名プロファイルをアクティブ化します。

**権限:** `MEMBER`以上

**レスポンス:** `200 OK`
```json
{
  "id": "anon_001",
  "organizationId": "org_456",
  "anonymousId": "匿名ユーザー#A7F3",
  "isActive": true,
  "activatedAt": "2024-01-20T14:30:00Z"
}
```

---

## 進捗投稿API

### 10. 投稿作成

#### `POST /posts`

新しい進捗投稿を作成します。

**権限:** `MEMBER`以上

**リクエストボディ:**
```json
{
  "content": "今日はログイン機能の実装を完了しました。\nJWTトークンの検証部分で少し詰まりましたが、無事解決。",
  "postType": "DAILY_REPORT",
  "visibilityScope": "ORGANIZATION",
  "targetOrganizationId": "org_456",
  "isAnonymous": true,
  "tags": ["開発", "認証"],
  "linkedOkrIds": ["okr_123"]
}
```

**レスポンス:** `201 Created`
```json
{
  "id": "post_001",
  "content": "今日はログイン機能の実装を完了しました。\nJWTトークンの検証部分で少し詰まりましたが、無事解決。",
  "postType": "DAILY_REPORT",
  "visibilityScope": "ORGANIZATION",
  "isAnonymous": true,
  "author": {
    "id": "anon_001",
    "displayName": "匿名ユーザー#A7F3",
    "isAnonymous": true
  },
  "organization": {
    "id": "org_456",
    "name": "開発部"
  },
  "tags": ["開発", "認証"],
  "linkedOkrs": [
    {
      "id": "okr_123",
      "title": "認証システムの構築"
    }
  ],
  "reactionCount": 0,
  "commentCount": 0,
  "createdAt": "2024-01-20T15:00:00Z",
  "updatedAt": "2024-01-20T15:00:00Z"
}
```

---

### 11. タイムライン取得

#### `GET /posts`

投稿のタイムラインを取得します。

**権限:** `MEMBER`以上

**クエリパラメータ:**
- `organizationId` (optional): 組織フィルタ
- `postType` (optional): 投稿タイプフィルタ（`DAILY_REPORT`, `WEEKLY_SUMMARY`, `CHALLENGE_UPDATE`, `ACHIEVEMENT`）
- `visibilityScope` (optional): 公開範囲フィルタ
- `startDate`, `endDate` (optional): 日付範囲フィルタ（ISO 8601形式）
- `tags` (optional): タグフィルタ（カンマ区切り）
- `page`, `size`, `sort`: ページネーション

**レスポンス:** `200 OK`
```json
{
  "content": [
    {
      "id": "post_001",
      "content": "今日はログイン機能の実装を完了しました。",
      "postType": "DAILY_REPORT",
      "visibilityScope": "ORGANIZATION",
      "isAnonymous": true,
      "author": {
        "id": "anon_001",
        "displayName": "匿名ユーザー#A7F3",
        "isAnonymous": true
      },
      "organization": {
        "id": "org_456",
        "name": "開発部"
      },
      "tags": ["開発", "認証"],
      "reactionCount": 5,
      "commentCount": 2,
      "createdAt": "2024-01-20T15:00:00Z"
    }
  ],
  "page": {
    "number": 0,
    "size": 20,
    "totalElements": 120,
    "totalPages": 6
  }
}
```

---

### 12. 投稿詳細取得

#### `GET /posts/{postId}`

**権限:** `MEMBER`以上（可視性チェックあり）

**レスポンス:** `200 OK`
```json
{
  "id": "post_001",
  "content": "今日はログイン機能の実装を完了しました。\nJWTトークンの検証部分で少し詰まりましたが、無事解決。",
  "postType": "DAILY_REPORT",
  "visibilityScope": "ORGANIZATION",
  "isAnonymous": true,
  "author": {
    "id": "anon_001",
    "displayName": "匿名ユーザー#A7F3",
    "isAnonymous": true
  },
  "organization": {
    "id": "org_456",
    "name": "開発部"
  },
  "tags": ["開発", "認証"],
  "linkedOkrs": [
    {
      "id": "okr_123",
      "title": "認証システムの構築",
      "progress": 75.0
    }
  ],
  "reactions": [
    {
      "type": "THUMBS_UP",
      "count": 3
    },
    {
      "type": "CELEBRATE",
      "count": 2
    }
  ],
  "comments": [
    {
      "id": "comment_001",
      "content": "お疲れ様です！",
      "author": {
        "displayName": "匿名ユーザー#C5X1",
        "isAnonymous": true
      },
      "createdAt": "2024-01-20T15:30:00Z"
    }
  ],
  "createdAt": "2024-01-20T15:00:00Z",
  "updatedAt": "2024-01-20T15:00:00Z"
}
```

---

### 13. 投稿更新

#### `PATCH /posts/{postId}`

**権限:** 投稿者本人

**リクエストボディ:**
```json
{
  "content": "今日はログイン機能の実装を完了しました。（更新版）",
  "tags": ["開発", "認証", "完了"]
}
```

**レスポンス:** `200 OK`

---

### 14. 投稿削除

#### `DELETE /posts/{postId}`

**権限:** 投稿者本人 または `ORG_ADMIN`

**レスポンス:** `204 No Content`

---

### 15. リアクション追加

#### `POST /posts/{postId}/reactions`

投稿にリアクションを追加します。

**権限:** `MEMBER`以上

**リクエストボディ:**
```json
{
  "type": "THUMBS_UP"
}
```

**利用可能なリアクションタイプ:**
- `THUMBS_UP`: いいね
- `CELEBRATE`: お祝い
- `SUPPORT`: 応援
- `INSIGHTFUL`: 参考になる
- `THANKS`: ありがとう

**レスポンス:** `201 Created`
```json
{
  "id": "reaction_001",
  "postId": "post_001",
  "type": "THUMBS_UP",
  "user": {
    "id": "anon_002",
    "displayName": "匿名ユーザー#B7K2",
    "isAnonymous": true
  },
  "createdAt": "2024-01-20T16:00:00Z"
}
```

---

### 16. リアクション削除

#### `DELETE /posts/{postId}/reactions/{reactionId}`

**権限:** リアクション作成者本人

**レスポンス:** `204 No Content`

---

## OKR管理API

### 17. Objective作成

#### `POST /okrs/objectives`

新しいObjectiveを作成します。

**権限:** `MEMBER`以上

**リクエストボディ:**
```json
{
  "title": "顧客満足度を向上させる",
  "description": "NPS +10ポイントを目指す",
  "organizationId": "org_456",
  "ownerId": "user_123",
  "quarter": "2024_Q1",
  "status": "IN_PROGRESS",
  "parentObjectiveId": null
}
```

**レスポンス:** `201 Created`
```json
{
  "id": "obj_001",
  "title": "顧客満足度を向上させる",
  "description": "NPS +10ポイントを目指す",
  "organizationId": "org_456",
  "owner": {
    "id": "user_123",
    "displayName": "田中花子"
  },
  "quarter": "2024_Q1",
  "status": "IN_PROGRESS",
  "progress": 0.0,
  "keyResults": [],
  "createdAt": "2024-01-10T10:00:00Z"
}
```

---

### 18. Key Result作成

#### `POST /okrs/objectives/{objectiveId}/key-results`

**権限:** Objective所有者 または `ORG_ADMIN`

**リクエストボディ:**
```json
{
  "title": "問い合わせ対応時間を平均2時間以内にする",
  "description": "現状平均4時間から改善",
  "startValue": 4.0,
  "targetValue": 2.0,
  "currentValue": 3.5,
  "unit": "時間",
  "ownerId": "user_456"
}
```

**レスポンス:** `201 Created`
```json
{
  "id": "kr_001",
  "objectiveId": "obj_001",
  "title": "問い合わせ対応時間を平均2時間以内にする",
  "description": "現状平均4時間から改善",
  "startValue": 4.0,
  "targetValue": 2.0,
  "currentValue": 3.5,
  "unit": "時間",
  "progress": 25.0,
  "owner": {
    "id": "user_456",
    "displayName": "山田太郎"
  },
  "createdAt": "2024-01-10T11:00:00Z"
}
```

---

### 19. Key Result進捗更新

#### `PATCH /okrs/key-results/{krId}/progress`

**権限:** KeyResult所有者

**リクエストボディ:**
```json
{
  "currentValue": 2.8,
  "note": "先週から0.7時間改善しました"
}
```

**レスポンス:** `200 OK`
```json
{
  "id": "kr_001",
  "currentValue": 2.8,
  "progress": 60.0,
  "updatedAt": "2024-01-20T17:00:00Z"
}
```

---

### 20. Objective一覧取得

#### `GET /okrs/objectives`

**権限:** `MEMBER`以上

**クエリパラメータ:**
- `organizationId` (optional): 組織フィルタ
- `quarter` (optional): 四半期フィルタ（例: `2024_Q1`）
- `status` (optional): ステータスフィルタ（`NOT_STARTED`, `IN_PROGRESS`, `COMPLETED`, `CANCELLED`）
- `ownerId` (optional): 所有者フィルタ
- `page`, `size`, `sort`: ページネーション

**レスポンス:** `200 OK`
```json
{
  "content": [
    {
      "id": "obj_001",
      "title": "顧客満足度を向上させる",
      "organizationId": "org_456",
      "owner": {
        "id": "user_123",
        "displayName": "田中花子"
      },
      "quarter": "2024_Q1",
      "status": "IN_PROGRESS",
      "progress": 42.5,
      "keyResultCount": 3,
      "createdAt": "2024-01-10T10:00:00Z"
    }
  ],
  "page": {
    "number": 0,
    "size": 20,
    "totalElements": 25,
    "totalPages": 2
  }
}
```

---

### 21. 投稿とOKRのリンク作成

#### `POST /okrs/objectives/{objectiveId}/link-post`

進捗投稿をObjectiveにリンクします。

**権限:** `MEMBER`以上

**リクエストボディ:**
```json
{
  "postId": "post_001"
}
```

**レスポンス:** `201 Created`
```json
{
  "id": "link_001",
  "objectiveId": "obj_001",
  "postId": "post_001",
  "linkedAt": "2024-01-20T18:00:00Z"
}
```

---

## 評価・分析API

### 22. 評価スナップショット作成

#### `POST /evaluations/snapshots`

ユーザーの評価を記録します（人事評価・ピアレビュー等）。

**権限:** `ORG_ADMIN` または 評価権限を持つユーザー

**リクエストボディ:**
```json
{
  "userId": "user_123",
  "organizationId": "org_456",
  "period": "2024_Q1",
  "evaluationType": "QUARTERLY_REVIEW",
  "score": 4.2,
  "strengths": "チーム貢献度が高い",
  "improvements": "ドキュメント作成を強化",
  "evaluatorId": "user_789"
}
```

**レスポンス:** `201 Created`
```json
{
  "id": "eval_001",
  "userId": "user_123",
  "organizationId": "org_456",
  "period": "2024_Q1",
  "evaluationType": "QUARTERLY_REVIEW",
  "score": 4.2,
  "strengths": "チーム貢献度が高い",
  "improvements": "ドキュメント作成を強化",
  "evaluator": {
    "id": "user_789",
    "displayName": "佐藤次郎"
  },
  "createdAt": "2024-03-31T23:59:59Z"
}
```

---

### 23. ユーザー評価履歴取得

#### `GET /evaluations/users/{userId}/history`

**権限:** 本人 または `ORG_ADMIN`

**クエリパラメータ:**
- `startPeriod`, `endPeriod` (optional): 期間範囲フィルタ

**レスポンス:** `200 OK`
```json
{
  "userId": "user_123",
  "evaluations": [
    {
      "id": "eval_001",
      "period": "2024_Q1",
      "evaluationType": "QUARTERLY_REVIEW",
      "score": 4.2,
      "createdAt": "2024-03-31T23:59:59Z"
    },
    {
      "id": "eval_002",
      "period": "2023_Q4",
      "evaluationType": "QUARTERLY_REVIEW",
      "score": 3.8,
      "createdAt": "2023-12-31T23:59:59Z"
    }
  ]
}
```

---

### 24. 組織統計取得

#### `GET /analytics/organizations/{orgId}/stats`

組織のアクティビティ統計を取得します。

**権限:** `ORG_ADMIN`

**クエリパラメータ:**
- `startDate`, `endDate`: 集計期間（ISO 8601形式）

**レスポンス:** `200 OK`
```json
{
  "organizationId": "org_456",
  "period": {
    "start": "2024-01-01T00:00:00Z",
    "end": "2024-01-31T23:59:59Z"
  },
  "metrics": {
    "totalPosts": 450,
    "dailyReportPosts": 320,
    "challengeUpdatePosts": 80,
    "achievementPosts": 50,
    "totalReactions": 1250,
    "activeMembers": 45,
    "averagePostsPerMember": 10.0,
    "okrCompletionRate": 68.5
  },
  "topContributors": [
    {
      "userId": "anon_001",
      "displayName": "匿名ユーザー#A7F3",
      "postCount": 25
    }
  ]
}
```

---

### 25. OKR進捗ダッシュボード

#### `GET /analytics/okrs/dashboard`

OKRの進捗状況をダッシュボード形式で取得します。

**権限:** `MEMBER`以上

**クエリパラメータ:**
- `organizationId` (optional): 組織フィルタ
- `quarter`: 四半期指定（必須）

**レスポンス:** `200 OK`
```json
{
  "quarter": "2024_Q1",
  "summary": {
    "totalObjectives": 15,
    "completedObjectives": 6,
    "inProgressObjectives": 8,
    "notStartedObjectives": 1,
    "averageProgress": 58.3
  },
  "objectives": [
    {
      "id": "obj_001",
      "title": "顧客満足度を向上させる",
      "progress": 42.5,
      "status": "IN_PROGRESS",
      "keyResults": [
        {
          "id": "kr_001",
          "title": "問い合わせ対応時間を平均2時間以内にする",
          "progress": 60.0
        }
      ]
    }
  ]
}
```

---

## 監査ログAPI

### 26. 監査ログ記録

#### `POST /audit-logs`

システム操作の監査ログを記録します（主にシステム内部から自動呼び出し）。

**権限:** `SYSTEM_ADMIN`

**リクエストボディ:**
```json
{
  "action": "USER_LOGIN",
  "targetType": "USER",
  "targetId": "user_123",
  "changes": {
    "lastLoginAt": "2024-01-20T09:00:00Z"
  },
  "ipAddress": "192.168.1.100",
  "userAgent": "Mozilla/5.0..."
}
```

**レスポンス:** `201 Created`
```json
{
  "id": "audit_001",
  "tenantId": "01H8X3Z...",
  "userId": "user_123",
  "action": "USER_LOGIN",
  "targetType": "USER",
  "targetId": "user_123",
  "changes": {
    "lastLoginAt": "2024-01-20T09:00:00Z"
  },
  "ipAddress": "192.168.1.100",
  "userAgent": "Mozilla/5.0...",
  "timestamp": "2024-01-20T09:00:00Z"
}
```

---

### 27. 監査ログ検索

#### `GET /audit-logs`

監査ログを検索・取得します。

**権限:** `TENANT_ADMIN`, `ORG_ADMIN`

**クエリパラメータ:**
- `action` (optional): アクションフィルタ
- `targetType` (optional): 対象タイプフィルタ
- `targetId` (optional): 対象IDフィルタ
- `userId` (optional): 実行ユーザーフィルタ
- `startDate`, `endDate` (optional): 日時範囲フィルタ
- `page`, `size`, `sort`: ページネーション

**レスポンス:** `200 OK`
```json
{
  "content": [
    {
      "id": "audit_001",
      "action": "USER_LOGIN",
      "targetType": "USER",
      "targetId": "user_123",
      "user": {
        "id": "user_123",
        "displayName": "田中花子"
      },
      "ipAddress": "192.168.1.100",
      "timestamp": "2024-01-20T09:00:00Z"
    }
  ],
  "page": {
    "number": 0,
    "size": 50,
    "totalElements": 1234,
    "totalPages": 25
  }
}
```

**主要なアクションタイプ:**
- `USER_LOGIN`, `USER_LOGOUT`
- `POST_CREATED`, `POST_UPDATED`, `POST_DELETED`
- `OKR_CREATED`, `OKR_UPDATED`, `OKR_PROGRESS_UPDATED`
- `MEMBER_INVITED`, `MEMBER_REMOVED`
- `ORGANIZATION_CREATED`, `ORGANIZATION_UPDATED`
- `POLICY_UPDATED`

---

## エラーレスポンス

### エラーレスポンス形式

すべてのエラーは以下の統一形式で返されます。

```json
{
  "error": {
    "code": "ERROR_CODE",
    "message": "エラーメッセージ",
    "details": {
      "field": "fieldName",
      "reason": "詳細な理由"
    },
    "timestamp": "2024-01-20T10:30:00Z",
    "path": "/api/v1/posts"
  }
}
```

### HTTPステータスコード

| コード | 説明 | 例 |
|--------|------|-----|
| `200 OK` | 成功 | GET, PATCH成功 |
| `201 Created` | 作成成功 | POST成功 |
| `204 No Content` | 削除成功 | DELETE成功 |
| `400 Bad Request` | リクエスト不正 | バリデーションエラー |
| `401 Unauthorized` | 認証エラー | JWTトークン無効 |
| `403 Forbidden` | 権限不足 | アクセス権限なし |
| `404 Not Found` | リソース未存在 | 指定IDのデータなし |
| `409 Conflict` | 競合エラー | 一意制約違反 |
| `422 Unprocessable Entity` | 処理不可 | ビジネスロジックエラー |
| `429 Too Many Requests` | レート制限 | API呼び出し上限超過 |
| `500 Internal Server Error` | サーバーエラー | 予期せぬエラー |

### エラーコード一覧

#### 認証・認可エラー
- `AUTH_TOKEN_INVALID`: JWTトークンが無効
- `AUTH_TOKEN_EXPIRED`: JWTトークンが期限切れ
- `AUTH_INSUFFICIENT_PERMISSION`: 権限不足
- `TENANT_ACCESS_DENIED`: テナントへのアクセス権限なし

#### バリデーションエラー
- `VALIDATION_FAILED`: 入力値バリデーションエラー
- `REQUIRED_FIELD_MISSING`: 必須フィールド未入力
- `INVALID_FORMAT`: フォーマット不正

#### ビジネスロジックエラー
- `TENANT_USER_LIMIT_EXCEEDED`: テナントユーザー上限超過
- `ORGANIZATION_NOT_FOUND`: 組織が見つからない
- `POST_VISIBILITY_VIOLATION`: 投稿の可視性違反
- `OKR_ALREADY_COMPLETED`: OKRが既に完了済み
- `ANONYMOUS_PROFILE_NOT_FOUND`: 匿名プロファイルが見つからない

#### リソースエラー
- `RESOURCE_NOT_FOUND`: リソースが見つからない
- `RESOURCE_ALREADY_EXISTS`: リソースが既に存在
- `RESOURCE_CONFLICT`: リソース競合

### エラーレスポンス例

**401 Unauthorized**
```json
{
  "error": {
    "code": "AUTH_TOKEN_EXPIRED",
    "message": "認証トークンの有効期限が切れています",
    "timestamp": "2024-01-20T10:30:00Z",
    "path": "/api/v1/posts"
  }
}
```

**400 Bad Request**
```json
{
  "error": {
    "code": "VALIDATION_FAILED",
    "message": "入力値が不正です",
    "details": {
      "content": "投稿内容は1文字以上10000文字以内で入力してください",
      "postType": "投稿タイプは必須です"
    },
    "timestamp": "2024-01-20T10:30:00Z",
    "path": "/api/v1/posts"
  }
}
```

**403 Forbidden**
```json
{
  "error": {
    "code": "AUTH_INSUFFICIENT_PERMISSION",
    "message": "この操作を実行する権限がありません",
    "details": {
      "required": "ORG_ADMIN",
      "current": "MEMBER"
    },
    "timestamp": "2024-01-20T10:30:00Z",
    "path": "/api/v1/organizations/org_456/members/invite"
  }
}
```

**422 Unprocessable Entity**
```json
{
  "error": {
    "code": "TENANT_USER_LIMIT_EXCEEDED",
    "message": "テナントのユーザー数上限を超過しています",
    "details": {
      "maxUsers": 500,
      "currentUsers": 500
    },
    "timestamp": "2024-01-20T10:30:00Z",
    "path": "/api/v1/organizations/org_456/members/invite"
  }
}
```

---

## 付録

### ポリシー設定API

#### `GET /policies`

組織のポリシー設定を取得します。

**権限:** `MEMBER`以上

**クエリパラメータ:**
- `organizationId`: 組織ID

**レスポンス:** `200 OK`
```json
{
  "policies": [
    {
      "id": "policy_001",
      "organizationId": "org_456",
      "policyType": "POST_APPROVAL_REQUIRED",
      "enabled": false,
      "config": {}
    },
    {
      "id": "policy_002",
      "organizationId": "org_456",
      "policyType": "ANONYMITY_ENFORCED",
      "enabled": true,
      "config": {
        "allowNameReveal": false
      }
    }
  ]
}
```

#### `PATCH /policies/{policyId}`

ポリシー設定を更新します。

**権限:** `ORG_ADMIN`

**リクエストボディ:**
```json
{
  "enabled": true,
  "config": {
    "allowNameReveal": true
  }
}
```

**レスポンス:** `200 OK`

---

### レート制限

APIリクエストには以下のレート制限が適用されます：

| エンドポイントタイプ | 制限 | 期間 |
|---------------------|------|------|
| 認証API | 10リクエスト | 1分 |
| 投稿作成 | 30リクエスト | 1分 |
| 読み取りAPI | 100リクエスト | 1分 |
| その他 | 60リクエスト | 1分 |

レート制限超過時は `429 Too Many Requests` が返されます。

**レスポンスヘッダー:**
```http
X-RateLimit-Limit: 100
X-RateLimit-Remaining: 45
X-RateLimit-Reset: 1705748400
```

---

### Webhook設定（将来拡張予定）

特定のイベント発生時に外部URLへ通知を送信する機能（将来実装予定）。

**対応予定イベント:**
- `post.created`
- `okr.progress.updated`
- `evaluation.created`

---

## バージョン履歴

| バージョン | リリース日 | 変更内容 |
|-----------|-----------|----------|
| v1.0 | 2024-01-20 | 初版リリース |

---

## サポート・お問い合わせ

API仕様に関するお問い合わせは以下まで：
- 開発者ドキュメント: https://docs.letteral.com/api
- サポート: support@letteral.com

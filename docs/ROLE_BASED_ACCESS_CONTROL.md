# ロールベースアクセス制御 (RBAC) システム

このドキュメントでは、Letteralプロジェクトで実装されているロールベースアクセス制御（RBAC）システムの使い方を説明します。

## 概要

本システムは、5つのユーザーロールと80以上の細かい権限を定義し、企業向けSaaSアプリケーションとして適切なアクセス制御を実現しています。

## ユーザーロール

### 1. SUPER_ADMIN（スーパー管理者）
- **権限範囲**: システム全体
- **主な権限**: すべての権限
- **用途**: システム管理者、プラットフォーム運営者

### 2. TENANT_ADMIN（テナント管理者）
- **権限範囲**: 自社（テナント）内のすべてのリソース
- **主な権限**:
  - テナント設定の管理
  - ユーザーの招待・削除・ロール変更
  - 組織階層の作成・管理
  - 全投稿・OKRの閲覧とモデレーション
  - レポート・監査ログの閲覧
- **用途**: 企業の管理者、人事部門

### 3. ORG_ADMIN（組織管理者）
- **権限範囲**: 自組織とその配下の組織
- **主な権限**:
  - 組織メンバーの管理
  - 組織内の投稿・OKRの管理
  - グループの作成・管理
  - 組織内レポートの閲覧
- **用途**: 部門長、チームリーダー

### 4. MODERATOR（モデレーター）
- **権限範囲**: テナント内のコンテンツ
- **主な権限**:
  - 投稿のモデレーション
  - チャットのモデレーション
  - グループの管理
- **用途**: コンテンツ管理者、コミュニティマネージャー

### 5. USER（一般ユーザー）
- **権限範囲**: 自分のコンテンツ
- **主な権限**:
  - 投稿の作成・編集・削除（自分の投稿のみ）
  - OKRの作成・編集・削除（自分のOKRのみ）
  - グループへの参加
  - チャットの送受信
- **用途**: 一般社員

## バックエンド実装

### アノテーションベースの権限チェック

#### @RequirePermission
特定の権限が必要なエンドポイントに使用します。

```java
@GetMapping("/posts")
@RequirePermission(Permission.POST_VIEW)
public ResponseEntity<List<Post>> getPosts() {
    // 実装
}
```

#### @RequireRole
特定のロールが必要なエンドポイントに使用します。

```java
@PostMapping("/tenants")
@RequireRole(UserRole.SUPER_ADMIN)
public ResponseEntity<Tenant> createTenant(@RequestBody Tenant tenant) {
    // 実装
}
```

### 権限定義の場所

- **Permission enum**: `backend/src/main/java/com/chatapp/security/Permission.java`
- **UserRole enum**: `backend/src/main/java/com/chatapp/security/UserRole.java`
- **PermissionAspect**: `backend/src/main/java/com/chatapp/security/PermissionAspect.java`
- **PermissionChecker**: `backend/src/main/java/com/chatapp/security/PermissionChecker.java`

## フロントエンド実装

### 型定義
`src/types/permissions.ts`に以下が定義されています：
- `Permission` enum
- `UserRole` enum
- `RolePermissions` マッピング
- ヘルパー関数（`hasPermission`, `hasAnyPermission`, `hasAllPermissions`）

### コンポーネントでの使用

#### PermissionGuardコンポーネント

権限に基づいてUIの表示/非表示を制御します。

```tsx
import { PermissionGuard } from '@/components/auth/PermissionGuard';
import { Permission, UserRole } from '@/types/permissions';

// 単一の権限チェック
<PermissionGuard
  permission={Permission.POST_CREATE}
  currentRole={user.role}
>
  <button>投稿を作成</button>
</PermissionGuard>

// 複数権限のいずれか
<PermissionGuard
  anyPermissions={[Permission.POST_UPDATE, Permission.POST_DELETE]}
  currentRole={user.role}
  fallback={<span>権限がありません</span>}
>
  <button>編集</button>
</PermissionGuard>

// ロールチェック
<PermissionGuard
  anyRoles={[UserRole.TENANT_ADMIN, UserRole.ORG_ADMIN]}
  currentRole={user.role}
>
  <AdminPanel />
</PermissionGuard>
```

#### usePermissionsフック

プログラマティックな権限チェックに使用します。

```tsx
import { usePermissions } from '@/hooks/usePermissions';
import { Permission, UserRole } from '@/types/permissions';

function MyComponent() {
  const { can, canAny, canAll, is, isAny } = usePermissions(user.role);

  // 単一権限チェック
  if (can(Permission.POST_CREATE)) {
    // 投稿作成ボタンを表示
  }

  // 複数権限チェック
  if (canAny([Permission.POST_UPDATE, Permission.POST_DELETE])) {
    // 編集ボタンを表示
  }

  // ロールチェック
  if (is(UserRole.TENANT_ADMIN)) {
    // 管理者メニューを表示
  }

  if (isAny([UserRole.TENANT_ADMIN, UserRole.ORG_ADMIN])) {
    // 管理者機能を表示
  }

  return <div>...</div>;
}
```

## 権限マトリックス

### テナント管理

| 操作 | SUPER_ADMIN | TENANT_ADMIN | ORG_ADMIN | MODERATOR | USER |
|-----|-------------|--------------|-----------|-----------|------|
| テナント作成 | ✅ | ❌ | ❌ | ❌ | ❌ |
| テナント閲覧 | ✅ | ✅ | ✅ | ✅ | ✅ |
| テナント更新 | ✅ | ✅ | ❌ | ❌ | ❌ |
| テナント削除 | ✅ | ❌ | ❌ | ❌ | ❌ |

### ユーザー管理

| 操作 | SUPER_ADMIN | TENANT_ADMIN | ORG_ADMIN | MODERATOR | USER |
|-----|-------------|--------------|-----------|-----------|------|
| 全ユーザー閲覧 | ✅ | ✅ | ❌ | ❌ | ❌ |
| ユーザー検索 | ✅ | ✅ | ✅ | ✅ | ✅ |
| ユーザー作成 | ✅ | ✅ | ❌ | ❌ | ❌ |
| ユーザー招待 | ✅ | ✅ | ✅ | ❌ | ❌ |
| ロール変更 | ✅ | ✅ | ❌ | ❌ | ❌ |
| ユーザー削除 | ✅ | ✅ | ❌ | ❌ | ❌ |

### 組織管理

| 操作 | SUPER_ADMIN | TENANT_ADMIN | ORG_ADMIN | MODERATOR | USER |
|-----|-------------|--------------|-----------|-----------|------|
| 組織作成 | ✅ | ✅ | ❌ | ❌ | ❌ |
| 組織閲覧 | ✅ | ✅ | ✅ | ✅ | ✅ |
| 組織更新 | ✅ | ✅ | ✅* | ❌ | ❌ |
| 組織削除 | ✅ | ✅ | ❌ | ❌ | ❌ |
| メンバー管理 | ✅ | ✅ | ✅* | ❌ | ❌ |
| 階層管理 | ✅ | ✅ | ❌ | ❌ | ❌ |

*自組織のみ

### 投稿管理

| 操作 | SUPER_ADMIN | TENANT_ADMIN | ORG_ADMIN | MODERATOR | USER |
|-----|-------------|--------------|-----------|-----------|------|
| 全投稿閲覧 | ✅ | ✅ | ✅ | ✅ | ❌ |
| 投稿閲覧 | ✅ | ✅ | ✅ | ✅ | ✅ |
| 投稿作成 | ✅ | ✅ | ✅ | ✅ | ✅ |
| 投稿更新 | ✅ | ✅ | ✅ | ✅ | ✅* |
| 投稿削除 | ✅ | ✅ | ✅ | ❌ | ✅* |
| モデレーション | ✅ | ✅ | ✅ | ✅ | ❌ |

*自分の投稿のみ

### OKR管理

| 操作 | SUPER_ADMIN | TENANT_ADMIN | ORG_ADMIN | MODERATOR | USER |
|-----|-------------|--------------|-----------|-----------|------|
| 全OKR閲覧 | ✅ | ✅ | ✅ | ✅ | ❌ |
| OKR閲覧 | ✅ | ✅ | ✅ | ✅ | ✅ |
| OKR作成 | ✅ | ✅ | ✅ | ✅ | ✅ |
| OKR更新 | ✅ | ✅ | ✅ | ✅ | ✅* |
| OKR削除 | ✅ | ✅ | ✅ | ❌ | ✅* |
| 組織OKR管理 | ✅ | ✅ | ✅ | ❌ | ❌ |

*自分のOKRのみ

### グループ・チャット

| 操作 | SUPER_ADMIN | TENANT_ADMIN | ORG_ADMIN | MODERATOR | USER |
|-----|-------------|--------------|-----------|-----------|------|
| グループ作成 | ✅ | ✅ | ✅ | ✅ | ✅ |
| グループ更新 | ✅ | ✅ | ✅ | ✅ | ❌ |
| グループ削除 | ✅ | ✅ | ✅ | ❌ | ❌ |
| メンバー管理 | ✅ | ✅ | ✅ | ✅ | ❌ |
| チャット送信 | ✅ | ✅ | ✅ | ✅ | ✅ |
| チャット削除 | ✅ | ✅ | ❌ | ❌ | ❌ |
| モデレーション | ✅ | ✅ | ✅ | ✅ | ❌ |

## エラーハンドリング

権限不足の場合、以下のHTTPステータスコードが返されます：

- **403 Forbidden**: 権限が不足している場合
- **401 Unauthorized**: 認証されていない場合

フロントエンドでは適切にエラーメッセージを表示してください。

## テスト

### バックエンドテスト例

```java
@Test
@WithMockUser(username = "user", roles = {"USER"})
public void testUserCannotDeleteOthersPosts() throws Exception {
    mockMvc.perform(delete("/api/posts/{id}", otherUsersPostId))
           .andExpect(status().isForbidden());
}

@Test
@WithMockUser(username = "admin", roles = {"TENANT_ADMIN"})
public void testTenantAdminCanViewAllPosts() throws Exception {
    mockMvc.perform(get("/api/posts/all"))
           .andExpect(status().isOk());
}
```

### フロントエンドテスト例

```tsx
import { render, screen } from '@testing-library/react';
import { PermissionGuard } from '@/components/auth/PermissionGuard';
import { Permission, UserRole } from '@/types/permissions';

test('PermissionGuard shows content when user has permission', () => {
  render(
    <PermissionGuard
      permission={Permission.POST_CREATE}
      currentRole={UserRole.USER}
    >
      <button>投稿を作成</button>
    </PermissionGuard>
  );

  expect(screen.getByText('投稿を作成')).toBeInTheDocument();
});

test('PermissionGuard hides content when user lacks permission', () => {
  render(
    <PermissionGuard
      permission={Permission.USER_DELETE}
      currentRole={UserRole.USER}
    >
      <button>ユーザー削除</button>
    </PermissionGuard>
  );

  expect(screen.queryByText('ユーザー削除')).not.toBeInTheDocument();
});
```

## ベストプラクティス

### 1. 最小権限の原則
ユーザーには必要最小限の権限のみを付与してください。

### 2. バックエンドでの権限チェック
フロントエンドの権限チェックは見た目の制御のみに使用し、必ずバックエンドでも権限チェックを行ってください。

### 3. 適切なロールの選択
新しいユーザーを追加する際は、その役割に最も適したロールを選択してください。

### 4. 定期的な権限レビュー
定期的にユーザーのロールと権限を見直し、不要な権限は削除してください。

### 5. 監査ログの活用
重要な操作は監査ログに記録し、定期的にレビューしてください。

## トラブルシューティング

### 403エラーが発生する場合

1. ユーザーが正しいロールを持っているか確認
2. 必要な権限がそのロールに付与されているか確認
3. トークンが有効か確認
4. バックエンドログで詳細なエラーメッセージを確認

### 権限が正しく動作しない場合

1. `PermissionAspect`が正しく動作しているか確認
2. `@RequirePermission`/`@RequireRole`アノテーションが正しく付与されているか確認
3. Springのコンポーネントスキャンに問題がないか確認

## 今後の拡張

- 動的権限の追加
- カスタムパーミッションセットの作成
- 時間制限付き権限
- 承認フロー付き権限変更

## 参考資料

- [Spring Security Documentation](https://docs.spring.io/spring-security/reference/)
- [RBAC Best Practices](https://csrc.nist.gov/projects/role-based-access-control)

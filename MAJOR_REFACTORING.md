# 大規模リファクタリング: 企業向け階層的組織管理システムへの移行

**日付**: 2025-11-11
**目的**: 個人向けチャレンジアプリから企業向け匿名進捗共有プラットフォームへの変更

---

## 📋 概要

デイリーチャレンジ、DM、フレンド機能、グループチャット機能を削除し、企業で使いやすい階層的な組織管理システムに変更しました。

---

## 🎯 新機能: 階層的権限システム

### 役割（OrganizationRole）

新しい階層構造の役割を追加：

1. **OWNER** - システムオーナー（最高権限）
2. **ADMIN_ROOT** - 役員・社長レベル
   - 会社全体の管理（部・支店レベル）
   - メンバーの追加・削除
   - 部署などの階層グループ追加
   - ADMIN_ROOTまでの権限付与が可能

3. **ADMIN_CORE** - 部長レベル
   - 部門管理（課・グループレベル）
   - メンバーの追加・削除
   - 課階層グループ追加
   - ADMIN_COREまでの権限付与が可能

4. **ADMIN_LEAD** - 課長レベル
   - 課管理（チームレベル）
   - メンバーの追加・削除
   - チーム階層グループ追加
   - ADMIN_LEADまでの権限付与が可能

5. **ADMIN_SUPER** - PMレベル
   - チーム管理（プロジェクトレベル）
   - メンバーの追加・削除
   - プロジェクトチーム階層グループ追加
   - ADMIN_SUPERまでの権限付与が可能

6. **MEMBER** - 通常メンバー
   - 閲覧・投稿のみ

### 権限チェックメソッド

- `hasAuthorityOver()` - 指定された役割以上の権限を持つかチェック
- `canAssignRole()` - 指定された役割を付与できるかチェック

---

## 🗑️ 削除された機能

### バックエンド

#### 1. デイリーチャレンジシステム（8エンティティ）
- `DailyChallenge` - チャレンジ定義
- `ChallengeCompletion` - ユーザー完了記録
- `UserProgress` - ゲーミフィケーション進捗
- `ChallengeShare` - タイムライン共有
- `ChallengeShareReaction` - リアクション
- `ChallengeShareReadStatus` - 既読状態
- `Badge` - バッジ定義
- `UserBadge` - ユーザーバッジ

**削除されたファイル:**
- `ChallengeController.java`
- `ChallengeShareController.java`
- `ChallengeService.java`
- `ChallengeShareService.java`
- `BadgeService.java`
- `DailyChallengeRepository.java`
- `ChallengeCompletionRepository.java`
- `ChallengeShareRepository.java`
- `ChallengeShareReactionRepository.java`
- `ChallengeShareReadStatusRepository.java`
- `UserProgressRepository.java`
- `BadgeRepository.java`
- `UserBadgeRepository.java`
- `ChallengeShareDtos.java`
- テストファイル2件

#### 2. フレンド・DM機能（1エンティティ）
- `Friend` - フレンド関係

**削除されたファイル:**
- `FriendController.java`
- `FriendService.java`
- `FriendRepository.java`

### フロントエンド

**削除されたコンポーネント:**
- `DailyChallenges.tsx`
- `Badges.tsx`
- `BadgeNotificationModal.tsx`
- `ChallengeShareTimeline.tsx`
- `FriendList.tsx`

**api.tsから削除:**
- `Friend`, `FriendWithId`, `FriendStats` インターフェース
- `ChallengeCompletionSummary`, `DailyChallengeSummary` インターフェース
- `ChallengeShare`, `PagedChallengeShares` インターフェース
- `Badge`, `UserBadge` インターフェース
- `friendService` (12メソッド)
- `badgeService` (3メソッド)
- `challengeServiceApi` (1メソッド)
- `challengeShareService` (6メソッド)
- `chatService.getFriendMessages()`

---

## ✨ 新規追加

### バックエンド

#### 1. OrganizationRole Enum（更新）
**ファイル:** `backend/src/main/java/com/chatapp/model/enums/OrganizationRole.java`

新しい階層的役割と権限チェックメソッドを追加。

#### 2. OrganizationPermissionService
**ファイル:** `backend/src/main/java/com/chatapp/service/OrganizationPermissionService.java`

階層的権限管理サービス：
- `hasPermission()` - 権限チェック
- `canAssignRole()` - 役割付与可能かチェック
- `canAddMember()` - メンバー追加可能かチェック
- `canRemoveMember()` - メンバー削除可能かチェック
- `canCreateSubOrganization()` - 子組織作成可能かチェック
- `canManageMember()` - メンバー管理可能かチェック
- `addMemberToOrganization()` - メンバー追加
- `removeMemberFromOrganization()` - メンバー削除
- `updateMemberRole()` - 役割変更

#### 3. OrganizationManagementController
**ファイル:** `backend/src/main/java/com/chatapp/controller/OrganizationManagementController.java`

新しいAPI エンドポイント：
- `POST /api/organization-management/{organizationId}/members` - メンバー追加
- `DELETE /api/organization-management/{organizationId}/members/{userId}` - メンバー削除
- `PUT /api/organization-management/{organizationId}/members/{userId}/role` - 役割変更
- `POST /api/organization-management/{parentOrganizationId}/sub-organizations` - 子組織作成
- `GET /api/organization-management/{organizationId}/permissions` - 権限確認

#### 4. OrganizationMemberRepository（更新）
**ファイル:** `backend/src/main/java/com/chatapp/repository/OrganizationMemberRepository.java`

新メソッド追加：
- `findByUserIdAndOrganizationId()` - ユーザーIDと組織IDで検索

---

## 🗄️ データベースマイグレーション

新しいマイグレーションファイル（V13-V18）:

1. **V13__Drop_Challenge_Share_Tables.sql**
   - `challenge_share_read_status` テーブル削除
   - `challenge_share_reactions` テーブル削除
   - `challenge_shares` テーブル削除

2. **V14__Drop_Challenge_Completion_Tables.sql**
   - `challenge_completions` テーブル削除

3. **V15__Drop_User_Progress_Table.sql**
   - `user_progress` テーブル削除

4. **V16__Drop_Daily_Challenges_Table.sql**
   - `daily_challenges` テーブル削除

5. **V17__Drop_Badges_Tables.sql**
   - `user_badges` テーブル削除
   - `badges` テーブル削除

6. **V18__Drop_Friends_Table.sql**
   - `friends` テーブル削除

---

## 📊 影響範囲まとめ

| カテゴリ | 削除数 | 追加数 |
|---------|--------|--------|
| Javaクラス | 25+ | 2 |
| データベーステーブル | 9 | 0 |
| APIエンドポイント (削除) | 35+ | 5 |
| フロントエンドコンポーネント | 5 | 0 |
| API関数 (frontend) | 24+ | 0 |
| 推定削除行数 | 5,000+ | - |

---

## 🔄 移行ステップ

### データベース
```bash
# マイグレーションを実行
./mvnw spring-boot:run
# または
./mvnw flyway:migrate
```

### バックエンド
```bash
# コンパイル確認
./mvnw clean compile
```

### フロントエンド
```bash
# 依存関係の確認
npm install
# ビルド確認
npm run build
```

---

## ⚠️ 重要な注意事項

1. **データ損失**: 削除されたテーブルのデータは復元できません。バックアップを推奨します。
2. **API変更**: フレンド・チャレンジ関連のAPIは完全に削除されました。
3. **互換性**: 既存のフロントエンドコンポーネントでこれらの機能を使用している場合、エラーが発生します。
4. **グループチャット**: 組織ベースのチャンネルシステムに変更されます。

---

## 🎯 今後の実装予定

1. **フロントエンドUI** - 組織階層管理画面の実装
2. **権限管理画面** - 役割の可視化と編集UI
3. **組織ツリー表示** - 階層構造の視覚化
4. **メンバー管理UI** - 追加・削除・役割変更のUI
5. **監査ログ** - 権限変更の履歴追跡

---

## 📝 変更されたファイル一覧

### Backend (Java)
- ✅ `OrganizationRole.java` (更新)
- ✅ `OrganizationMemberRepository.java` (更新)
- ➕ `OrganizationPermissionService.java` (新規)
- ➕ `OrganizationManagementController.java` (新規)
- ➕ `V13__Drop_Challenge_Share_Tables.sql` (新規)
- ➕ `V14__Drop_Challenge_Completion_Tables.sql` (新規)
- ➕ `V15__Drop_User_Progress_Table.sql` (新規)
- ➕ `V16__Drop_Daily_Challenges_Table.sql` (新規)
- ➕ `V17__Drop_Badges_Tables.sql` (新規)
- ➕ `V18__Drop_Friends_Table.sql` (新規)

### Frontend (TypeScript/React)
- ✅ `src/services/api.ts` (大幅更新 - 不要なサービス削除)
- ❌ `src/components/DailyChallenges.tsx` (削除)
- ❌ `src/components/Badges.tsx` (削除)
- ❌ `src/components/BadgeNotificationModal.tsx` (削除)
- ❌ `src/components/ChallengeShareTimeline.tsx` (削除)
- ❌ `src/components/FriendList.tsx` (削除)

---

## 🚀 次のステップ

1. バックエンドをビルド・起動してマイグレーションを実行
2. フロントエンドで削除されたコンポーネントを参照している箇所を修正
3. 新しい組織管理UIコンポーネントの実装
4. テストの実行と動作確認

---

**変更担当**: Claude
**変更日**: 2025-11-11
**承認**: 未承認

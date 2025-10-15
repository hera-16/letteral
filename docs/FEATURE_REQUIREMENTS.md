# チャットアプリケーション機能要件

## 概要
このドキュメントでは、ログイン後のユーザー体験とメイン機能の詳細を定義します。

## ログイン後のフロー

### 1. 選択画面（メインメニュー）
ログイン成功後、ユーザーは以下の3つのオプションを選択できる画面に遷移します:

- **フレンド機能**
- **招待制グループ**
- **公開グループ**

---

## フレンド機能

### 概要
ユーザー同士が1対1で繋がり、プライベートチャットを行うための機能。

### 主要機能

#### 1. フレンド追加
- ユーザー名 または ユーザーIDでフレンドを検索
- フレンドリクエストを送信
- 相手の承認待ち状態になる

#### 2. フレンド一覧閲覧
- 現在のフレンド一覧を表示
- 各フレンドのオンライン/オフライン状態を表示
- フレンドのプロフィール情報を閲覧

#### 3. フレンドリクエスト承認
- 受信したフレンドリクエスト一覧を表示
- リクエストを承認または拒否
- 承認後、フレンド一覧に自動追加

#### 4. フレンドチャット
- フレンド一覧から選択して1対1チャット開始
- リアルタイムメッセージング
- 過去のメッセージ履歴表示

### 必要なエンドポイント
- `GET /api/friends` - フレンド一覧取得
- `GET /api/friends/requests` - フレンドリクエスト一覧取得
- `POST /api/friends/request/{username}` - フレンドリクエスト送信
- `POST /api/friends/accept/{requestId}` - リクエスト承認
- `POST /api/friends/reject/{requestId}` - リクエスト拒否
- `DELETE /api/friends/{friendId}` - フレンド削除

---

## 招待制グループ

### 概要
招待コードまたは直接招待によってのみ参加できるプライベートグループ。

### 主要機能

#### 1. グループ作成
- グループ名、説明を設定
- 招待コードを自動生成
- メンバー上限設定（オプション）

#### 2. 参加グループ一覧
- 現在参加している招待制グループを一覧表示
- グループ名、メンバー数、最終メッセージ時刻を表示
- グループ選択でチャットルームに入室

#### 3. グループへの招待
- 招待コードを共有して他ユーザーを招待
- フレンド一覧から直接招待
- 管理者のみ招待可能（オプション設定）

#### 4. グループチャット
- グループメンバー全員とリアルタイムチャット
- メンバー一覧表示
- グループ退出機能

### 必要なエンドポイント
- `POST /api/groups/invite` - 招待制グループ作成
- `GET /api/groups/invite/my` - 参加中の招待制グループ一覧
- `POST /api/groups/invite/{groupId}/join` - 招待コードでグループ参加
- `GET /api/groups/invite/{groupId}` - グループ詳細取得
- `GET /api/groups/invite/{groupId}/members` - グループメンバー一覧
- `POST /api/groups/invite/{groupId}/invite/{friendId}` - フレンドを直接招待
- `DELETE /api/groups/invite/{groupId}/leave` - グループ退出

---

## 公開グループ（トピックベース）

### 概要
誰でも参加できるトピック別の公開チャットルーム。

### 主要機能

#### 1. トピック一覧表示
- 利用可能な公開トピック一覧を表示
- トピック名、説明、現在のアクティブユーザー数を表示
- カテゴリー別フィルタリング（オプション）

#### 2. トピック選択
- トピックを選択して即座にチャットルームに入室
- 認証済みユーザーなら誰でも参加可能

#### 3. 公開チャット
- トピック内でリアルタイムメッセージング
- 参加中のユーザー一覧表示
- 過去のメッセージ履歴表示（直近N件）

#### 4. トピック管理（管理者のみ）
- 新しいトピックの作成
- 既存トピックの編集・削除
- 不適切なメッセージの削除

### 必要なエンドポイント
- `GET /api/topics` - トピック一覧取得
- `POST /api/topics` - トピック作成（管理者）
- `GET /api/topics/{topicId}` - トピック詳細取得
- `GET /api/topics/{topicId}/messages` - トピック内メッセージ取得
- `DELETE /api/topics/{topicId}` - トピック削除（管理者）

---

## データモデル（追加必要分）

### Group（グループ）
```java
@Entity
public class Group {
    @Id
    @GeneratedValue
    private Long id;
    
    private String name;
    private String description;
    
    @Enumerated(EnumType.STRING)
    private GroupType groupType; // INVITE_ONLY, PUBLIC_TOPIC
    
    private String inviteCode; // 招待制グループのみ
    private Integer maxMembers; // オプション
    
    @ManyToOne
    private User creator;
    
    private LocalDateTime createdAt;
}
```

### GroupMember（グループメンバー）
```java
@Entity
public class GroupMember {
    @Id
    @GeneratedValue
    private Long id;
    
    @ManyToOne
    private Group group;
    
    @ManyToOne
    private User user;
    
    @Enumerated(EnumType.STRING)
    private MemberRole role; // ADMIN, MEMBER
    
    private LocalDateTime joinedAt;
}
```

### Topic（公開トピック）
```java
@Entity
public class Topic {
    @Id
    @GeneratedValue
    private Long id;
    
    private String name;
    private String description;
    private String category; // オプション
    
    @ManyToOne
    private User creator;
    
    private LocalDateTime createdAt;
}
```

---

## 実装の優先順位

### Phase 1: フレンド機能
1. Friend エンティティ（既存）
2. FriendController エンドポイント完成
3. FriendService ビジネスロジック実装
4. フロントエンド: フレンド一覧・追加・承認UI

### Phase 2: 招待制グループ
1. Group, GroupMember エンティティ作成
2. GroupRepository, GroupMemberRepository 作成
3. GroupService 実装
4. GroupController エンドポイント実装
5. フロントエンド: グループ一覧・作成・チャットUI

### Phase 3: 公開グループ（トピック）
1. Topic エンティティ作成
2. TopicRepository 作成
3. TopicService 実装
4. TopicController エンドポイント実装
5. フロントエンド: トピック一覧・チャットUI

---

## WebSocket 統合

### 既存のWebSocket構成を拡張
- `/topic/{roomId}` - 既存のチャットルーム
- `/topic/group/{groupId}` - グループチャット
- `/topic/topic/{topicId}` - トピックチャット
- `/topic/friend/{friendshipId}` - フレンド間チャット

---

## 次のステップ
1. ✅ WebSecurityConfig の型エラー修正
2. ✅ 要件ドキュメント作成
3. ⏳ 必要なエンティティ・リポジトリ実装確認
4. ⏳ Group, GroupMember, Topic エンティティ作成
5. ⏳ 対応するサービス・コントローラ実装

---

## 自己肯定感向上ダッシュボード拡張

デイリーチャレンジ機能を中心に、ユーザーの努力を「共有」と「可視化」で後押しする機能群を追加する。

### A. チャレンジ共有タイムライン

#### 目的
- 達成の瞬間を仲間と共有し、ポジティブなフィードバックを獲得できる場を提供する。
- 「応援」「共感」「すごい！」など手軽なリアクションで相互承認を促進する。

#### ユーザーストーリー
- ユーザーはチャレンジ完了時にメモ／感想を添えて共有タイムラインへ投稿できる。
- 他のメンバーはタイムラインをスクロールして閲覧し、ワンタップでリアクションできる。
- フィードを既読にすると新着バッジのモーダルと同様、未読数がクリアされる。

#### データモデル（追加）
```java
@Entity
public class ChallengeShare {
    @Id @GeneratedValue
    private Long id;

    @ManyToOne(optional = false)
    private User user;

    @ManyToOne(optional = false)
    private DailyChallenge challenge;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String comment; // 共有メッセージ（最大500文字程度）

    private String mood; // 任意：today, proud, grateful など

    private LocalDateTime sharedAt;
}

@Entity
public class ChallengeShareReaction {
    @Id @GeneratedValue
    private Long id;

    @ManyToOne(optional = false)
    private ChallengeShare share;

    @ManyToOne(optional = false)
    private User user;

    @Enumerated(EnumType.STRING)
    private ReactionType type; // ENCOURAGE, EMPATHY, AWESOME

    private LocalDateTime reactedAt;
}
```

#### API エンドポイント案
- `POST /api/shares` — チャレンジ達成時の共有投稿（note, mood, visibility）
- `GET /api/shares?cursor=` — タイムライン取得（ページネーション、最新優先）
- `POST /api/shares/{shareId}/reactions` — リアクション追加／切替
- `DELETE /api/shares/{shareId}/reactions/{type}` — リアクション解除
- `GET /api/shares/unread-count` — 未読数取得（バッジ通知と同じUIで表示）

#### フロントエンド UI
- `ChallengeShareFeed.tsx`：無限スクロールのカード型リスト、バッジメダル表示。
- `ShareComposer.tsx`：達成時モーダルから連動して投稿できる入力フォーム。
- `ReactionBar.tsx`：3種リアクションのトグルボタン＋リアクション数。

#### 実装ステップ
1. エンティティ・リポジトリ作成（Share, Reaction）
2. ShareService／ShareController 実装、認可は本人＋閲覧権限ユーザーに限定
3. フロントエンド：既存の`DailyChallenges`コンポーネントから投稿導線追加
4. タイムラインページ／リアクションUI／未読数バッジ連携
5. E2E テスト：投稿→リアクション→未読数クリアまで

---

### B. 統計グラフ・進捗可視化

#### 目的
- 小さな成功の蓄積を視覚化し、自己肯定感を定着させる。
- カテゴリ別／期間別のバランスを確認し、翌週の目標設定を支援。

#### 指標と可視化
| 観点 | 指標 | 可視化 | メモ |
|------|------|--------|------|
| 週間トレンド | 日ごとの達成数、獲得ポイント、ストリーク | 折れ線グラフ | 1週間／4週間切替 |
| カテゴリバランス | カテゴリ別達成数 | 100%積み上げ棒 or ドーナツチャート | 「偏り」の可視化 |
| 月間カレンダー | 日別達成状況、バッジ獲得日 | カレンダービュー | ホバーで詳細表示 |
| レベル進捗 | 現在の花レベル、次レベルまでの残ポイント | プログレスバー | ダッシュボード上部 |

#### API エンドポイント案
- `GET /api/statistics/summary?range=weekly|monthly` — メイン集計（累積・平均）
- `GET /api/statistics/daily?start=&end=` — 日別達成数リスト（チャート用）
- `GET /api/statistics/category?range=` — カテゴリ別割合
- `GET /api/statistics/calendar?year=&month=` — カレンダーデータ（1日1オブジェクト）

#### バックエンド実装メモ
- `ChallengeCompletionRepository` に期間／カテゴリでグルーピングするカスタムクエリを追加。
- `UserProgress` と連動させて花レベルの進捗を計算（次レベルの残ポイント）。
- 集計処理は `StatisticsService` として切り出し、レスポンスDTOを Tailwind グラフと合わせる。

#### フロントエンド UI
- `StatisticsDashboard.tsx`：`@/app/dashboard` 内に配置、Tabs で週間／月間を切替。
- グラフライブラリは `react-chartjs-2` または `recharts` を想定。
- カレンダー表示は `react-day-picker` 等で実装し、達成日はハイライト、バッジ日はアイコン付与。

#### 実装ステップ
1. DTO／サービス層での集計ロジック実装＋単体テスト
2. エンドポイント公開と OpenAPI ドキュメント更新
3. フロントエンド：API フック作成、グラフコンポーネント実装
4. カレンダービュー＋バッジイベント連携
5. 統合テスト／スナップショットテストで視覚崩れを確認

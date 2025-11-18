# Phase 4 実装計画: 分析・レポート機能とエクスポート

## 概要
Phase 4では、管理者およびマネージャーがチームの進捗を効率的に把握・分析できる機能を実装します。

## 実装機能

### 1. 進捗ダイジェスト機能 (Progress Digest)
定期的にチームメンバーの進捗をまとめて配信・確認できる機能

#### データベース設計
すでに作成済み: `V20__Create_Digest_And_Meeting_Tables.sql`

**progress_digests テーブル**
- 週次・月次のダイジェスト生成
- 特定の組織単位またはグループごとの集計
- 配信スケジュール設定

#### バックエンド実装
- **エンティティ**: `ProgressDigest.java`
- **リポジトリ**: `ProgressDigestRepository.java`
- **サービス**: `ProgressDigestService.java`
  - ダイジェストの自動生成ロジック
  - 期間指定での進捗集計
  - Slack/Email での配信機能
- **コントローラー**: `ProgressDigestController.java`
  - `POST /api/digests` - ダイジェスト作成
  - `GET /api/digests` - ダイジェスト一覧
  - `GET /api/digests/{id}` - ダイジェスト詳細
  - `POST /api/digests/{id}/send` - ダイジェスト送信

#### フロントエンド実装
- `/src/app/digest/page.tsx` - ダイジェスト一覧・管理画面
- ダイジェストの作成・編集フォーム
- プレビュー機能
- 配信履歴の確認

### 2. 1on1ミーティングメモ機能
マネージャーとメンバー間の1on1の記録・管理機能

#### データベース設計
すでに作成済み: `V20__Create_Digest_And_Meeting_Tables.sql`

**one_on_one_meetings テーブル**
- 参加者情報（マネージャー、メンバー）
- ミーティング日時
- メモ・アジェンダ・アクションアイテム
- フォローアップ機能

#### バックエンド実装
- **エンティティ**: `OneOnOneMeeting.java`
- **リポジトリ**: `OneOnOneMeetingRepository.java`
- **サービス**: `OneOnOneMeetingService.java`
  - ミーティングスケジュール管理
  - メモの作成・更新
  - 過去のミーティング履歴取得
  - 次回のアクションアイテム管理
- **コントローラー**: `OneOnOneMeetingController.java`
  - `POST /api/meetings/one-on-one` - ミーティング作成
  - `GET /api/meetings/one-on-one` - ミーティング一覧
  - `GET /api/meetings/one-on-one/{id}` - ミーティング詳細
  - `PUT /api/meetings/one-on-one/{id}` - ミーティング更新
  - `GET /api/meetings/one-on-one/upcoming` - 今後のミーティング
  - `GET /api/meetings/one-on-one/history` - 履歴

#### フロントエンド実装
- `/src/app/meetings/one-on-one/page.tsx` - ミーティング一覧
- ミーティング詳細・編集画面
- カレンダービュー
- アクションアイテムのトラッキング

### 3. エクスポート機能 (CSV/Excel)
データをエクスポートして外部で分析できる機能

#### バックエンド実装
- **サービス**: `ExportService.java`
  - CSV形式でのエクスポート
  - Excel形式でのエクスポート（Apache POI使用）
  - エクスポート対象:
    - ユーザー一覧
    - 進捗投稿データ
    - OKRデータ
    - 1on1ミーティング記録
    - 通報データ
- **コントローラー**: `ExportController.java`
  - `GET /api/export/users` - ユーザーエクスポート
  - `GET /api/export/progress-posts` - 進捗投稿エクスポート
  - `GET /api/export/okrs` - OKRエクスポート
  - `GET /api/export/meetings` - ミーティングエクスポート
  - `GET /api/export/reports` - 通報データエクスポート

#### 依存関係追加
`pom.xml` に Apache POI を追加:
```xml
<dependency>
    <groupId>org.apache.poi</groupId>
    <artifactId>poi-ooxml</artifactId>
    <version>5.2.3</version>
</dependency>
```

### 4. セキュリティとアクセス制御

#### RBAC強化
- 管理者ダッシュボード機能への権限チェック追加
- エクスポート機能はマネージャー以上に制限
- 1on1メモは関係者のみアクセス可能

#### 実装方針
- Spring Security の `@PreAuthorize` アノテーション使用
- カスタム権限チェックロジック
- テナント分離の徹底

## 実装順序

### フェーズ1: バックエンド基盤（Day 1-2）
1. ✅ データベースマイグレーション確認
2. エンティティ・リポジトリ実装
3. サービス層の実装
4. コントローラー実装

### フェーズ2: エクスポート機能（Day 3）
1. Apache POI 依存関係追加
2. ExportService 実装
3. ExportController 実装
4. CSV/Excel生成ロジック

### フェーズ3: フロントエンド実装（Day 4-5）
1. 進捗ダイジェスト画面
2. 1on1ミーティング画面
3. エクスポート機能のUI統合

### フェーズ4: テストと統合（Day 6）
1. 統合テスト
2. バグ修正
3. ドキュメント更新

## 技術スタック

### バックエンド
- Java 17
- Spring Boot 3.x
- Spring Data JPA
- Apache POI (Excel生成)
- OpenCSV (CSV生成)

### フロントエンド
- Next.js 14
- TypeScript
- TailwindCSS
- React Hook Form

## 期待される成果

1. **可視化の向上**
   - チーム全体の進捗を一目で把握
   - データドリブンな意思決定

2. **コミュニケーション強化**
   - 1on1の記録により継続的なフィードバック
   - 進捗ダイジェストによる情報共有

3. **データ活用**
   - エクスポート機能による外部分析
   - レポート作成の効率化

## 次のフェーズ (Phase 5)

- 自動モデレーション機能の強化
- 機械学習による異常検知
- より高度な分析機能（予測分析等）

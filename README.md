# Letteral - 企業向け匿名進捗・目標開示プラットフォーム

> 匿名性を担保しながら、日々の目標と進捗、課題・悩みを安全に共有できる業務ツール

[![Next.js](https://img.shields.io/badge/Next.js-15.5.4-black?logo=next.js)](https://nextjs.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.6-brightgreen?logo=spring)](https://spring.io/projects/spring-boot)
[![TypeScript](https://img.shields.io/badge/TypeScript-5.0-blue?logo=typescript)](https://www.typescriptlang.org/)
[![Java](https://img.shields.io/badge/Java-17-orange?logo=openjdk)](https://openjdk.org/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

---

## 📋 目次

- [概要](#概要)
- [主要機能](#主要機能)
- [技術スタック](#技術スタック)
- [クイックスタート](#クイックスタート)
- [プロジェクト構造](#プロジェクト構造)
- [実装済み機能](#実装済み機能)
- [開発ロードマップ](#開発ロードマップ)
- [ドキュメント](#ドキュメント)

---

## 概要

**Letteral** は、組織での日々の進捗共有・目標開示・悩み相談を匿名で安全に行える企業向けプラットフォームです。

従来のリアルタイムチャット機能を活かしつつ、**匿名性**と**階層的な公開範囲制御**を組み合わせることで、心理的安全性を保ちながら透明性のあるコミュニケーションを実現します。

### 解決する課題

- **進捗・目標の共有が属人化**し、会議コストや心理的安全性の低さから本音が出にくい
- **評価・1on1の材料が散在**し、事実ベースの対話が難しい
- **若手・新卒の悩みが表出しづらい**（早期検知が困難）

### 提供価値

| 対象 | 価値 |
|------|------|
| 👔 **会社・人事** | 早期に組織課題を可視化し、離職リスクを低減 |
| 👨‍💼 **管理職** | 1on1・評価面談で使える進捗データを自動生成 |
| 👤 **従業員** | 匿名で安全に本音を共有、成長実感を得られる |

---

## 主要機能

### ✅ 実装済み機能

#### 🎯 進捗投稿システム
- **日報・週報形式の進捗投稿**
  - タイトル、本文、画像添付
  - 投稿タイプ: 通常投稿 / 質問投稿
  - 匿名番号による投稿者識別（同一人物の投稿を追跡可能）

- **返信・コメント機能**
  - 投稿への返信
  - 返信の編集・削除
  - 返信者の匿名番号表示

#### 🏢 組織階層管理
- **階層的な組織構造**
  - 会社 → 部門 → チーム → グループの多階層対応
  - 組織ツリー表示（親子関係の可視化）
  - 階層ごとの投稿フィルタリング

- **組織ロール・権限管理**
  - OWNER: 組織の全権限
  - ADMIN: 組織管理・メンバー管理
  - MODERATOR: 投稿モデレーション
  - MEMBER: 一般メンバー

#### 👥 招待システム
- 組織への招待コード生成
- 招待リンクによる参加
- 招待の有効期限管理

#### 💬 リアルタイムチャット
- グループチャット（WebSocket対応）
- 個人間チャット
- メッセージ履歴

#### 🔐 認証・セキュリティ
- JWT認証
- ロールベースアクセス制御（RBAC）
- Box（グループ）単位のアクセス制御
- レート制限（API保護）

---

## 技術スタック

### バックエンド

| 技術 | バージョン | 用途 |
|------|-----------|------|
| Java | 17 | 実行環境 |
| Spring Boot | 3.5.6 | フレームワーク |
| Spring Security | - | JWT認証・RBAC |
| Spring Data JPA | - | データアクセス層 |
| WebSocket (STOMP) | - | リアルタイム通信 |
| MySQL | 8.0 | データベース |
| Flyway | - | データベースマイグレーション |

### フロントエンド

| 技術 | バージョン | 用途 |
|------|-----------|------|
| Next.js | 15.5.4 | フレームワーク（App Router） |
| TypeScript | 5.0 | 型安全性 |
| Tailwind CSS | 3.0 | スタイリング |
| Axios | - | HTTP クライアント |
| STOMP.js | 1.6.1 | WebSocket クライアント |

---

## クイックスタート

### 📋 必要な環境

- **Java 17** 以上
- **Node.js 18** 以上
- **MySQL 8.0**
- **Maven**（または同梱の`mvnw`を使用）

### ⚡ セットアップ手順

#### 1️⃣ データベースのセットアップ

```sql
-- MySQLにログイン
mysql -u root -p

-- データベース作成
CREATE DATABASE chatapp CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- ユーザー作成（オプション）
CREATE USER 'chatapp_user'@'localhost' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON chatapp.* TO 'chatapp_user'@'localhost';
FLUSH PRIVILEGES;
```

#### 2️⃣ バックエンドの起動

```bash
# backendディレクトリに移動
cd backend

# 環境変数設定（backend/src/main/resources/application.properties）
# 以下の設定を確認・編集
# spring.datasource.url=jdbc:mysql://localhost:3306/chatapp
# spring.datasource.username=root
# spring.datasource.password=your_password
# jwt.secret=your-secret-key-here

# Windowsの場合
set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.16.8-hotspot
..\mvnw.cmd spring-boot:run

# Mac/Linuxの場合
./mvnw spring-boot:run
```

バックエンドは `http://localhost:8080` で起動します。

#### 3️⃣ フロントエンドの起動

```bash
# プロジェクトルートディレクトリで
npm install
npm run dev
```

フロントエンドは `http://localhost:3000` で起動します。

#### 4️⃣ ブラウザでアクセス

```
http://localhost:3000
```

### 🎬 初期データ

アプリケーション起動時に以下のテストデータが自動作成されます：

- **テストユーザー**:
  - username: `testuser1` / password: `password123`
  - username: `testuser2` / password: `password123`

- **テスト組織**: サンプル組織と階層構造
- **テスト投稿**: サンプルの進捗投稿

---

## プロジェクト構造

```
letteral/
├── backend/                           # Spring Boot バックエンド
│   ├── src/main/java/com/chatapp/
│   │   ├── config/                   # 設定クラス（Security, WebSocket等）
│   │   ├── controller/               # REST API コントローラー
│   │   │   ├── AuthController.java              # 認証API
│   │   │   ├── ProgressPostController.java      # 進捗投稿API
│   │   │   ├── PostReplyController.java         # 返信API
│   │   │   ├── OrganizationController.java      # 組織API
│   │   │   └── ...
│   │   ├── dto/                      # データ転送オブジェクト
│   │   ├── model/                    # JPA エンティティ
│   │   │   ├── ProgressPost.java               # 進捗投稿
│   │   │   ├── PostReply.java                  # 返信
│   │   │   ├── Organization.java               # 組織
│   │   │   ├── User.java                       # ユーザー
│   │   │   └── ...
│   │   ├── repository/               # データアクセス層
│   │   ├── service/                  # ビジネスロジック
│   │   │   ├── ProgressPostService.java
│   │   │   ├── PostReplyService.java
│   │   │   ├── OrganizationService.java
│   │   │   └── ...
│   │   ├── security/                 # 認証・認可
│   │   ├── interceptor/              # リクエストインターセプター
│   │   └── exception/                # 例外ハンドリング
│   └── src/main/resources/
│       ├── application.properties    # アプリケーション設定
│       └── db/migration/            # Flywayマイグレーション
│
├── src/                              # Next.js フロントエンド
│   ├── app/                          # App Router ページ
│   │   ├── page.tsx                 # ホーム
│   │   ├── progress/                # 進捗投稿
│   │   ├── admin/                   # 管理画面
│   │   └── invite/                  # 招待
│   ├── components/                   # React コンポーネント
│   │   ├── ProgressPostForm.tsx     # 投稿フォーム
│   │   ├── ProgressPostTimeline.tsx # タイムライン
│   │   ├── OrganizationTree.tsx     # 組織ツリー
│   │   └── ...
│   └── services/                     # API クライアント
│       └── api.ts                   # API関数
│
├── docs/                             # ドキュメント
│   ├── LETTERAL_NEW_SPEC_DESIGN.md  # 仕様設計書
│   ├── API_DESIGN.md                # API設計書
│   ├── DATABASE_SCHEMA_DESIGN.md    # DB設計書
│   └── ...
│
├── package.json                      # Node.js 依存関係
└── README.md                         # このファイル
```

---

## 実装済み機能

### データベーススキーマ（主要テーブル）

| テーブル | 説明 |
|---------|------|
| `users` | ユーザー情報 |
| `organizations` | 組織（階層構造対応） |
| `organization_members` | 組織メンバーシップとロール |
| `progress_posts` | 進捗投稿（匿名番号付き） |
| `post_replies` | 投稿への返信 |
| `boxes` | グループチャット |
| `chat_messages` | チャットメッセージ |
| `invites` | 招待コード |

### API エンドポイント（主要）

#### 認証
- `POST /api/auth/register` - ユーザー登録
- `POST /api/auth/login` - ログイン
- `POST /api/auth/logout` - ログアウト

#### 進捗投稿
- `GET /api/progress-posts` - 投稿一覧取得
- `POST /api/progress-posts` - 投稿作成
- `GET /api/progress-posts/{id}` - 投稿詳細取得
- `PUT /api/progress-posts/{id}` - 投稿更新
- `DELETE /api/progress-posts/{id}` - 投稿削除

#### 返信
- `GET /api/post-replies/post/{postId}` - 投稿の返信一覧
- `POST /api/post-replies` - 返信作成
- `PUT /api/post-replies/{id}` - 返信更新
- `DELETE /api/post-replies/{id}` - 返信削除

#### 組織管理
- `GET /api/organizations/tree` - 組織ツリー取得
- `POST /api/organizations` - 組織作成
- `GET /api/organizations/{id}/members` - メンバー一覧
- `POST /api/organizations/{id}/members` - メンバー追加

詳細は [API設計書](docs/API_DESIGN.md) を参照。

---

## 開発ロードマップ

### ✅ Phase 1-7: 完了
- [x] 基本的なチャット機能
- [x] 認証・認可システム
- [x] 進捗投稿機能
- [x] 返信機能
- [x] 組織階層管理
- [x] 招待システム
- [x] 質問投稿タイプ
- [x] 匿名番号システム

### 🚧 Phase 8: 現在開発中
- [ ] OKR連携機能
- [ ] 週次/月次ダイジェスト自動生成
- [ ] 評価期間スナップショット
- [ ] 管理者ダッシュボード

### 📋 Phase 9以降: 計画中
- [ ] SSO（Single Sign-On）対応
- [ ] 外部連携（Slack, Teams等）
- [ ] モバイルアプリ
- [ ] 高度な分析・レポート機能
- [ ] 機械学習によるリスク検知

詳細は [LETTERAL_NEW_SPEC_DESIGN.md](LETTERAL_NEW_SPEC_DESIGN.md) を参照。

---

## セキュリティ・匿名性設計

### 匿名性の実装

1. **匿名番号**: 各ユーザーに組織内で一意の匿名番号を自動割り当て
2. **投稿者の匿名化**: 進捗投稿には匿名番号のみを表示
3. **返信の匿名化**: 返信にも匿名番号を使用（投稿者との関連付け可能）

### セキュリティ機能

- **JWT認証**: トークンベースの認証
- **RBAC**: ロールベースアクセス制御
- **Box権限**: グループ単位のアクセス制御
- **レート制限**: API過負荷保護
- **入力検証**: XSS, SQLインジェクション対策

---

## トラブルシューティング

### バックエンド起動エラー

**問題**: `Address already in use: bind`
```bash
# Windowsの場合
netstat -ano | findstr :8080
taskkill /PID <プロセスID> /F

# Mac/Linuxの場合
lsof -ti:8080 | xargs kill -9
```

**問題**: データベース接続エラー
- MySQL が起動しているか確認
- `application.properties` の接続情報を確認
- データベースが作成されているか確認

### フロントエンド起動エラー

**問題**: `EADDRINUSE: address already in use`
```bash
# ポート3000を使用しているプロセスを停止
npx kill-port 3000
```

---

## ドキュメント

### 📚 主要ドキュメント

| ドキュメント | 説明 |
|------------|------|
| [LETTERAL_NEW_SPEC_DESIGN.md](LETTERAL_NEW_SPEC_DESIGN.md) | 新仕様設計書 |
| [API_DESIGN.md](docs/API_DESIGN.md) | API設計書 |
| [DATABASE_SCHEMA_DESIGN.md](docs/DATABASE_SCHEMA_DESIGN.md) | データベース設計書 |
| [MIGRATION_GUIDE.md](MIGRATION_GUIDE.md) | 移行ガイド |

### 📋 Phase別サマリー

| ドキュメント | 説明 |
|------------|------|
| [PHASE5_SUMMARY.md](docs/PHASE5_SUMMARY.md) | Phase 5: Letteral新機能実装 |
| [PHASE6_SUMMARY.md](docs/PHASE6_SUMMARY.md) | Phase 6: AWS本番インフラ構築 |
| [PHASE7_SUMMARY.md](docs/PHASE7_SUMMARY.md) | Phase 7: テスト・ドキュメント整備 |

---

## 🤝 コントリビューション

プルリクエスト大歓迎です！

1. Fork this repository
2. Create feature branch (`git checkout -b feature/amazing`)
3. Commit changes (`git commit -m 'Add amazing feature'`)
4. Push to branch (`git push origin feature/amazing`)
5. Open Pull Request

### 開発ガイドライン

- コードスタイルは既存コードに準拠
- 新機能には必ずテストを追加
- コミットメッセージは日本語でも英語でも可

---

## 📄 ライセンス

MIT License - 詳細は [LICENSE](LICENSE) を参照

---

## 🙏 謝辞

- [Spring Framework](https://spring.io/)
- [Next.js](https://nextjs.org/)
- [Tailwind CSS](https://tailwindcss.com/)
- [MySQL](https://www.mysql.com/)

---

<div align="center">

**企業の透明性と心理的安全性を両立する**

Made with ❤️ by hera-16

[🌟 Star this repo](https://github.com/hera-16/letteral) | [🐛 Report Bug](https://github.com/hera-16/letteral/issues) | [💡 Request Feature](https://github.com/hera-16/letteral/issues)

</div>

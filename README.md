f# チャットアプリケーション# チャットアプリケーション



リアルタイムチャット機能を備えたモダンなWebアプリケーション。Spring Bootバックエンドと、フレンド管理、グループチャット、パブリックトピックをサポートします。Java Spring Boot（バックエンド）とNext.js（フロントエンド）で構築されたリアルタイムチャットアプリケーションです。



## 📋 目次## 技術スタック



- [概要](#概要)### バックエンド

- [主な機能](#主な機能)- **Java 17**

- [技術スタック](#技術スタック)- **Spring Boot 3.2.0**

- [プロジェクト構造](#プロジェクト構造)- **Spring Security** (JWT認証)

- [クイックスタート](#クイックスタート)- **Spring Data JPA**

- [ドキュメント](#ドキュメント)- **WebSocket** (STOMP Protocol)

- [API エンドポイント](#apiエンドポイント)- **H2 Database** (開発用)

- [開発ガイド](#開発ガイド)- **Maven**

- [トラブルシューティング](#トラブルシューティング)

### フロントエンド

---- **Next.js 14** (App Router)

- **TypeScript**

## 概要- **Tailwind CSS**

- **Axios** (HTTP クライアント)

このアプリケーションは、以下の3種類のチャット機能を提供する包括的なコミュニケーションプラットフォームです:- **STOMP.js** (WebSocket クライアント)

- **SockJS** (WebSocket フォールバック)

1. **フレンドチャット**: 1対1のプライベートメッセージング

2. **招待制グループ**: 招待コードによる限定的なグループチャット## 機能

3. **パブリックトピック**: 誰でも参加できるオープンな議論スペース

- ✅ ユーザー新規登録・ログイン（JWT認証）

---- ✅ リアルタイムチャット（WebSocket）

- ✅ メッセージ履歴の保存・取得

## 主な機能- ✅ ユーザー入退室通知

- ✅ レスポンシブデザイン

### ✨ ユーザー管理

- ユーザー登録・ログイン(JWT認証)## プロジェクト構造

- プロフィール管理

- セキュアなパスワードハッシュ化```

chat-app/

### 👥 フレンド機能├── backend/                    # Spring Boot バックエンド

- フレンドリクエスト送信・承認・拒否│   ├── src/main/java/com/chatapp/

- フレンド一覧表示│   │   ├── controller/        # REST API コントローラー

- ユーザーブロック機能│   │   ├── dto/              # データ転送オブジェクト

- 保留中のリクエスト管理│   │   ├── model/            # JPA エンティティ

│   │   ├── repository/       # データアクセス層

### 💬 グループチャット│   │   ├── security/         # 認証・認可設定

- 招待制グループの作成│   │   ├── service/          # ビジネスロジック

- 招待コードによる参加│   │   └── websocket/        # WebSocket設定

- メンバー管理(管理者・メンバー)│   └── pom.xml              # Maven 設定

- グループからの退出├── src/                      # Next.js フロントエンド

- 管理者権限の昇格│   ├── app/                 # App Router ページ

│   ├── components/          # React コンポーネント

### 🌐 パブリックトピック│   └── services/           # API クライアント

- トピックの作成・検索└── package.json            # Node.js 依存関係

- カテゴリー別表示```

- トピックのアクティブ/非アクティブ管理

- 自由参加・退出## セットアップ手順



### 🔔 リアルタイム通信### 前提条件

- WebSocket (STOMP) によるリアルタイムメッセージング

- メッセージ履歴の取得- **Java 17** 以上

- 入退室通知- **Node.js 18** 以上

- **Maven 3.6** 以上

---

### 1. リポジトリのクローン

## 技術スタック

```bash

### バックエンドgit clone <repository-url>

- **Java 17**: 最新のLTS版Javacd chat-app

- **Spring Boot 3.5.6**: アプリケーションフレームワーク```

- **Spring Security**: JWT認証・認可

- **Spring Data JPA**: データアクセス層### 2. バックエンドの起動

- **Spring WebSocket**: リアルタイム通信

- **MySQL 8.0**: 本番用データベース```bash

- **H2 Database**: 開発・テスト用データベース# バックエンドディレクトリに移動

- **Maven**: ビルドツールcd backend



### フロントエンド (予定)# Maven で依存関係をインストール・ビルド

- **React**: UIフレームワークmvn clean install

- **SockJS / STOMP**: WebSocketクライアント

- **Axios**: HTTPクライアント# Spring Boot アプリケーションを起動

mvn spring-boot:run

### インフラストラクチャ```

- **Docker**: コンテナ化

- **Docker Compose**: マルチコンテナ管理バックエンドは http://localhost:8080 で起動します。

- **Nginx**: リバースプロキシ

- **Let's Encrypt**: SSL/TLS証明書### 3. フロントエンドの起動



---新しいターミナルウィンドウで：



## プロジェクト構造```bash

# プロジェクトルートに移動（chat-app フォルダ）

```cd chat-app

チャレキャラ/

├── backend/                        # Spring Bootバックエンド# Node.js 依存関係をインストール

│   ├── src/npm install

│   │   ├── main/

│   │   │   ├── java/com/example/chatapp/# Next.js 開発サーバーを起動

│   │   │   │   ├── config/        # 設定クラスnpm run dev

│   │   │   │   ├── controller/    # REST & WebSocketコントローラー```

│   │   │   │   ├── model/         # エンティティクラス

│   │   │   │   ├── repository/    # データアクセス層フロントエンドは http://localhost:3000 で起動します。

│   │   │   │   ├── service/       # ビジネスロジック

│   │   │   │   ├── security/      # セキュリティ関連### 4. アプリケーションへのアクセス

│   │   │   │   └── dto/           # データ転送オブジェクト

│   │   │   └── resources/1. ブラウザで http://localhost:3000 にアクセス

│   │   │       ├── application.properties2. 「新規登録」でアカウントを作成

│   │   │       ├── application-dev.properties.example3. ログイン後、チャットルームでメッセージを送信

│   │   │       ├── application-prod.properties.example

│   │   │       └── db/migration/  # データベーススキーマ## API エンドポイント

│   │   └── test/                  # テストコード

│   ├── pom.xml                    # Maven依存関係### 認証

│   ├── Dockerfile                 # Dockerイメージ定義- `POST /api/auth/signup` - 新規ユーザー登録

│   └── README.md                  # バックエンドドキュメント- `POST /api/auth/signin` - ログイン

│

├── docs/                          # プロジェクトドキュメント### チャット

│   ├── API_REFERENCE.md          # API仕様書- `GET /api/chat/messages/{roomId}` - チャットメッセージ取得

│   ├── FEATURE_REQUIREMENTS.md   # 機能要件- `WebSocket /ws` - リアルタイム通信

│   ├── FRONTEND_INTEGRATION.md   # フロントエンド統合ガイド

│   └── DEPLOYMENT.md             # デプロイメントガイド## データベース

│

├── docker-compose.yml            # Docker Compose設定開発環境では H2 インメモリデータベースを使用します。

└── README.md                     # このファイル

```- **H2 Console**: http://localhost:8080/h2-console

- **JDBC URL**: `jdbc:h2:mem:chatdb`

---- **Username**: `sa`

- **Password**: `password`

## クイックスタート

## 設定

### 必要な環境

### バックエンド設定

- **Java 17** 以上

- **Maven 3.8** 以上`backend/src/main/resources/application.properties` で以下を設定可能：

- **MySQL 8.0** (本番環境の場合)

- **Docker & Docker Compose** (オプション)- データベース接続

- JWT秘密鍵・有効期限

### オプション1: ローカル開発環境(H2データベース)- CORS許可オリジン



```bash### フロントエンド設定

# 1. リポジトリをクローン

git clone <repository-url>`.env.local` で以下を設定：

cd チャレキャラ/backend

```env

# 2. ビルドと起動(H2データベース使用)NEXT_PUBLIC_API_URL=http://localhost:8080/api

mvnw spring-boot:run```



# 3. アプリケーションにアクセス## 開発

# - API: http://localhost:8080

# - H2 Console: http://localhost:8080/h2-console### バックエンド開発

```

```bash

### オプション2: MySQLを使用した開発cd backend



```bash# テスト実行

# 1. MySQLデータベースを作成mvn test

mysql -u root -p

CREATE DATABASE chatapp_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;# パッケージ作成

mvn package

# 2. application.propertiesを更新```

# spring.datasource.url=jdbc:mysql://localhost:3306/chatapp_db

# spring.datasource.username=your_username### フロントエンド開発

# spring.datasource.password=your_password

```bash

# 3. スキーマを初期化# 型チェック

mysql -u root -p chatapp_db < src/main/resources/db/migration/V1__Initial_Schema.sqlnpm run type-check



# 4. サンプルデータを挿入(オプション)# リント

mysql -u root -p chatapp_db < src/main/resources/db/migration/V2__Sample_Data.sqlnpm run lint



# 5. アプリケーションを起動# ビルド

mvnw spring-boot:runnpm run build

``````



### オプション3: Dockerを使用## 本番デプロイ



```bash### バックエンド

# 1. Docker Composeでビルド&起動

docker-compose up -d --build1. PostgreSQL などの本番データベースに変更

2. `application-prod.properties` で本番設定を作成

# 2. ログを確認3. JAR ファイルを作成して本番サーバーにデプロイ

docker-compose logs -f backend

### フロントエンド

# 3. 停止

docker-compose down1. 本番API URLを環境変数で設定

```2. Next.js アプリをビルド・デプロイ



---```bash

npm run build

## ドキュメントnpm start

```

プロジェクトには以下の詳細なドキュメントが用意されています:

## トラブルシューティング

| ドキュメント | 説明 |

|------------|------|### よくある問題

| [backend/README.md](backend/README.md) | バックエンドのセットアップと開発ガイド |

| [docs/API_REFERENCE.md](docs/API_REFERENCE.md) | 全APIエンドポイントの詳細仕様 |1. **CORS エラー**

| [docs/FEATURE_REQUIREMENTS.md](docs/FEATURE_REQUIREMENTS.md) | 機能要件と実装フェーズ |   - `application.properties` の `app.cors.allowed-origins` を確認

| [docs/FRONTEND_INTEGRATION.md](docs/FRONTEND_INTEGRATION.md) | フロントエンド開発者向け統合ガイド |

| [docs/DEPLOYMENT.md](docs/DEPLOYMENT.md) | 本番環境へのデプロイ手順 |2. **WebSocket 接続エラー**

   - バックエンドが起動していることを確認

---   - ポート 8080 が使用可能であることを確認



## API エンドポイント3. **JWT トークンエラー**

   - ローカルストレージをクリアしてログインし直す

### 認証

4. **データベース接続エラー**

| メソッド | エンドポイント | 説明 |   - H2 データベースの設定を確認

|---------|-------------|------|   - メモリ不足の場合は JVM ヒープサイズを増加

| POST | `/api/auth/register` | 新規ユーザー登録 |

| POST | `/api/auth/login` | ログイン(JWTトークン取得) |## ライセンス



### フレンド管理MIT License



| メソッド | エンドポイント | 説明 |## 貢献

|---------|-------------|------|

| GET | `/api/friends` | フレンド一覧取得 |1. Fork the repository

| POST | `/api/friends/request/{username}` | フレンドリクエスト送信 |2. Create your feature branch (`git checkout -b feature/AmazingFeature`)

| POST | `/api/friends/accept/{id}` | フレンドリクエスト承認 |3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)

| GET | `/api/friends/requests/pending` | 保留中のリクエスト取得 |4. Push to the branch (`git push origin feature/AmazingFeature`)

| DELETE | `/api/friends/{id}` | フレンド削除 |5. Open a Pull Request


### グループ管理

| メソッド | エンドポイント | 説明 |
|---------|-------------|------|
| POST | `/api/groups/invite` | 招待制グループ作成 |
| POST | `/api/groups/invite/join` | 招待コードでグループ参加 |
| GET | `/api/groups/{id}` | グループ詳細取得 |
| GET | `/api/groups/{id}/members` | グループメンバー一覧 |
| DELETE | `/api/groups/{id}/leave` | グループから退出 |
| GET | `/api/groups/public` | パブリックトピック一覧 |

### トピック管理

| メソッド | エンドポイント | 説明 |
|---------|-------------|------|
| POST | `/api/topics` | トピック作成 |
| GET | `/api/topics` | アクティブなトピック一覧 |
| GET | `/api/topics/category/{category}` | カテゴリー別トピック取得 |
| GET | `/api/topics/search?q={query}` | トピック検索 |
| PUT | `/api/topics/{id}` | トピック更新 |

### チャットメッセージ

| メソッド | エンドポイント | 説明 |
|---------|-------------|------|
| GET | `/api/chat/messages/{roomId}` | メッセージ履歴取得 |
| GET | `/api/chat/groups/{groupId}/messages` | グループメッセージ取得 |
| GET | `/api/chat/topics/{topicId}/messages` | トピックメッセージ取得 |
| GET | `/api/chat/friends/{friendshipId}/messages` | フレンドメッセージ取得 |

### WebSocket

| エンドポイント | 説明 |
|-------------|------|
| `/ws` | WebSocket接続エンドポイント |
| `/app/chat.sendMessage` | メッセージ送信 |
| `/topic/{roomId}` | ルーム別メッセージ購読 |

詳細は [docs/API_REFERENCE.md](docs/API_REFERENCE.md) を参照してください。

---

## 開発ガイド

### 環境設定

#### 開発環境

```bash
# H2データベースを使用した開発モード
mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

#### テスト実行

```bash
# 全テスト実行
mvnw test

# 特定のテストクラスを実行
mvnw test -Dtest=UserServiceTest

# カバレッジレポート生成
mvnw clean test jacoco:report
```

#### ビルド

```bash
# JARファイル生成
mvnw clean package

# テストをスキップしてビルド
mvnw clean package -DskipTests
```

### コーディング規約

- **Java**: Google Java Style Guideに準拠
- **命名規則**: 
  - クラス: PascalCase
  - メソッド/変数: camelCase
  - 定数: UPPER_SNAKE_CASE
- **インデント**: スペース4つ
- **コメント**: 重要なロジックには日本語コメントを記述

---

## トラブルシューティング

### よくある問題

#### 1. ビルドエラー: `mvnw: command not found`

**解決策**:
```bash
# Windowsの場合
mvnw.cmd spring-boot:run

# または直接Mavenを使用
mvn spring-boot:run
```

#### 2. データベース接続エラー

**原因**: MySQLが起動していない、または接続情報が間違っている

**解決策**:
```bash
# MySQLが起動しているか確認 (Windows)
sc query MySQL80

# 接続情報を確認
mysql -u your_username -p -h localhost chatapp_db
```

#### 3. ポート8080が既に使用されている

**解決策**:
```bash
# ポートを使用しているプロセスを確認 (Windows)
netstat -ano | findstr :8080

# プロセスを終了するか、別のポートを使用
mvnw spring-boot:run -Dserver.port=8081
```

#### 4. JWT トークンが無効

**原因**: トークンの有効期限切れ、または`jwt.secret`が変更された

**解決策**:
- ログアウトして再ログイン
- `application.properties`の`jwt.secret`を確認

#### 5. WebSocket接続エラー

**原因**: CORSの設定、またはプロキシの設定が間違っている

**解決策**:
- `WebSecurityConfig.java`のCORS設定を確認
- Nginxを使用している場合、WebSocketプロキシの設定を確認

---

## パフォーマンスとセキュリティ

### セキュリティ対策

- ✅ BCryptによるパスワードハッシュ化
- ✅ JWT認証・認可
- ✅ CORS設定
- ✅ SQLインジェクション対策(JPA/Hibernate)
- ✅ XSS対策(Spring Security)

### パフォーマンス最適化

- データベースインデックスの適切な設計
- HikariCPによる接続プール管理
- キャッシング戦略(必要に応じてRedis導入)
- 水平スケーリング対応(ステートレス設計)

---

## 貢献

プルリクエストを歓迎します!大きな変更の場合は、まずissueを開いて変更内容を議論してください。

### 開発フロー

1. このリポジトリをフォーク
2. フィーチャーブランチを作成 (`git checkout -b feature/AmazingFeature`)
3. 変更をコミット (`git commit -m 'Add some AmazingFeature'`)
4. ブランチにプッシュ (`git push origin feature/AmazingFeature`)
5. プルリクエストを作成

---

## ライセンス

このプロジェクトは MIT ライセンスの下で公開されています。

---

## 謝辞

- [Spring Framework](https://spring.io/)
- [MySQL](https://www.mysql.com/)
- [SockJS](https://github.com/sockjs/sockjs-client)
- [STOMP](https://stomp.github.io/)

---

**バージョン**: 1.0.0  
**最終更新**: 2024年1月

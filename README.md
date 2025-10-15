# 🎮 チャレキャラ - Challenge Character Platformf# チャットアプリケーション# チャットアプリケーション



> 「毎日のチャレンジで成長する」ソーシャルチャットアプリケーション



[![Next.js](https://img.shields.io/badge/Next.js-15.5.4-black?logo=next.js)](https://nextjs.org/)リアルタイムチャット機能を備えたモダンなWebアプリケーション。Spring Bootバックエンドと、フレンド管理、グループチャット、パブリックトピックをサポートします。Java Spring Boot（バックエンド）とNext.js（フロントエンド）で構築されたリアルタイムチャットアプリケーションです。

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.6-brightgreen?logo=spring)](https://spring.io/projects/spring-boot)

[![TypeScript](https://img.shields.io/badge/TypeScript-5.0-blue?logo=typescript)](https://www.typescriptlang.org/)

[![Java](https://img.shields.io/badge/Java-17-orange?logo=openjdk)](https://openjdk.org/)

[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)## 📋 目次## 技術スタック



---



## ✨ 特徴- [概要](#概要)### バックエンド



🎯 **毎日のチャレンジ** - 日替わりのチャレンジに挑戦してバッジを獲得  - [主な機能](#主な機能)- **Java 17**

💬 **リアルタイムチャット** - WebSocketでスムーズなコミュニケーション  

👥 **フレンド機能** - 友達を追加してプライベートチャット  - [技術スタック](#技術スタック)- **Spring Boot 3.2.0**

🏆 **バッジシステム** - 達成感のある実績管理  

🎨 **ダークテーマ** - 目に優しいモダンなUI  - [プロジェクト構造](#プロジェクト構造)- **Spring Security** (JWT認証)



---- [クイックスタート](#クイックスタート)- **Spring Data JPA**



## 🚀 クイックスタート- [ドキュメント](#ドキュメント)- **WebSocket** (STOMP Protocol)



### 📋 必要な環境- [API エンドポイント](#apiエンドポイント)- **H2 Database** (開発用)



- **Java 17** 以上- [開発ガイド](#開発ガイド)- **Maven**

- **Node.js 18** 以上

- **MySQL 8.0** (本番環境)- [トラブルシューティング](#トラブルシューティング)



### ⚡ 3ステップで起動### フロントエンド



```bash---- **Next.js 14** (App Router)

# 1️⃣ バックエンドを起動 (ターミナル1)

cd backend- **TypeScript**

mvnw.cmd spring-boot:run

## 概要- **Tailwind CSS**

# 2️⃣ フロントエンドを起動 (ターミナル2)

npm install- **Axios** (HTTP クライアント)

npm run dev

このアプリケーションは、以下の3種類のチャット機能を提供する包括的なコミュニケーションプラットフォームです:- **STOMP.js** (WebSocket クライアント)

# 3️⃣ ブラウザでアクセス

# 👉 http://localhost:3000- **SockJS** (WebSocket フォールバック)

```

1. **フレンドチャット**: 1対1のプライベートメッセージング

### 🎬 初回セットアップ

2. **招待制グループ**: 招待コードによる限定的なグループチャット## 機能

1. **データベース作成**

```sql3. **パブリックトピック**: 誰でも参加できるオープンな議論スペース

CREATE DATABASE chatapp CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

```- ✅ ユーザー新規登録・ログイン（JWT認証）



2. **環境変数設定** (`backend/.env`)---- ✅ リアルタイムチャット（WebSocket）

```env

JWT_SECRET=your-secret-key-here- ✅ メッセージ履歴の保存・取得

SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/chatapp

SPRING_DATASOURCE_USERNAME=root## 主な機能- ✅ ユーザー入退室通知

SPRING_DATASOURCE_PASSWORD=your-password

```- ✅ レスポンシブデザイン



3. **起動して完了!** 🎉### ✨ ユーザー管理



---- ユーザー登録・ログイン(JWT認証)## プロジェクト構造



## 🏗️ 技術スタック- プロフィール管理



### フロントエンド- セキュアなパスワードハッシュ化```

| 技術 | バージョン | 用途 |

|------|-----------|------|chat-app/

| ![Next.js](https://img.shields.io/badge/-Next.js-000000?logo=next.js) | 15.5.4 | フレームワーク |

| ![TypeScript](https://img.shields.io/badge/-TypeScript-3178C6?logo=typescript&logoColor=white) | 5.0 | 型安全性 |### 👥 フレンド機能├── backend/                    # Spring Boot バックエンド

| ![Tailwind CSS](https://img.shields.io/badge/-Tailwind-38B2AC?logo=tailwind-css&logoColor=white) | 3.0 | スタイリング |

| ![SockJS](https://img.shields.io/badge/-SockJS-010101?logo=socketdotio) | 1.6.1 | WebSocket |- フレンドリクエスト送信・承認・拒否│   ├── src/main/java/com/chatapp/



### バックエンド- フレンド一覧表示│   │   ├── controller/        # REST API コントローラー

| 技術 | バージョン | 用途 |

|------|-----------|------|- ユーザーブロック機能│   │   ├── dto/              # データ転送オブジェクト

| ![Java](https://img.shields.io/badge/-Java%2017-007396?logo=openjdk&logoColor=white) | 17 | 実行環境 |

| ![Spring Boot](https://img.shields.io/badge/-Spring%20Boot-6DB33F?logo=spring-boot&logoColor=white) | 3.5.6 | フレームワーク |- 保留中のリクエスト管理│   │   ├── model/            # JPA エンティティ

| ![MySQL](https://img.shields.io/badge/-MySQL-4479A1?logo=mysql&logoColor=white) | 8.0 | データベース |

| ![JWT](https://img.shields.io/badge/-JWT-000000?logo=json-web-tokens) | - | 認証 |│   │   ├── repository/       # データアクセス層



---### 💬 グループチャット│   │   ├── security/         # 認証・認可設定



## 📂 プロジェクト構造- 招待制グループの作成│   │   ├── service/          # ビジネスロジック



```- 招待コードによる参加│   │   └── websocket/        # WebSocket設定

チャレキャラ/

├── src/                    # Next.js フロントエンド- メンバー管理(管理者・メンバー)│   └── pom.xml              # Maven 設定

│   ├── app/               # App Router (ページ)

│   │   ├── page.tsx      # トップページ- グループからの退出├── src/                      # Next.js フロントエンド

│   │   └── dashboard.tsx # ダッシュボード

│   ├── components/        # UIコンポーネント- 管理者権限の昇格│   ├── app/                 # App Router ページ

│   │   ├── DailyChallenges.tsx

│   │   ├── Badges.tsx│   ├── components/          # React コンポーネント

│   │   └── ChatRoom.tsx

│   └── services/          # API通信### 🌐 パブリックトピック│   └── services/           # API クライアント

│       ├── api.ts

│       └── websocket.ts- トピックの作成・検索└── package.json            # Node.js 依存関係

│

├── backend/               # Spring Boot バックエンド- カテゴリー別表示```

│   ├── src/main/java/com/chatapp/

│   │   ├── controller/   # REST API- トピックのアクティブ/非アクティブ管理

│   │   ├── service/      # ビジネスロジック

│   │   ├── model/        # データモデル- 自由参加・退出## セットアップ手順

│   │   ├── repository/   # データアクセス

│   │   └── security/     # 認証・認可

│   └── pom.xml           # Maven依存関係

│### 🔔 リアルタイム通信### 前提条件

└── docs/                  # ドキュメント

    ├── API_REFERENCE.md- WebSocket (STOMP) によるリアルタイムメッセージング

    └── DEPLOYMENT.md

```- メッセージ履歴の取得- **Java 17** 以上



---- 入退室通知- **Node.js 18** 以上



## 🎮 主な機能- **Maven 3.6** 以上



### 1. デイリーチャレンジ---

毎日3つの新しいチャレンジが登場。達成するとバッジを獲得できます。

### 1. リポジトリのクローン

**チャレンジ例:**

- 🏃 「10分間ジョギング」## 技術スタック

- 📚 「本を30ページ読む」

- 💧 「水を2リットル飲む」```bash



### 2. リアルタイムチャット### バックエンドgit clone <repository-url>

WebSocket (STOMP) を使用した高速なメッセージング。

- **Java 17**: 最新のLTS版Javacd chat-app

**対応チャット種別:**

- 👤 フレンドチャット (1対1)- **Spring Boot 3.5.6**: アプリケーションフレームワーク```

- 👥 グループチャット (招待制)

- 🌐 パブリックトピック (誰でも参加可能)- **Spring Security**: JWT認証・認可



### 3. フレンド管理- **Spring Data JPA**: データアクセス層### 2. バックエンドの起動

- フレンドリクエスト送信・承認

- ブロック機能- **Spring WebSocket**: リアルタイム通信

- フレンド一覧表示

- **MySQL 8.0**: 本番用データベース```bash

### 4. バッジシステム

- 達成バッジの収集- **H2 Database**: 開発・テスト用データベース# バックエンドディレクトリに移動

- バッジコレクション表示

- プロフィールで自慢- **Maven**: ビルドツールcd backend



---



## 🔧 開発者向け情報### フロントエンド (予定)# Maven で依存関係をインストール・ビルド



### API エンドポイント- **React**: UIフレームワークmvn clean install



#### 認証- **SockJS / STOMP**: WebSocketクライアント

```

POST /api/auth/signup    # 新規登録- **Axios**: HTTPクライアント# Spring Boot アプリケーションを起動

POST /api/auth/signin    # ログイン

```mvn spring-boot:run



#### チャレンジ### インフラストラクチャ```

```

GET  /api/challenges/daily          # 今日のチャレンジ取得- **Docker**: コンテナ化

POST /api/challenges/{id}/complete  # チャレンジ完了

```- **Docker Compose**: マルチコンテナ管理バックエンドは http://localhost:8080 で起動します。



#### チャット- **Nginx**: リバースプロキシ

```

GET  /api/chat/messages/{roomId}    # メッセージ履歴- **Let's Encrypt**: SSL/TLS証明書### 3. フロントエンドの起動

WebSocket /ws                        # リアルタイム通信

```



#### フレンド---新しいターミナルウィンドウで：

```

GET  /api/friends                   # フレンド一覧

POST /api/friends/request/{id}      # リクエスト送信

POST /api/friends/accept/{id}       # リクエスト承認## プロジェクト構造```bash

```

# プロジェクトルートに移動（chat-app フォルダ）

詳細は [API_REFERENCE.md](docs/API_REFERENCE.md) を参照。

```cd chat-app

### ビルド・テスト

チャレキャラ/

```bash

# バックエンドテスト├── backend/                        # Spring Bootバックエンド# Node.js 依存関係をインストール

cd backend

mvnw test│   ├── src/npm install



# フロントエンドビルド│   │   ├── main/

npm run build

│   │   │   ├── java/com/example/chatapp/# Next.js 開発サーバーを起動

# 型チェック

npm run type-check│   │   │   │   ├── config/        # 設定クラスnpm run dev

```

│   │   │   │   ├── controller/    # REST & WebSocketコントローラー```

---

│   │   │   │   ├── model/         # エンティティクラス

## 🌈 カラーテーマ

│   │   │   │   ├── repository/    # データアクセス層フロントエンドは http://localhost:3000 で起動します。

アプリケーションは統一されたダークテーマを採用:

│   │   │   │   ├── service/       # ビジネスロジック

| 色名 | Hex | 用途 |

|------|-----|------|│   │   │   │   ├── security/      # セキュリティ関連### 4. アプリケーションへのアクセス

| Dark | `#222831` | 背景 |

| Medium | `#393E46` | カード背景 |│   │   │   │   └── dto/           # データ転送オブジェクト

| Cyan | `#00ADB5` | アクセント |

| Light | `#EEEEEE` | テキスト |│   │   │   └── resources/1. ブラウザで http://localhost:3000 にアクセス



---│   │   │       ├── application.properties2. 「新規登録」でアカウントを作成



## 📚 ドキュメント│   │   │       ├── application-dev.properties.example3. ログイン後、チャットルームでメッセージを送信



| ドキュメント | 説明 |│   │   │       ├── application-prod.properties.example

|------------|------|

| [API_REFERENCE.md](docs/API_REFERENCE.md) | API仕様書 |│   │   │       └── db/migration/  # データベーススキーマ## API エンドポイント

| [DEPLOYMENT.md](docs/DEPLOYMENT.md) | デプロイ手順 |

| [FEATURE_REQUIREMENTS.md](docs/FEATURE_REQUIREMENTS.md) | 機能要件 |│   │   └── test/                  # テストコード

| [PRODUCTION_SETUP.md](backend/PRODUCTION_SETUP.md) | 本番環境設定 |

│   ├── pom.xml                    # Maven依存関係### 認証

---

│   ├── Dockerfile                 # Dockerイメージ定義- `POST /api/auth/signup` - 新規ユーザー登録

## 🤝 コントリビューション

│   └── README.md                  # バックエンドドキュメント- `POST /api/auth/signin` - ログイン

プルリクエスト大歓迎です!

│

1. Fork this repository

2. Create feature branch (`git checkout -b feature/amazing`)├── docs/                          # プロジェクトドキュメント### チャット

3. Commit changes (`git commit -m 'Add amazing feature'`)

4. Push to branch (`git push origin feature/amazing`)│   ├── API_REFERENCE.md          # API仕様書- `GET /api/chat/messages/{roomId}` - チャットメッセージ取得

5. Open Pull Request

│   ├── FEATURE_REQUIREMENTS.md   # 機能要件- `WebSocket /ws` - リアルタイム通信

---

│   ├── FRONTEND_INTEGRATION.md   # フロントエンド統合ガイド

## 📄 ライセンス

│   └── DEPLOYMENT.md             # デプロイメントガイド## データベース

MIT License - 詳細は [LICENSE](LICENSE) を参照

│

---

├── docker-compose.yml            # Docker Compose設定開発環境では H2 インメモリデータベースを使用します。

## 🙏 謝辞

└── README.md                     # このファイル

- [Spring Framework](https://spring.io/)

- [Next.js](https://nextjs.org/)```- **H2 Console**: http://localhost:8080/h2-console

- [Tailwind CSS](https://tailwindcss.com/)

- [MySQL](https://www.mysql.com/)- **JDBC URL**: `jdbc:h2:mem:chatdb`



------- **Username**: `sa`



<div align="center">- **Password**: `password`



**Made with ❤️ for challenge lovers**## クイックスタート



[🌟 Star this repo](https://github.com/hera-16/letteral) | [🐛 Report Bug](https://github.com/hera-16/letteral/issues) | [💡 Request Feature](https://github.com/hera-16/letteral/issues)## 設定



</div>### 必要な環境


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

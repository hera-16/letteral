# チャットアプリケーション - バックエンド

Spring Boot ベースのリアルタイムチャットアプリケーションのバックエンド実装

## 📋 機能概要

### ✅ 実装完了機能

#### 🔐 認証・ユーザー管理
- ユーザー登録・ログイン（JWT認証）
- ユーザープロフィール管理

#### 👥 フレンド機能
- フレンドリクエスト送信・承認・拒否
- フレンド一覧表示
- ユーザーブロック機能
- フレンド削除

#### 💬 チャット機能
- リアルタイムメッセージング（WebSocket/STOMP）
- メッセージ履歴取得
- 複数ルームタイプサポート

#### 🏘️ グループチャット
- 招待制グループ作成・管理
- 招待コードによる参加
- メンバー管理（昇格・退出）
- グループメンバー一覧

#### 📢 公開トピック
- トピック作成・編集・削除
- カテゴリー別分類
- トピック検索機能
- アクティブ/非アクティブ管理

---

## 🏗️ アーキテクチャ

### 技術スタック
- **Java 17**
- **Spring Boot 3.5.6**
- **Spring Security** (JWT認証)
- **Spring Data JPA**
- **WebSocket/STOMP**
- **MySQL / H2**
- **Maven**

### プロジェクト構造
```
backend/
├── src/main/java/com/chatapp/
│   ├── config/           # 設定クラス
│   │   ├── WebSecurityConfig.java
│   │   └── WebSocketConfig.java
│   ├── controller/       # REST/WebSocketコントローラ
│   │   ├── AuthController.java
│   │   ├── FriendController.java
│   │   ├── GroupController.java
│   │   ├── TopicController.java
│   │   ├── ChatController.java
│   │   └── ChatRestController.java
│   ├── dto/             # データ転送オブジェクト
│   │   ├── JwtResponse.java
│   │   ├── LoginRequest.java
│   │   ├── SignupRequest.java
│   │   └── ChatMessageDto.java
│   ├── model/           # エンティティクラス
│   │   ├── User.java
│   │   ├── Friend.java
│   │   ├── ChatMessage.java
│   │   ├── Group.java
│   │   ├── GroupMember.java
│   │   └── Topic.java
│   ├── repository/      # データアクセス層
│   │   ├── UserRepository.java
│   │   ├── FriendRepository.java
│   │   ├── ChatMessageRepository.java
│   │   ├── GroupRepository.java
│   │   ├── GroupMemberRepository.java
│   │   └── TopicRepository.java
│   ├── security/        # セキュリティ関連
│   │   ├── JwtUtils.java
│   │   ├── AuthTokenFilter.java
│   │   ├── AuthEntryPointJwt.java
│   │   └── UserPrincipal.java
│   ├── service/         # ビジネスロジック層
│   │   ├── CustomUserDetailsService.java
│   │   ├── FriendService.java
│   │   ├── GroupService.java
│   │   ├── TopicService.java
│   │   └── ChatService.java
│   └── websocket/       # WebSocketイベント
│       └── WebSocketEventListener.java
├── src/main/resources/
│   └── application.properties
└── pom.xml
```

---

## 🚀 セットアップ

### 前提条件
- JDK 17以上
- Maven 3.6以上
- MySQL 8.0以上（または H2 for 開発）

### 1. リポジトリクローン
```bash
git clone https://github.com/hera-16/letteral.git
cd letteral/backend
```

### 2. データベース設定

#### MySQL使用の場合
```sql
CREATE DATABASE chatapp;
CREATE USER 'chatapp_user'@'localhost' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON chatapp.* TO 'chatapp_user'@'localhost';
FLUSH PRIVILEGES;
```

`src/main/resources/application.properties` を編集：
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/chatapp
spring.datasource.username=chatapp_user
spring.datasource.password=your_password
spring.jpa.hibernate.ddl-auto=update
```

#### H2使用の場合（開発用）
```properties
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driverClassName=org.h2.Driver
spring.jpa.hibernate.ddl-auto=create-drop
spring.h2.console.enabled=true
```

### 3. JWT秘密鍵設定
```properties
app.jwt.secret=your-256-bit-secret-key-here
app.jwt.expiration=86400000
```

### 4. ビルド・実行
```bash
# ビルド
./mvnw clean install

# 実行
./mvnw spring-boot:run
```

アプリケーションは `http://localhost:8080` で起動します。

### 5. デフォルトのテストユーザー

アプリケーション起動時に、次のテストユーザーが自動的に作成されます。すべてのアカウントで共通パスワード `password123` を使用できます。

| ユーザー名 | 表示名 | メールアドレス |
| --- | --- | --- |
| alice | Alice Wonderland | alice@example.com |
| bob | Bob Builder | bob@example.com |
| charlie | Charlie Chaplin | charlie@example.com |
| diana | Diana Prince | diana@example.com |
| eve | Eve Online | eve@example.com |

> 🔐 **補足:** パスワードはアプリ内で BCrypt によってハッシュ化されて保存されます。ログインテスト時のみプレーンテキストの `password123` を使用してください。

---

## 📡 API エンドポイント

詳細は [API_REFERENCE.md](../docs/API_REFERENCE.md) を参照してください。

### 主要エンドポイント

#### 認証
- `POST /api/auth/signup` - ユーザー登録
- `POST /api/auth/login` - ログイン

#### フレンド
- `GET /api/friends` - フレンド一覧
- `POST /api/friends/request/{username}` - リクエスト送信
- `POST /api/friends/accept/{requestId}` - リクエスト承認

#### グループ
- `POST /api/groups/invite` - 招待制グループ作成
- `POST /api/groups/invite/join` - 招待コード入力で参加
- `GET /api/groups/public` - 公開トピック一覧

#### トピック
- `GET /api/topics` - 全トピック一覧
- `POST /api/topics` - トピック作成
- `GET /api/topics/search?q=keyword` - トピック検索

#### チャット
- `GET /api/chat/groups/{groupId}/messages` - グループメッセージ取得
- `GET /api/chat/topics/{topicId}/messages` - トピックメッセージ取得

#### WebSocket
- **接続:** `ws://localhost:8080/ws`
- **送信:** `/app/chat.sendMessage`
- **購読:** `/topic/{roomId}`, `/topic/group-{id}`, `/topic/topic-{id}`

---

## 🔧 設定オプション

### application.properties

```properties
# サーバー設定
server.port=8080

# データソース
spring.datasource.url=jdbc:mysql://localhost:3306/chatapp
spring.datasource.username=root
spring.datasource.password=password

# JPA/Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

# JWT設定
app.jwt.secret=your-secret-key
app.jwt.expiration=86400000

# CORS設定
app.cors.allowed-origins=http://localhost:3000,http://localhost:3001

# WebSocket
spring.websocket.allowed-origins=*

# H2コンソール（開発用）
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console
```

---

## 🧪 テスト

```bash
# 全テスト実行
./mvnw test

# 統合テスト実行
./mvnw verify

# カバレッジレポート生成
./mvnw jacoco:report
```

---

## 📊 データベーススキーマ

### エンティティ関係図（ER図）

```
User (users)
  ├── Friend (friends) - 多対多（自己参照）
  ├── ChatMessage (chat_messages) - 1対多
  ├── Group (groups) - 1対多（作成者）
  ├── GroupMember (group_members) - 多対多
  └── Topic (topics) - 1対多（作成者）

Group (groups)
  └── GroupMember (group_members) - 1対多

GroupMember (group_members)
  ├── Group - 多対1
  └── User - 多対1
```

### 主要テーブル

#### users
- id, username, email, password, display_name, created_at

#### friends
- id, requester_id, addressee_id, status, requested_at, responded_at

#### groups
- id, name, description, group_type, invite_code, max_members, creator_id, created_at

#### group_members
- id, group_id, user_id, role, joined_at

#### topics
- id, name, description, category, creator_id, created_at, is_active

#### chat_messages
- id, content, sender_id, room_id, message_type, created_at

---

## 🔐 セキュリティ

### 認証フロー
1. ユーザーが `/api/auth/login` でログイン
2. サーバーがJWTトークンを発行
3. クライアントが `Authorization: Bearer {token}` ヘッダーで送信
4. サーバーがトークンを検証して認証

### パスワードハッシュ化
BCryptPasswordEncoderを使用（強度10）

### CORS設定
設定ファイルで許可するオリジンを指定

---

## 🐛 トラブルシューティング

### ビルドエラー
```bash
# 依存関係を再取得
./mvnw clean install -U
```

### WebSocket接続エラー
- CORS設定を確認
- ファイアウォールでポート8080が開いているか確認

### データベース接続エラー
- MySQL/H2が起動しているか確認
- application.propertiesの接続情報を確認
- データベースユーザーの権限を確認

---

## 📚 関連ドキュメント

- [機能要件定義](../docs/FEATURE_REQUIREMENTS.md)
- [APIリファレンス](../docs/API_REFERENCE.md)
- [フロントエンド統合ガイド](../docs/FRONTEND_INTEGRATION.md) *(作成予定)*

---

## 🤝 コントリビューション

1. Forkする
2. Feature branchを作成 (`git checkout -b feature/amazing-feature`)
3. 変更をCommit (`git commit -m 'Add amazing feature'`)
4. Branchにpush (`git push origin feature/amazing-feature`)
5. Pull Requestを作成

---

## 📄 ライセンス

このプロジェクトはMITライセンスの下でライセンスされています。

---

## 👤 作成者

**hera-16**
- GitHub: [@hera-16](https://github.com/hera-16)
- Repository: [letteral](https://github.com/hera-16/letteral)

---

## 🙏 謝辞

- Spring Boot チーム
- WebSocket/STOMP プロトコル
- JWT.io

---

**最終更新:** 2025年10月1日

# チャットアプリケーション

Java Spring Boot（バックエンド）とNext.js（フロントエンド）で構築されたリアルタイムチャットアプリケーションです。

## 技術スタック

### バックエンド
- **Java 17**
- **Spring Boot 3.2.0**
- **Spring Security** (JWT認証)
- **Spring Data JPA**
- **WebSocket** (STOMP Protocol)
- **H2 Database** (開発用)
- **Maven**

### フロントエンド
- **Next.js 14** (App Router)
- **TypeScript**
- **Tailwind CSS**
- **Axios** (HTTP クライアント)
- **STOMP.js** (WebSocket クライアント)
- **SockJS** (WebSocket フォールバック)

## 機能

- ✅ ユーザー新規登録・ログイン（JWT認証）
- ✅ リアルタイムチャット（WebSocket）
- ✅ メッセージ履歴の保存・取得
- ✅ ユーザー入退室通知
- ✅ レスポンシブデザイン

## プロジェクト構造

```
chat-app/
├── backend/                    # Spring Boot バックエンド
│   ├── src/main/java/com/chatapp/
│   │   ├── controller/        # REST API コントローラー
│   │   ├── dto/              # データ転送オブジェクト
│   │   ├── model/            # JPA エンティティ
│   │   ├── repository/       # データアクセス層
│   │   ├── security/         # 認証・認可設定
│   │   ├── service/          # ビジネスロジック
│   │   └── websocket/        # WebSocket設定
│   └── pom.xml              # Maven 設定
├── src/                      # Next.js フロントエンド
│   ├── app/                 # App Router ページ
│   ├── components/          # React コンポーネント
│   └── services/           # API クライアント
└── package.json            # Node.js 依存関係
```

## セットアップ手順

### 前提条件

- **Java 17** 以上
- **Node.js 18** 以上
- **Maven 3.6** 以上

### 1. リポジトリのクローン

```bash
git clone <repository-url>
cd chat-app
```

### 2. バックエンドの起動

```bash
# バックエンドディレクトリに移動
cd backend

# Maven で依存関係をインストール・ビルド
mvn clean install

# Spring Boot アプリケーションを起動
mvn spring-boot:run
```

バックエンドは http://localhost:8080 で起動します。

### 3. フロントエンドの起動

新しいターミナルウィンドウで：

```bash
# プロジェクトルートに移動（chat-app フォルダ）
cd chat-app

# Node.js 依存関係をインストール
npm install

# Next.js 開発サーバーを起動
npm run dev
```

フロントエンドは http://localhost:3000 で起動します。

### 4. アプリケーションへのアクセス

1. ブラウザで http://localhost:3000 にアクセス
2. 「新規登録」でアカウントを作成
3. ログイン後、チャットルームでメッセージを送信

## API エンドポイント

### 認証
- `POST /api/auth/signup` - 新規ユーザー登録
- `POST /api/auth/signin` - ログイン

### チャット
- `GET /api/chat/messages/{roomId}` - チャットメッセージ取得
- `WebSocket /ws` - リアルタイム通信

## データベース

開発環境では H2 インメモリデータベースを使用します。

- **H2 Console**: http://localhost:8080/h2-console
- **JDBC URL**: `jdbc:h2:mem:chatdb`
- **Username**: `sa`
- **Password**: `password`

## 設定

### バックエンド設定

`backend/src/main/resources/application.properties` で以下を設定可能：

- データベース接続
- JWT秘密鍵・有効期限
- CORS許可オリジン

### フロントエンド設定

`.env.local` で以下を設定：

```env
NEXT_PUBLIC_API_URL=http://localhost:8080/api
```

## 開発

### バックエンド開発

```bash
cd backend

# テスト実行
mvn test

# パッケージ作成
mvn package
```

### フロントエンド開発

```bash
# 型チェック
npm run type-check

# リント
npm run lint

# ビルド
npm run build
```

## 本番デプロイ

### バックエンド

1. PostgreSQL などの本番データベースに変更
2. `application-prod.properties` で本番設定を作成
3. JAR ファイルを作成して本番サーバーにデプロイ

### フロントエンド

1. 本番API URLを環境変数で設定
2. Next.js アプリをビルド・デプロイ

```bash
npm run build
npm start
```

## トラブルシューティング

### よくある問題

1. **CORS エラー**
   - `application.properties` の `app.cors.allowed-origins` を確認

2. **WebSocket 接続エラー**
   - バックエンドが起動していることを確認
   - ポート 8080 が使用可能であることを確認

3. **JWT トークンエラー**
   - ローカルストレージをクリアしてログインし直す

4. **データベース接続エラー**
   - H2 データベースの設定を確認
   - メモリ不足の場合は JVM ヒープサイズを増加

## ライセンス

MIT License

## 貢献

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

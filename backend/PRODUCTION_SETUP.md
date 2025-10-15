# 本番環境セットアップガイド

このガイドでは、本番環境にデプロイするための環境変数と設定方法を説明します。

## 📋 環境変数の設定

### 必須の環境変数

本番環境では、以下の環境変数を必ず設定してください:

#### 1. JWT秘密鍵 (必須)

```bash
JWT_SECRET=your-secure-random-jwt-secret-at-least-64-characters-long
```

**生成方法:**
```powershell
# PowerShellで強力なランダムキーを生成 (64バイト = 88文字のBase64文字列)
[Convert]::ToBase64String((1..64 | ForEach-Object { Get-Random -Minimum 0 -Maximum 256 }))
```

#### 2. データベース設定 (必須)

```bash
# データベースURL (本番環境では必ずSSL有効化: useSSL=true)
SPRING_DATASOURCE_URL=jdbc:mysql://your-db-host:3306/chatapp?useSSL=true&serverTimezone=Asia/Tokyo

# データベースユーザー名
SPRING_DATASOURCE_USERNAME=chatapp_user

# データベースパスワード (強力なパスワードを使用)
SPRING_DATASOURCE_PASSWORD=your-secure-database-password
```

#### 3. CORS設定 (必須)

```bash
# フロントエンドのURLをカンマ区切りで指定
CORS_ALLOWED_ORIGINS=https://your-frontend-domain.com,https://www.your-frontend-domain.com
```

#### 4. サーバーポート (オプション)

```bash
# デフォルトは8080
SERVER_PORT=8080
```

## 🔧 設定方法

### 方法1: .envファイルを使用 (推奨)

1. `.env.example` をコピー:
```bash
cd backend
cp .env.example .env
```

2. `.env` ファイルを編集:
```bash
# Windows
notepad .env

# Linux/Mac
nano .env
```

3. 上記の必須環境変数を設定

4. アプリケーションを起動:
```bash
mvn spring-boot:run
# または
java -jar target/chat-backend-0.0.1-SNAPSHOT.jar
```

### 方法2: systemdサービスで環境変数を設定

`/etc/systemd/system/chatapp.service`:

```ini
[Unit]
Description=Chat Application
After=mysql.service

[Service]
User=chatapp
WorkingDirectory=/opt/chatapp

# 環境変数ファイルから読み込み
EnvironmentFile=/opt/chatapp/.env

# または直接指定
# Environment="JWT_SECRET=your_secret"
# Environment="SPRING_DATASOURCE_URL=jdbc:mysql://..."
# Environment="CORS_ALLOWED_ORIGINS=https://..."

ExecStart=/usr/bin/java -jar /opt/chatapp/chatapp.jar
Restart=always

[Install]
WantedBy=multi-user.target
```

サービス有効化:
```bash
sudo systemctl daemon-reload
sudo systemctl enable chatapp.service
sudo systemctl start chatapp.service
```

### 方法3: Docker Composeで環境変数を設定

プロジェクトルートに `.env` ファイルを作成:

```env
# MySQL Configuration
MYSQL_ROOT_PASSWORD=secure_root_password
MYSQL_DATABASE=chatapp
MYSQL_USER=chatapp_user
MYSQL_PASSWORD=secure_password

# Application Configuration
JWT_SECRET=your_secure_jwt_secret
SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/chatapp?useSSL=true&serverTimezone=Asia/Tokyo
SPRING_DATASOURCE_USERNAME=chatapp_user
SPRING_DATASOURCE_PASSWORD=secure_password
CORS_ALLOWED_ORIGINS=https://your-domain.com
```

起動:
```bash
docker-compose up -d
```

## ⚙️ application.properties の設定

`backend/src/main/resources/application.properties` では環境変数が自動的に読み込まれます:

```properties
# 環境変数から読み込み、未設定の場合はデフォルト値を使用
jwt.secret=${JWT_SECRET:changeme-please-set-jwt-secret-environment-variable}

spring.datasource.url=${SPRING_DATASOURCE_URL:jdbc:mysql://localhost:3306/chatapp?useSSL=false&serverTimezone=Asia/Tokyo}
spring.datasource.username=${SPRING_DATASOURCE_USERNAME:chatapp_user}
spring.datasource.password=${SPRING_DATASOURCE_PASSWORD:chatapp_password}

app.cors.allowed-origins=${CORS_ALLOWED_ORIGINS:http://localhost:3000}

server.port=${SERVER_PORT:8080}
```

## 🔒 セキュリティチェックリスト

本番環境にデプロイする前に、以下を確認してください:

- [ ] **JWT_SECRET**: 64文字以上の強力なランダム文字列を設定
- [ ] **データベースパスワード**: デフォルト値から変更し、強力なパスワードを使用
- [ ] **データベースSSL**: `useSSL=true` を設定
- [ ] **CORS**: 実際のフロントエンドドメインのみを許可 (localhostを削除)
- [ ] **`.env`ファイル**: gitignoreで除外されていることを確認
- [ ] **環境変数**: 本番サーバーで正しく設定されていることを確認
- [ ] **ファイアウォール**: 必要なポートのみ開放 (通常は80, 443)
- [ ] **HTTPS**: SSL/TLS証明書を取得してHTTPSを有効化

## 📝 設定例

### 開発環境の例

```bash
JWT_SECRET=dev-secret-key-not-for-production
SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/chatapp?useSSL=false&serverTimezone=Asia/Tokyo
SPRING_DATASOURCE_USERNAME=root
SPRING_DATASOURCE_PASSWORD=password
CORS_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:3001
SERVER_PORT=8080
```

### 本番環境の例

```bash
JWT_SECRET=iHRhLUYJfKeNreIKNDHSmzlujwPAYEA/XNxU+EI8mGJnPGESnNGfzZzU9sh9vZzOpiNkkH0b7Wh4Cb8wm1nGqQ==
SPRING_DATASOURCE_URL=jdbc:mysql://db.production.example.com:3306/chatapp?useSSL=true&serverTimezone=Asia/Tokyo
SPRING_DATASOURCE_USERNAME=chatapp_prod_user
SPRING_DATASOURCE_PASSWORD=Str0ng!P@ssw0rd#2025$Secure
CORS_ALLOWED_ORIGINS=https://chatapp.example.com,https://www.chatapp.example.com
SERVER_PORT=8080
```

## 🐛 トラブルシューティング

### 環境変数が読み込まれない

1. `.env` ファイルが `backend/` ディレクトリに配置されているか確認
2. ファイル名が正確に `.env` であることを確認 (`.env.txt` などではない)
3. systemdを使用する場合、`EnvironmentFile` のパスが正しいか確認
4. アプリケーションを再起動

### JWT認証エラー

1. `JWT_SECRET` が正しく設定されているか確認
2. 64文字以上のランダム文字列を使用しているか確認
3. 本番環境と開発環境で異なる秘密鍵を使用しているか確認

### データベース接続エラー

1. データベースURL、ユーザー名、パスワードが正しいか確認
2. MySQLサーバーが起動しているか確認
3. ファイアウォールでデータベースポート(3306)が開いているか確認
4. SSLを使用する場合、証明書が正しく設定されているか確認

### CORSエラー

1. `CORS_ALLOWED_ORIGINS` にフロントエンドのURLが含まれているか確認
2. プロトコル(http/https)、ドメイン、ポートが正確に一致しているか確認
3. カンマの前後にスペースがないことを確認

## 📚 関連ドキュメント

- [完全なデプロイガイド](../docs/DEPLOYMENT.md)
- [API仕様書](../docs/API_REFERENCE.md)
- [フロントエンド統合ガイド](../docs/FRONTEND_INTEGRATION.md)

## ⚠️ 重要な注意事項

1. **`.env` ファイルは絶対にGitにコミットしないでください**
   - `.gitignore` で除外されていますが、念のため `git status` で確認してください

2. **本番環境では必ず環境変数を使用してください**
   - `application.properties` に直接機密情報を書き込まないでください

3. **JWT秘密鍵は定期的に変更してください**
   - 変更する場合、既存のJWTトークンは無効になるため、ユーザーは再ログインが必要です

4. **データベースバックアップを定期的に取得してください**
   - 詳細は [DEPLOYMENT.md](../docs/DEPLOYMENT.md#バックアップ戦略) を参照

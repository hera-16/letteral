# デプロイメントガイド

このドキュメントは、チャットアプリケーションを本番環境にデプロイするための手順を説明します。

## 目次

1. [前提条件](#前提条件)
2. [環境変数の設定](#環境変数の設定)
3. [データベースのセットアップ](#データベースのセットアップ)
4. [Dockerを使用したデプロイ](#dockerを使用したデプロイ)
5. [手動デプロイ](#手動デプロイ)
6. [Nginx リバースプロキシの設定](#nginx-リバースプロキシの設定)
7. [SSL/TLS の設定](#ssltls-の設定)
8. [モニタリングとログ](#モニタリングとログ)
9. [バックアップ戦略](#バックアップ戦略)

---

## 前提条件

### サーバー要件

- **OS**: Ubuntu 20.04 LTS 以上 / CentOS 8 以上
- **CPU**: 最低 2コア (推奨: 4コア以上)
- **RAM**: 最低 4GB (推奨: 8GB以上)
- **ストレージ**: 最低 20GB (推奨: 50GB以上 SSD)

### ソフトウェア要件

- **Java**: OpenJDK 17 以上
- **Maven**: 3.8 以上 (ビルド用)
- **MySQL**: 8.0 以上
- **Docker**: 20.10 以上 (オプション)
- **Docker Compose**: 2.0 以上 (オプション)
- **Nginx**: 1.18 以上 (リバースプロキシ用)

---

## 環境変数の設定

### 本番環境の環境変数

本番環境では、`backend/.env.example` をコピーして `backend/.env` を作成し、以下の環境変数を設定します:

```bash
# backend/.env ファイルを作成
cd backend
cp .env.example .env
nano .env  # または好きなエディタで編集
```

**必須の環境変数:**

```bash
# JWT設定 (必須)
# 強力なランダム文字列を生成して設定してください (64文字以上推奨)
# 生成方法: PowerShell: [Convert]::ToBase64String((1..64 | ForEach-Object { Get-Random -Minimum 0 -Maximum 256 }))
JWT_SECRET=your_very_long_and_secure_random_secret_key_at_least_64_characters

# データベース設定 (必須)
# 本番環境のMySQLサーバー接続情報
# SSLを有効化することを推奨: useSSL=true
SPRING_DATASOURCE_URL=jdbc:mysql://your-db-host:3306/chatapp?useSSL=true&serverTimezone=Asia/Tokyo
SPRING_DATASOURCE_USERNAME=chatapp_user
SPRING_DATASOURCE_PASSWORD=your_secure_database_password

# CORS設定 (必須)
# フロントエンドのURLをカンマ区切りで指定
CORS_ALLOWED_ORIGINS=https://your-frontend-domain.com,https://www.your-frontend-domain.com

# サーバー設定 (オプション)
SERVER_PORT=8080
```

### systemd サービスファイルの例

`/etc/systemd/system/chatapp.service` を作成:

```ini
[Unit]
Description=Chat Application Spring Boot Service
After=syslog.target network.target mysql.service

[Service]
User=chatapp
WorkingDirectory=/opt/chatapp
ExecStart=/usr/bin/java -jar /opt/chatapp/chatapp.jar
SuccessExitStatus=143
StandardOutput=journal
StandardError=journal

# 環境変数ファイルから読み込み
EnvironmentFile=/opt/chatapp/.env

Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
```

または、環境変数を直接指定する場合:

```ini
[Service]
# ... 他の設定 ...

# Environment variables
Environment="SPRING_PROFILES_ACTIVE=prod"
Environment="JWT_SECRET=your_jwt_secret"
Environment="SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/chatapp?useSSL=true&serverTimezone=Asia/Tokyo"
Environment="SPRING_DATASOURCE_USERNAME=chatapp_user"
Environment="SPRING_DATASOURCE_PASSWORD=your_secure_password"
Environment="CORS_ALLOWED_ORIGINS=https://your-frontend-domain.com"
```

サービスを有効化:

```bash
sudo systemctl daemon-reload
sudo systemctl enable chatapp.service
sudo systemctl start chatapp.service
```

---

## データベースのセットアップ

### MySQL のインストール

```bash
# Ubuntu
sudo apt update
sudo apt install mysql-server

# CentOS
sudo yum install mysql-server
```

### データベースとユーザーの作成

```sql
-- MySQLにログイン
sudo mysql -u root -p

-- データベース作成
CREATE DATABASE chatapp_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- ユーザー作成と権限付与
CREATE USER 'chatapp_user'@'localhost' IDENTIFIED BY 'your_secure_password';
GRANT ALL PRIVILEGES ON chatapp_db.* TO 'chatapp_user'@'localhost';
FLUSH PRIVILEGES;

-- 接続確認
EXIT;
mysql -u chatapp_user -p chatapp_db
```

### スキーマの初期化

```bash
# マイグレーションスクリプトを実行
mysql -u chatapp_user -p chatapp_db < backend/src/main/resources/db/migration/V1__Initial_Schema.sql

# サンプルデータを挿入(オプション)
mysql -u chatapp_user -p chatapp_db < backend/src/main/resources/db/migration/V2__Sample_Data.sql
```

---

## Dockerを使用したデプロイ

### Docker Compose によるデプロイ

1. **環境変数ファイルの作成**

本番環境用の `.env` ファイルを作成 (プロジェクトルートに配置):

```env
# MySQL Configuration
MYSQL_ROOT_PASSWORD=secure_root_password_here
MYSQL_DATABASE=chatapp
MYSQL_USER=chatapp_user
MYSQL_PASSWORD=secure_chatapp_password_here

# Application Configuration
JWT_SECRET=your_secure_random_jwt_secret_at_least_64_characters_long
CORS_ALLOWED_ORIGINS=https://your-frontend-domain.com,https://www.your-frontend-domain.com
SERVER_PORT=8080

# Database URL for Spring Boot
SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/chatapp?useSSL=true&serverTimezone=Asia/Tokyo
SPRING_DATASOURCE_USERNAME=chatapp_user
SPRING_DATASOURCE_PASSWORD=secure_chatapp_password_here
```

**重要:** 本番環境では強力なパスワードとランダムなJWTシークレットを使用してください。

2. **コンテナのビルドと起動**

```bash
# ビルドと起動
docker-compose up -d --build

# ログの確認
docker-compose logs -f backend

# 停止
docker-compose down

# 完全削除(ボリュームも含む)
docker-compose down -v
```

3. **ヘルスチェック**

```bash
# バックエンドの状態確認
curl http://localhost:8080/actuator/health

# データベース接続確認
docker-compose exec mysql mysql -u chatapp_user -p
```

---

## 手動デプロイ

### 1. アプリケーションのビルド

```bash
cd backend
./mvnw clean package -DskipTests
```

### 2. JARファイルの配置

```bash
# デプロイディレクトリの作成
sudo mkdir -p /opt/chatapp
sudo chown chatapp:chatapp /opt/chatapp

# JARファイルのコピー
sudo cp target/chatapp-0.0.1-SNAPSHOT.jar /opt/chatapp/chatapp.jar
```

### 3. 設定ファイルの配置

```bash
# 本番用設定ファイルを配置
sudo cp src/main/resources/application-prod.properties.example /opt/chatapp/application-prod.properties

# 必要な値を編集
sudo nano /opt/chatapp/application-prod.properties
```

### 4. アプリケーションの起動

```bash
# 手動起動(テスト用)
java -jar /opt/chatapp/chatapp.jar --spring.config.location=/opt/chatapp/application-prod.properties

# systemdサービスとして起動
sudo systemctl start chatapp.service
```

---

## Nginx リバースプロキシの設定

### Nginx のインストール

```bash
# Ubuntu
sudo apt install nginx

# CentOS
sudo yum install nginx
```

### 設定ファイル

`/etc/nginx/sites-available/chatapp` を作成:

```nginx
# HTTP から HTTPS へのリダイレクト
server {
    listen 80;
    server_name yourapp.com www.yourapp.com;
    return 301 https://$server_name$request_uri;
}

# HTTPS 設定
server {
    listen 443 ssl http2;
    server_name yourapp.com www.yourapp.com;

    # SSL証明書
    ssl_certificate /etc/letsencrypt/live/yourapp.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/yourapp.com/privkey.pem;

    # SSL設定
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers HIGH:!aNULL:!MD5;
    ssl_prefer_server_ciphers on;

    # ログ
    access_log /var/log/nginx/chatapp_access.log;
    error_log /var/log/nginx/chatapp_error.log;

    # REST API プロキシ
    location /api/ {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    # WebSocket プロキシ
    location /ws/ {
        proxy_pass http://localhost:8080;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_read_timeout 86400;
    }

    # アクチュエータエンドポイント(内部アクセスのみ)
    location /actuator/ {
        allow 127.0.0.1;
        deny all;
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
    }

    # フロントエンド(オプション)
    location / {
        root /var/www/chatapp/frontend;
        try_files $uri $uri/ /index.html;
    }
}
```

設定を有効化:

```bash
# シンボリックリンクを作成
sudo ln -s /etc/nginx/sites-available/chatapp /etc/nginx/sites-enabled/

# 設定テスト
sudo nginx -t

# Nginxを再起動
sudo systemctl restart nginx
```

---

## SSL/TLS の設定

### Let's Encrypt による SSL 証明書取得

```bash
# Certbot のインストール
sudo apt install certbot python3-certbot-nginx

# 証明書の取得
sudo certbot --nginx -d yourapp.com -d www.yourapp.com

# 自動更新の設定
sudo systemctl enable certbot.timer
sudo systemctl start certbot.timer

# テスト更新
sudo certbot renew --dry-run
```

---

## モニタリングとログ

### アプリケーションログ

```bash
# systemd ログの確認
sudo journalctl -u chatapp.service -f

# アプリケーションログファイル
tail -f /opt/chatapp/logs/chatapp.log
```

### Spring Boot Actuator エンドポイント

```bash
# ヘルスチェック
curl http://localhost:8080/actuator/health

# メトリクス
curl http://localhost:8080/actuator/metrics

# アプリケーション情報
curl http://localhost:8080/actuator/info
```

### Prometheusとの統合(オプション)

`pom.xml` に追加:

```xml
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-registry-prometheus</artifactId>
</dependency>
```

`application-prod.properties` に追加:

```properties
management.endpoints.web.exposure.include=health,info,metrics,prometheus
management.metrics.export.prometheus.enabled=true
```

---

## バックアップ戦略

### データベースバックアップ

#### 日次バックアップスクリプト

`/opt/scripts/backup-db.sh` を作成:

```bash
#!/bin/bash

# 設定
DB_NAME="chatapp_db"
DB_USER="chatapp_user"
DB_PASS="your_password"
BACKUP_DIR="/var/backups/chatapp/db"
DATE=$(date +%Y%m%d_%H%M%S)
RETENTION_DAYS=7

# バックアップディレクトリ作成
mkdir -p $BACKUP_DIR

# バックアップ実行
mysqldump -u $DB_USER -p$DB_PASS $DB_NAME | gzip > $BACKUP_DIR/backup_$DATE.sql.gz

# 古いバックアップを削除
find $BACKUP_DIR -name "backup_*.sql.gz" -mtime +$RETENTION_DAYS -delete

echo "Backup completed: backup_$DATE.sql.gz"
```

実行権限を付与:

```bash
chmod +x /opt/scripts/backup-db.sh
```

#### Cron での自動実行

```bash
# crontab を編集
crontab -e

# 毎日午前3時にバックアップ
0 3 * * * /opt/scripts/backup-db.sh >> /var/log/chatapp-backup.log 2>&1
```

### アプリケーションファイルのバックアップ

```bash
# JARファイルとログのバックアップ
tar -czf /var/backups/chatapp/app_$(date +%Y%m%d).tar.gz /opt/chatapp
```

---

## トラブルシューティング

### アプリケーションが起動しない

```bash
# ログを確認
sudo journalctl -u chatapp.service -n 100

# JARファイルの権限確認
ls -l /opt/chatapp/chatapp.jar

# ポートの使用状況確認
sudo netstat -tlnp | grep 8080
```

### データベース接続エラー

```bash
# MySQL が起動しているか確認
sudo systemctl status mysql

# 接続テスト
mysql -u chatapp_user -p -h localhost chatapp_db

# ファイアウォール確認
sudo ufw status
```

### WebSocket 接続エラー

- Nginx の設定で `proxy_http_version 1.1` と `Upgrade` ヘッダーが正しく設定されているか確認
- ファイアウォールが WebSocket トラフィックを許可しているか確認

---

## セキュリティチェックリスト

- [ ] JWT シークレットキーを強力なランダム値に変更
- [ ] データベースパスワードを強力な値に変更
- [ ] SSH ログインにキーベース認証を使用
- [ ] ファイアウォールで必要なポートのみ開放 (80, 443)
- [ ] SSL/TLS 証明書を取得して HTTPS を有効化
- [ ] 定期的なセキュリティアップデートの適用
- [ ] アプリケーションログの定期的な監視
- [ ] データベースバックアップの自動化
- [ ] 非root ユーザーでアプリケーションを実行
- [ ] CORS 設定で許可するオリジンを制限

---

## パフォーマンス最適化

### JVM チューニング

```bash
java -Xms512m -Xmx2g \
     -XX:+UseG1GC \
     -XX:MaxGCPauseMillis=200 \
     -XX:+HeapDumpOnOutOfMemoryError \
     -XX:HeapDumpPath=/var/log/chatapp/ \
     -jar /opt/chatapp/chatapp.jar
```

### データベースインデックスの最適化

```sql
-- スロークエリログの有効化
SET GLOBAL slow_query_log = 'ON';
SET GLOBAL long_query_time = 1;

-- インデックスの確認
SHOW INDEX FROM chat_messages;
```

### 接続プール設定

`application-prod.properties`:

```properties
spring.datasource.hikari.maximum-pool-size=20
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.connection-timeout=30000
```

---

## スケーリング戦略

### 水平スケーリング

- ロードバランサー(Nginx/HAProxy)の導入
- 複数のアプリケーションインスタンスを起動
- Redis でセッション共有を実装

### 垂直スケーリング

- サーバーのCPU/メモリを増強
- JVM ヒープサイズを調整
- データベース接続プールを増加

---

## サポートとメンテナンス

### 定期メンテナンスタスク

- **毎日**: ログファイルの確認、バックアップの検証
- **毎週**: セキュリティアップデートの適用
- **毎月**: バックアップからのリストアテスト、パフォーマンスレビュー

### 監視すべきメトリクス

- CPU 使用率
- メモリ使用率
- ディスク使用率
- データベース接続数
- HTTP レスポンスタイム
- エラーレート

---

## 参考資料

- [Spring Boot Production-ready Features](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html)
- [MySQL 8.0 Documentation](https://dev.mysql.com/doc/refman/8.0/en/)
- [Nginx WebSocket Proxying](https://nginx.org/en/docs/http/websocket.html)
- [Let's Encrypt Documentation](https://letsencrypt.org/docs/)

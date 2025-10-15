# Chat Application - MySQL Setup Guide

## 前提条件
- MySQL Server 8.0以上がインストールされていること
- MySQL Serverが起動していること

## セットアップ手順

### 1. MySQLサーバーの起動確認
```cmd
# Windows Services から MySQL サービスを開始
# または
net start MySQL80
```

### 2. データベースとユーザーの作成

#### 方法A: SQLスクリプトを使用（推奨）
```cmd
cd backend
mysql -u root -p < setup-mysql.sql
```
rootパスワードを入力してください。

#### 方法B: 手動でMySQLにログインして実行
```cmd
mysql -u root -p
```

MySQLプロンプトで以下を実行:
```sql
CREATE DATABASE IF NOT EXISTS chatapp CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER IF NOT EXISTS 'chatapp_user'@'localhost' IDENTIFIED BY 'chatapp_password';
GRANT ALL PRIVILEGES ON chatapp.* TO 'chatapp_user'@'localhost';
FLUSH PRIVILEGES;
EXIT;
```

### 3. 接続確認
```cmd
mysql -u chatapp_user -p
# パスワード: chatapp_password
```

MySQLプロンプトで:
```sql
USE chatapp;
SHOW TABLES;
EXIT;
```

### 4. アプリケーション起動
```cmd
cd backend
.\mvnw.cmd spring-boot:run
```

## トラブルシューティング

### MySQLに接続できない
- MySQLサービスが起動しているか確認
- ファイアウォールがポート3306をブロックしていないか確認
- ユーザー名・パスワードが正しいか確認

### ユーザー作成エラー
- rootユーザーで実行しているか確認
- 既存のユーザーを削除してから再作成:
  ```sql
  DROP USER IF EXISTS 'chatapp_user'@'localhost';
  ```

### 接続エラー: Access denied
```sql
# rootでログインして権限を再付与
GRANT ALL PRIVILEGES ON chatapp.* TO 'chatapp_user'@'localhost';
FLUSH PRIVILEGES;
```

## データベース設定変更

`backend/src/main/resources/application.properties`で設定を変更できます:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/chatapp?useSSL=false&serverTimezone=Asia/Tokyo
spring.datasource.username=chatapp_user
spring.datasource.password=chatapp_password
```

## スキーマの自動更新

アプリケーション起動時、Hibernateが自動的にテーブルを作成・更新します:
- `spring.jpa.hibernate.ddl-auto=update`

本番環境では`validate`に変更することを推奨します。

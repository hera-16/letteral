-- MySQLデータベースとユーザーのセットアップ
-- 管理者権限でMySQLにログインして実行してください: mysql -u root -p < setup-mysql.sql

-- データベースの作成（既に存在する場合はスキップ）
CREATE DATABASE IF NOT EXISTS chatapp CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- ユーザーの作成と権限付与
CREATE USER IF NOT EXISTS 'chatapp_user'@'localhost' IDENTIFIED BY 'chatapp_password';
GRANT ALL PRIVILEGES ON chatapp.* TO 'chatapp_user'@'localhost';
FLUSH PRIVILEGES;

-- 確認
SELECT User, Host FROM mysql.user WHERE User = 'chatapp_user';
SHOW DATABASES LIKE 'chatapp';

-- 使用方法:
-- 1. MySQLサーバーを起動
-- 2. 管理者として実行: mysql -u root -p < setup-mysql.sql
-- 3. パスワードを入力
-- 4. データベースとユーザーが作成されます

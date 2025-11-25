# ローカルDB完全リセット手順

このガイドは、ローカル開発環境でデータベースを完全にリセットし、Flywayマイグレーションを最初から実行する手順をまとめたものです。

## 前提条件

- MySQL 8.0がローカルで起動している（ポート: 3308）
- MySQLクライアント（`mysql`コマンド）が利用可能
- プロジェクトのルートディレクトリで作業している

## いつこの手順が必要か？

以下のような状況でDBを完全リセットする必要があります：

1. **Flyway checksum mismatch エラー**が発生した時
   - マイグレーションファイルを修正したが、DBに古いchecksumが残っている
2. **テーブル構造が壊れた**時
3. **開発中にテストデータが不要になった**時
4. **新しいマイグレーションを追加してクリーンな状態で確認**したい時

---

## 手順1: 現在のアプリケーションを停止

実行中のSpring Bootアプリケーションをすべて停止してください。

```bash
# 実行中のJavaプロセスを確認
tasklist | findstr java

# 必要に応じてプロセスをキル
taskkill /F /PID <プロセスID>
```

---

## 手順2: データベースを完全削除して再作成

MySQLに接続してデータベースを削除・再作成します。

### オプション A: PowerShell から実行（推奨）

```powershell
# MySQLに接続してDBを削除・再作成
mysql -u chatapp_user -pchatapp_password -h localhost -P 3308 -e "DROP DATABASE IF EXISTS chatapp_db; CREATE DATABASE chatapp_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
```

### オプション B: MySQL CLIで対話的に実行

```bash
# MySQLに接続
mysql -u chatapp_user -pchatapp_password -h localhost -P 3308

# 以下をMySQLプロンプトで実行
DROP DATABASE IF EXISTS chatapp_db;
CREATE DATABASE chatapp_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE chatapp_db;
SHOW TABLES;  -- 空であることを確認
EXIT;
```

---

## 手順3: Mavenのクリーンビルド（オプションだが推奨）

キャッシュされたクラスファイルをクリアします。

```bash
cd backend
..\mvnw.cmd clean
```

---

## 手順4: Spring Bootアプリケーションを起動

Flywayが自動的にV1〜V22（またはそれ以降）のマイグレーションを順番に実行します。

```bash
# backendディレクトリで実行
..\mvnw.cmd spring-boot:run
```

### 期待される出力例

```
INFO  FlywayExecutor - Flyway Community Edition 9.x by Redgate
INFO  FlywayExecutor - Database: jdbc:mysql://localhost:3308/chatapp_db (MySQL 8.0)
INFO  FlywayExecutor - Successfully validated 22 migrations (execution time 00:00.045s)
INFO  FlywayExecutor - Creating Schema History table `chatapp_db`.`flyway_schema_history` ...
INFO  FlywayExecutor - Current version of schema `chatapp_db`: << Empty Schema >>
INFO  FlywayExecutor - Migrating schema `chatapp_db` to version "1 - Initial Schema"
INFO  FlywayExecutor - Migrating schema `chatapp_db` to version "2 - Sample Data"
...
INFO  FlywayExecutor - Migrating schema `chatapp_db` to version "22 - Add Performance Indexes"
INFO  FlywayExecutor - Successfully applied 22 migrations to schema `chatapp_db` (execution time 00:05.234s)
```

---

## 手順5: マイグレーションの成功を確認

アプリケーションが正常に起動したら、ブラウザで以下にアクセスします。

```
http://localhost:8080/api/health
```

または、MySQLで直接確認：

```bash
mysql -u chatapp_user -pchatapp_password -h localhost -P 3308 -e "USE chatapp_db; SELECT version, description, success FROM flyway_schema_history ORDER BY installed_rank;"
```

すべてのマイグレーションが`success = 1`になっていれば成功です。

---

## トラブルシューティング

### 問題1: `Access denied for user 'chatapp_user'`

**原因**: ユーザーが存在しないか、パスワードが間違っている

**解決策**:
```sql
-- rootユーザーでMySQLに接続
mysql -u root -p -h localhost -P 3308

-- ユーザーを作成（すでに存在する場合は削除してから再作成）
DROP USER IF EXISTS 'chatapp_user'@'%';
CREATE USER 'chatapp_user'@'%' IDENTIFIED BY 'chatapp_password';
GRANT ALL PRIVILEGES ON chatapp_db.* TO 'chatapp_user'@'%';
FLUSH PRIVILEGES;
```

### 問題2: Flyway checksum mismatch エラーが再発

**原因**: マイグレーションファイルを修正したが、まだDBに古いchecksumが残っている

**解決策**: 手順2に戻ってDBを完全削除・再作成してください

### 問題3: `Table 'xxx' already exists` 警告

**原因**: `CREATE TABLE IF NOT EXISTS`を使っているので、警告は無害です

**対処**: 警告は無視してOKです。エラーではないため、アプリは正常起動します

---

## 本番環境では絶対にやらないこと

⚠️ **警告**: 上記の手順は**開発環境専用**です。本番環境では以下の理由から実行しないでください：

- データが完全に失われます
- ダウンタイムが発生します
- 本番では「flyway repair」や「データ移行戦略」を使う必要があります

本番環境でのFlyway運用については、[FLYWAY_PRODUCTION_GUIDE.md](./FLYWAY_PRODUCTION_GUIDE.md)を参照してください。

---

## まとめ

この手順により、ローカル環境を常にクリーンな状態に保つことができます。マイグレーションファイルの修正後は、必ずこの手順でDBをリセットしてから動作確認してください。

```bash
# 1行コマンドでDB削除・再作成・起動
mysql -u chatapp_user -pchatapp_password -h localhost -P 3308 -e "DROP DATABASE IF EXISTS chatapp_db; CREATE DATABASE chatapp_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;" && cd backend && ..\mvnw.cmd spring-boot:run
```

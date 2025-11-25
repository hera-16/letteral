# Flyway 本番環境運用ガイド

このドキュメントは、AWS本番環境でFlywayを安全に運用するためのベストプラクティスをまとめたものです。

---

## 🎯 本番環境での基本方針

### 1. **マイグレーションファイルは絶対に変更しない**

**理由**: 一度本番環境に適用されたマイグレーションファイルのchecksumが変わると、アプリケーションの起動が失敗します。

**ルール**:
- すでにデプロイ済みのマイグレーション（例: V1〜V22）は**絶対に編集しない**
- 修正が必要な場合は、**新しいマイグレーション（V23以降）**を追加する

**悪い例**:
```sql
-- V8__Create_OKR_Tables.sql を直接編集（NG！）
ALTER TABLE okr_update_history MODIFY updated_by BIGINT NULL;
```

**良い例**:
```sql
-- V23__Fix_OKR_Update_History_Column.sql として新規作成（OK）
ALTER TABLE okr_update_history MODIFY updated_by BIGINT NULL COMMENT '更新者';
```

---

### 2. **本番適用前に必ずステージング環境でテストする**

**手順**:
1. ステージング環境に新しいマイグレーションをデプロイ
2. ログでマイグレーションの成功を確認
3. アプリケーションの動作確認（E2Eテスト）
4. 問題がなければ本番環境にデプロイ

**確認コマンド**:
```bash
# RDS（本番DB）でマイグレーション履歴を確認
mysql -h <RDSエンドポイント> -u <ユーザー> -p<パスワード> -e \
  "SELECT version, description, installed_on, success FROM flyway_schema_history ORDER BY installed_rank DESC LIMIT 10;"
```

---

### 3. **バックアップを必ず取得してからデプロイする**

**RDSの自動バックアップ設定**:
- 自動バックアップ保持期間: 7日以上を推奨
- スナップショット取得: デプロイ前に手動で取得

**AWS CLIでのスナップショット取得**:
```bash
aws rds create-db-snapshot \
  --db-instance-identifier chatapp-production-db \
  --db-snapshot-identifier chatapp-snapshot-$(date +%Y%m%d-%H%M%S)
```

**復元が必要になった場合**:
1. スナップショットから新しいDBインスタンスを作成
2. アプリケーションの接続先を変更
3. ダウンタイム: 約10〜15分

---

### 4. **ブルーグリーンデプロイメントでダウンタイムを最小化**

**戦略**:
- 新バージョン（Green環境）にマイグレーションを適用
- 動作確認後、ロードバランサーのターゲットを切り替え
- 問題があれば即座にBlue環境にロールバック

**AWS環境での実装例**:
```bash
# ECSサービスの新しいタスク定義をデプロイ
aws ecs update-service \
  --cluster chatapp-production \
  --service chatapp-backend \
  --task-definition chatapp-backend:LATEST \
  --desired-count 2
```

---

### 5. **Flyway repair を使ったトラブルシューティング**

**いつ使うか？**:
- マイグレーションが途中で失敗し、`flyway_schema_history`の状態が不整合になった時
- checksumエラーが発生したが、データベース構造自体には問題がない時

**注意点**:
- `flyway repair`は`flyway_schema_history`テーブルを修正するだけで、実際のテーブル構造は変更しない
- 本番環境で実行する前に、必ずステージング環境でテスト

**使用例**:
```bash
# Flywayのrepairコマンドを実行（Spring Bootアプリ停止中に実施）
# オプション1: Flyway CLIを使用
flyway -url=jdbc:mysql://<RDS_ENDPOINT>:3306/chatapp_db \
       -user=<DB_USER> \
       -password=<DB_PASSWORD> \
       repair

# オプション2: application.propertiesに一時的に追加
spring.flyway.repair=true  # 起動時に自動でrepair実行（使用後は削除すること）
```

---

## 🚨 よくある本番トラブルと対処法

### トラブル1: マイグレーションが失敗してアプリが起動しない

**症状**:
```
Migration V23__Add_New_Feature.sql failed
SQL State  : 42S01
Error Code : 1050
Message    : Table 'new_feature' already exists
```

**原因**: 手動でテーブルを作成した、または過去のデプロイで部分的に適用された

**対処法**:
```sql
-- 該当マイグレーションの状態を確認
SELECT * FROM flyway_schema_history WHERE version = '23';

-- 失敗したマイグレーションの記録を削除（慎重に！）
DELETE FROM flyway_schema_history WHERE version = '23' AND success = 0;

-- テーブルが既に存在する場合は、マイグレーションをスキップする新しいファイルを作成
-- V24__Skip_New_Feature_If_Exists.sql
CREATE TABLE IF NOT EXISTS new_feature (...);
```

---

### トラブル2: checksum mismatch エラーが発生

**症状**:
```
Migration checksum mismatch for migration version 8
-> Applied to database : 123456789
-> Resolved locally : 987654321
```

**原因**: マイグレーションファイルが変更された（開発環境で修正してコミット・デプロイしてしまった）

**対処法**:

**方法A: flyway repair（推奨）**
```bash
# checksumを現在のファイルに合わせて更新
flyway -url=jdbc:mysql://<RDS_ENDPOINT>:3306/chatapp_db \
       -user=<DB_USER> \
       -password=<DB_PASSWORD> \
       repair
```

**方法B: 手動でchecksumを更新**
```sql
-- 現在のchecksumを確認
SELECT version, checksum FROM flyway_schema_history WHERE version = '8';

-- checksumを手動で更新（非推奨: 緊急時のみ）
UPDATE flyway_schema_history
SET checksum = <新しいchecksum>
WHERE version = '8';
```

---

### トラブル3: ロールバックが必要になった

**Flywayの制限**: Flywayは基本的に「ロールバック機能」を持たない（Flyway Teamsでは提供）

**代替手段**:
1. **新しいマイグレーションで戻す**（推奨）
   ```sql
   -- V24__Rollback_Feature_X.sql
   DROP TABLE IF EXISTS feature_x;
   ALTER TABLE users DROP COLUMN new_field;
   ```

2. **DBスナップショットから復元**（緊急時）
   - RDSスナップショットから過去の状態に復元
   - ダウンタイム: 10〜30分

3. **Blue-Greenデプロイメント**
   - 旧バージョンの環境にルーティングを戻す

---

## ✅ デプロイ前チェックリスト

本番環境にデプロイする前に、以下を必ず確認してください。

- [ ] 新しいマイグレーションファイルがステージング環境でテスト済み
- [ ] マイグレーションのSQLが冪等性を持つ（`IF NOT EXISTS`等を使用）
- [ ] RDSのバックアップが取得されている
- [ ] ダウンタイムが許容範囲内（5分以内が理想）
- [ ] ロールバック計画が用意されている
- [ ] モニタリングアラートが設定されている（CloudWatch等）
- [ ] デプロイ後の動作確認手順が明確

---

## 📊 モニタリングとアラート

### CloudWatch Logs で Flyway ログを監視

**監視すべきキーワード**:
- `Migration failed`
- `checksum mismatch`
- `SQL State`
- `Error Code`

**CloudWatch Logs Insights クエリ例**:
```
fields @timestamp, @message
| filter @message like /Flyway/
| filter @message like /error/ or @message like /failed/
| sort @timestamp desc
| limit 50
```

### RDS パフォーマンスモニタリング

- **CPU使用率**: マイグレーション中は一時的に上昇（80%以下が理想）
- **接続数**: マイグレーション中はアプリの接続が増加
- **ストレージ**: マイグレーションでテーブルが増えるため、容量を監視

---

## 🔧 本番環境の推奨設定

### application-production.properties

```properties
# Flyway本番設定
spring.flyway.enabled=true
spring.flyway.baseline-on-migrate=false
spring.flyway.out-of-order=false  # 本番では無効化
spring.flyway.validate-on-migrate=true  # 起動時にバリデーション
spring.flyway.clean-disabled=true  # 誤ってcleanコマンドを実行しないように

# JPA設定
spring.jpa.hibernate.ddl-auto=validate  # Hibernateによる自動スキーマ変更を無効化
spring.jpa.show-sql=false  # 本番ではSQLログを無効化

# データソース（環境変数から取得）
spring.datasource.url=${SPRING_DATASOURCE_URL}
spring.datasource.username=${SPRING_DATASOURCE_USERNAME}
spring.datasource.password=${SPRING_DATASOURCE_PASSWORD}
```

---

## 📚 参考リンク

- [Flyway公式ドキュメント](https://flywaydb.org/documentation/)
- [AWS RDS自動バックアップ](https://docs.aws.amazon.com/AmazonRDS/latest/UserGuide/USER_WorkingWithAutomatedBackups.html)
- [Spring Boot Flyway統合](https://docs.spring.io/spring-boot/docs/current/reference/html/howto.html#howto.data-initialization.migration-tool.flyway)

---

## まとめ: 本番運用の5つの鉄則

1. **マイグレーションファイルは不変** - デプロイ後は絶対に変更しない
2. **バックアップを忘れずに** - RDSスナップショットを必ず取得
3. **ステージング環境でテスト** - 本番に適用する前に必ず検証
4. **ロールバック計画を用意** - 失敗時の復旧手順を事前に準備
5. **モニタリングは必須** - CloudWatchでログを監視し、異常を早期検知

これらのルールを守ることで、本番環境でのFlywayマイグレーションを安全に運用できます。

# Production Deployment Checklist

このチェックリストは `backend` プロジェクトを本番環境へデプロイする際に確認すべき設定項目をまとめたものです。CI/CD パイプラインや運用 runbook から参照できるよう、内容を最新の状態に保ってください。

## 1. プロファイルと環境変数

- [ ] `SPRING_PROFILES_ACTIVE=prod` を設定し、開発用設定と切り替えられるようにする。
- [ ] `application-prod.properties`（または `application.yml`）を用意し、本番専用の値を管理する。
- [ ] Java の `-Xms` / `-Xmx` 等ヒープサイズを用途に応じて調整する。
- [ ] CI/CD から `.env` ではなくシークレットストア（GitHub Actions Secret, Azure KeyVault など）経由で値を注入する。

## 2. データベース

- [ ] 本番用 MySQL / PostgreSQL など永続 DB の接続情報を環境変数化する。
  - `SPRING_DATASOURCE_URL`
  - `SPRING_DATASOURCE_USERNAME`
  - `SPRING_DATASOURCE_PASSWORD`
- [ ] コネクションプール（`spring.datasource.hikari.*`）のサイズやタイムアウトを、本番の負荷想定に合わせて調整する。
- [ ] `spring.jpa.hibernate.ddl-auto=validate` もしくは `none` に変更し、意図しないスキーマ変更を防止する。
- [ ] Flyway / Liquibase 等でスキーマ管理する場合は、マイグレーションの手順を CI/CD に組み込む。

## 3. セキュリティ / JWT

- [ ] `JWT_SECRET` を 256bit 以上のランダム文字列で用意し、`jwt.secret` にマップする。
- [ ] `JWT_EXPIRATION` を環境変数化し、必要に応じて有効期限を調整する。
- [ ] HTTPS リバースプロキシ（Nginx / ALB 等）の背後で動かす場合は、`server.forward-headers-strategy=framework` を設定してヘッダーを正しくリゾルブする。
- [ ] Spring Security の CORS 設定が最新のオリジンを許可しているかを確認する。

## 4. CORS / API エンドポイント

- [ ] `APP_CORS_ALLOWED_ORIGINS` を設定し、複数オリジンの場合はカンマ区切りで管理する。
- [ ] REST / WebSocket 両方で想定外の HTTP メソッドが許可されていないか確認する。
- [ ] WebSocket エンドポイントへ CSRF 対策やオリジン制限を適用する場合は、SecurityConfig へ追加する。

## 5. ロギング / 監視

- [ ] `logging.level.root=INFO` など本番向けログレベルを設定し、個人情報を含むログが出力されないようにする。
- [ ] 構造化ログ（JSON 形式）や外部監視サービス（CloudWatch, Datadog など）との連携が必要か検討する。
- [ ] 健康チェックエンドポイント（`/actuator/health` 等）を有効化し、負荷分散装置からの監視に対応する。

## 6. CI/CD

- [ ] `./mvnw clean test` を CI の最小ゲートとし、将来的に統合テストを追加する。
- [ ] Build Artifacts（Fat Jar / Docker Image）に Git のコミットハッシュ等メタ情報を含めておく。
- [ ] 失敗時のロールバック手順を決め、手動デプロイのチェックリストも準備する。

## 7. その他

- [ ] バックアップポリシー（DB スナップショット、ログ保存期間など）を定義する。
- [ ] 外部サービス（メール/SMS 通知等）を利用している場合、API キーの更新手順と期限を管理する。
- [ ] SLA / SLO 指標を決め、アラート閾値をモニタリング設定に反映させる。

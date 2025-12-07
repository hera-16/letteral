# CI/CD セットアップガイド - Letteral

## 📋 概要

Letteralプロジェクトでは、GitHub Actionsを使用した自動テストとCI/CDパイプラインを構築しています。

---

## 🚀 GitHub Actions ワークフロー

### 1. メインテストワークフロー

**ファイル:** [`.github/workflows/build-and-test.yml`](../.github/workflows/build-and-test.yml)

**トリガー:**
- `main`または`develop`ブランチへのプッシュ
- `main`または`develop`ブランチへのプルリクエスト

**ジョブ:**

#### Backend Tests
- **環境:** Ubuntu Latest + JDK 17
- **実行内容:**
  - Maven依存関係のキャッシュ
  - バックエンドテストの実行
  - テストレポートの生成
  - テスト結果の公開

```yaml
- name: Run backend tests
  run: |
    cd backend
    chmod +x ../mvnw
    ../mvnw clean test
  env:
    SPRING_PROFILES_ACTIVE: test
```

#### Frontend Tests
- **環境:** Ubuntu Latest + Node.js 18
- **実行内容:**
  - npm依存関係のインストール
  - フロントエンドテストの実行（カバレッジ付き）
  - Codecovへのカバレッジアップロード

```yaml
- name: Run tests
  run: npm test -- --passWithNoTests --coverage
```

#### Test Summary
- 全テスト結果のサマリーを表示
- いずれかのテストが失敗した場合、ワークフロー全体を失敗させる

---

## 📊 テストレポート

### バックエンドテストレポート

テスト実行後、以下の場所にレポートが生成されます：

```
backend/target/surefire-reports/
├── TEST-*.xml              # JUnit XMLレポート
└── surefire-report.html    # HTML形式のレポート
```

GitHub Actionsでは、`dorny/test-reporter`を使用してテスト結果を可視化します。

### フロントエンドカバレッジレポート

カバレッジレポートは以下に生成されます：

```
coverage/
├── lcov.info              # LCOV形式のカバレッジ
└── lcov-report/           # HTML形式のレポート
    └── index.html
```

Codecovに自動アップロードされ、PRごとにカバレッジの変化を確認できます。

---

## 🔧 プルリクエストワークフロー

**ファイル:** [`.github/workflows/pr-tests.yml`](../.github/workflows/pr-tests.yml)

プルリクエスト作成時に、より軽量なテストセットを実行します：

**実行内容:**
- バックエンドユニットテスト（Serviceレイヤーのみ）
- フロントエンドテスト
- テスト結果をPRにコメント

**メリット:**
- 高速なフィードバック（フルテストより早い）
- マージ前の基本的な品質チェック
- PRレビューアーへの可視性向上

---

## 📈 ステータスバッジ

README.mdにテストステータスバッジを追加しています：

```markdown
[![Tests](https://github.com/hera-16/letteral/actions/workflows/build-and-test.yml/badge.svg)](https://github.com/hera-16/letteral/actions/workflows/build-and-test.yml)
```

このバッジは、最新のテスト実行結果を表示します：
- ✅ 緑色 = すべてのテスト成功
- ❌ 赤色 = テスト失敗

---

## 🛠️ テスト設定ファイル

### バックエンドテスト設定

**ファイル:** [`backend/src/test/resources/application-test.properties`](../backend/src/test/resources/application-test.properties)

```properties
# H2インメモリデータベースを使用
spring.datasource.url=jdbc:h2:mem:testdb;MODE=MySQL
spring.datasource.driver-class-name=org.h2.Driver

# JPA設定
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=create-drop

# Flywayは無効化（create-dropを使用）
spring.flyway.enabled=false

# JWTテスト用シークレット
jwt.secret=test_jwt_secret_key_for_testing_purposes_minimum_256_bits_required_here
```

### フロントエンドテスト設定

**ファイル:** [`jest.config.js`](../jest.config.js)

```javascript
const nextJest = require('next/jest')

const createJestConfig = nextJest({
  dir: './',
})

const customJestConfig = {
  setupFilesAfterEnv: ['<rootDir>/jest.setup.js'],
  testEnvironment: 'jest-environment-jsdom',
  moduleNameMapper: {
    '^@/(.*)$': '<rootDir>/src/$1',
  },
}

module.exports = createJestConfig(customJestConfig)
```

---

## 🔄 CI/CDパイプライン フロー

### プッシュ時のフロー

```
┌─────────────────┐
│  git push       │
│  (main/develop) │
└────────┬────────┘
         │
         ▼
┌─────────────────────────────┐
│  GitHub Actions トリガー    │
└────────┬────────────────────┘
         │
         ├──────────────┬──────────────┐
         ▼              ▼              ▼
┌──────────────┐ ┌──────────────┐ ┌──────────────┐
│ Backend Tests│ │Frontend Tests│ │ Integration  │
│   (JUnit)    │ │    (Jest)    │ │    Tests     │
└──────┬───────┘ └──────┬───────┘ └──────┬───────┘
       │                │                │
       └────────┬───────┴────────────────┘
                ▼
       ┌──────────────────┐
       │  Test Summary    │
       │  ✅ or ❌        │
       └──────────────────┘
```

### プルリクエスト時のフロー

```
┌─────────────────┐
│  Pull Request   │
│    Created      │
└────────┬────────┘
         │
         ▼
┌─────────────────────────────┐
│  Quick Tests トリガー       │
└────────┬────────────────────┘
         │
         ├──────────────┬
         ▼              ▼
┌──────────────┐ ┌──────────────┐
│ Unit Tests   │ │Frontend Tests│
│  (Service層) │ │    (Jest)    │
└──────┬───────┘ └──────┬───────┘
       │                │
       └────────┬───────┘
                ▼
       ┌──────────────────┐
       │  PR Comment      │
       │  (Test Results)  │
       └──────────────────┘
```

---

## 🚨 トラブルシューティング

### テストがGitHub Actionsで失敗する

#### 1. ローカルでは成功するがCIで失敗する

**原因:** 環境依存の設定や、タイムゾーン、ファイルパスの違い

**解決策:**
```bash
# ローカルでテストプロファイルを使用して実行
cd backend
./mvnw test -Dspring.profiles.active=test
```

#### 2. 依存関係のインストールエラー

**原因:** キャッシュの問題やバージョン不整合

**解決策:**
- `pom.xml`や`package.json`を確認
- キャッシュをクリアして再実行

```yaml
# GitHub Actionsでキャッシュをクリア
- name: Clear cache
  run: |
    rm -rf ~/.m2/repository
    rm -rf node_modules
```

#### 3. タイムアウトエラー

**原因:** テストの実行時間が長すぎる

**解決策:**
```yaml
# タイムアウトを延長
- name: Run tests
  timeout-minutes: 20  # デフォルト: 6分
  run: ./mvnw test
```

### データベース関連のエラー

#### H2データベースの初期化エラー

**確認ポイント:**
1. `application-test.properties`の設定
2. Flywayが無効化されているか
3. `create-drop`設定が正しいか

```properties
# 正しい設定
spring.jpa.hibernate.ddl-auto=create-drop
spring.flyway.enabled=false
```

---

## 📝 ベストプラクティス

### 1. テストは必ず成功させてからマージ

```bash
# マージ前にローカルで全テストを実行
cd backend && ../mvnw clean test
npm test
```

### 2. カバレッジを維持する

目標カバレッジ:
- Service層: 80%以上
- Controller層: 70%以上
- コンポーネント: 60%以上

### 3. テストを並列実行する

```yaml
# GitHub Actionsで並列実行
jobs:
  backend-tests:
    # ...
  frontend-tests:
    # ...
  # 上記2つは並列実行される
```

### 4. 失敗時の通知設定

GitHub Actionsの設定で、テスト失敗時にSlackやメールで通知を受け取れます：

```yaml
- name: Notify on failure
  if: failure()
  uses: 8398a7/action-slack@v3
  with:
    status: ${{ job.status }}
    webhook_url: ${{ secrets.SLACK_WEBHOOK }}
```

---

## 🔐 シークレット管理

GitHub Actionsで使用するシークレットは、リポジトリ設定で管理します：

**設定方法:**
1. GitHub リポジトリ → Settings → Secrets and variables → Actions
2. 「New repository secret」をクリック
3. シークレットを追加

**推奨シークレット:**
- `CODECOV_TOKEN` - Codecovアップロード用
- `SLACK_WEBHOOK` - 通知用（オプション）

---

## 📚 関連ドキュメント

- [テストガイド](./TESTING_GUIDE.md) - テストの書き方と実行方法
- [コントリビューションガイド](../README.md#コントリビューション) - 開発フロー

---

## ✅ チェックリスト

新しい機能を追加する際のチェックリスト：

- [ ] ユニットテストを追加
- [ ] 統合テストを追加（必要に応じて）
- [ ] ローカルですべてのテストが成功することを確認
- [ ] PRを作成
- [ ] GitHub Actionsのテストが成功することを確認
- [ ] カバレッジが低下していないことを確認
- [ ] レビュー後にマージ

---

## 🎯 まとめ

Letteralプロジェクトでは、GitHub Actionsを使用した包括的なCI/CDパイプラインを構築しています：

✅ **自動テスト** - プッシュ・PRごとに自動実行
✅ **テストレポート** - 視覚的なレポートで品質を確認
✅ **カバレッジ追跡** - Codecovで継続的に監視
✅ **高速フィードバック** - PRごとのクイックテスト

これにより、高品質なコードベースを維持しながら、安心して開発を進められます。

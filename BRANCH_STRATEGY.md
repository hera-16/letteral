# ブランチ戦略

このリポジトリは2つのブランチで構成されています。

## 📊 ブランチ構成

### `main` ブランチ (本番用) ⭐

**目的:** クリーンな履歴で公開用のコードを管理

**特徴:**
- ✅ 機密情報を含まない安全な履歴
- ✅ 本番環境対応済みのコード
- ✅ 環境変数ベースの設定
- ✅ ポートフォリオや公開に最適

**使用場面:**
- ポートフォリオとして公開
- 本番環境へのデプロイ
- 他の開発者への共有
- クリーンなコードベースの参照

**コミット履歴:**
```
3800a54 feat: Initial commit - Production-ready chat application
```

### `develop` ブランチ (開発履歴保存用) 🔧

**目的:** 完全な開発履歴を保存

**特徴:**
- 📝 すべての開発コミットを含む
- 🐛 バグ修正の履歴が残っている
- 🔍 機能開発のプロセスが追跡可能
- ⚠️ 古い機密情報が履歴に含まれる (mySecretKey...)

**使用場面:**
- 開発履歴の参照
- 過去のコード変更の追跡
- バグ調査やデバッグ
- 学習目的での振り返り

**注意事項:**
- ⚠️ このブランチには古いJWT秘密鍵やパスワードが履歴に含まれています
- ⚠️ これらは開発用のサンプル値であり、本番環境では使用していません
- ✅ 本番環境では環境変数で異なる秘密鍵を使用しています

## 🔄 ブランチの切り替え

### mainブランチに切り替え (推奨)

```bash
git checkout main
```

### developブランチに切り替え (開発履歴を見る場合)

```bash
git checkout develop
```

## 📝 使い分けガイド

| 目的 | 使用するブランチ |
|------|------------------|
| 本番環境へのデプロイ | `main` |
| ポートフォリオとして公開 | `main` |
| 他の開発者と共有 | `main` |
| 新機能の開発 | `main` (または新しいfeatureブランチ) |
| 開発履歴の参照 | `develop` |
| 過去のコード変更の確認 | `develop` |
| バグの原因調査 | `develop` |

## 🚀 新機能の開発方法

新機能を開発する場合は、`main`ブランチから新しいブランチを作成します:

```bash
# mainブランチに切り替え
git checkout main

# 新しい機能ブランチを作成
git checkout -b feature/new-feature

# 開発してコミット
git add .
git commit -m "feat: Add new feature"

# GitHubにプッシュ
git push -u origin feature/new-feature

# GitHubでPull Requestを作成してmainにマージ
```

## 🔒 セキュリティに関する注意事項

### mainブランチ (安全) ✅

- ✅ 機密情報は環境変数で管理
- ✅ Git履歴に秘密鍵やパスワードが含まれていない
- ✅ 公開しても安全
- ✅ 本番環境で使用可能

### developブランチ (注意が必要) ⚠️

- ⚠️ 開発用のサンプル秘密鍵が履歴に含まれる
  - `jwt.secret=mySecretKey123456789012345678901234567890`
  - `spring.datasource.password=chatapp_password`
- ✅ これらは本番環境では使用していない
- ✅ 本番環境は環境変数で異なる秘密鍵を使用
- 📝 開発履歴の参照用として保存

## 📚 関連ドキュメント

- [README.md](../README.md) - プロジェクト概要とセットアップ
- [backend/PRODUCTION_SETUP.md](../backend/PRODUCTION_SETUP.md) - 本番環境設定ガイド
- [docs/DEPLOYMENT.md](../docs/DEPLOYMENT.md) - デプロイメントガイド
- [GIT_HISTORY_CLEANUP.md](../GIT_HISTORY_CLEANUP.md) - Git履歴クリーンアップの経緯

## 🎯 推奨される使用方法

### ポートフォリオとして公開する場合

```bash
# mainブランチを使用
git checkout main

# GitHubでリポジトリを公開設定にする
# Settings → Danger Zone → Change repository visibility → Public
```

### 本番環境にデプロイする場合

```bash
# mainブランチを使用
git checkout main

# 最新のコードを取得
git pull origin main

# デプロイ
# (デプロイ方法は docs/DEPLOYMENT.md を参照)
```

### 開発履歴を振り返る場合

```bash
# developブランチに切り替え
git checkout develop

# コミット履歴を確認
git log --oneline --graph

# 特定のファイルの変更履歴を確認
git log -p -- path/to/file
```

## ❓ よくある質問

**Q: どのブランチをクローンすればいいですか?**

A: デフォルトの`main`ブランチをクローンしてください。これが最も安全でクリーンな状態です。

```bash
git clone https://github.com/hera-16/letteral.git
# 自動的に main ブランチがチェックアウトされます
```

**Q: developブランチの機密情報は大丈夫ですか?**

A: はい、問題ありません。含まれているのは開発用のサンプル値であり、本番環境では使用していません。本番環境では環境変数で異なる強力な秘密鍵を使用しています。

**Q: 新しい機能を追加する場合、どのブランチから始めればいいですか?**

A: `main`ブランチから新しいfeatureブランチを作成してください。`develop`ブランチは参照用です。

**Q: mainブランチとdevelopブランチをマージしますか?**

A: いいえ、マージしません。`main`はクリーンな履歴、`develop`は完全な開発履歴として、それぞれ独立して管理します。

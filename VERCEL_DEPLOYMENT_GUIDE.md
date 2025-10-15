# 📦 Vercelデプロイ手順

## ✅ 完了したこと
- ✅ `vercel.json` 設定ファイル作成
- ✅ 不要ファイル削除 (cleanup-sensitive-data.ps1, GIT_HISTORY_CLEANUP.md)
- ✅ README.md を魅力的に改善
- ✅ ビルドエラーの修正 (`ChatRoom.tsx`, `next.config.ts`)
- ✅ ビルド成功確認 (`npm run build` ✓)
- ✅ GitHubにプッシュ (commit: 8845305)

---

## 🚀 Vercelデプロイ手順

### 1️⃣ Vercelアカウント作成・ログイン

1. [Vercel公式サイト](https://vercel.com/) にアクセス
2. 「Sign Up」をクリック
3. **「Continue with GitHub」** を選択 (推奨)
4. GitHubアカウントで認証

### 2️⃣ プロジェクトをインポート

1. Vercelダッシュボードで **「Add New」** → **「Project」** をクリック
2. GitHubリポジトリ一覧から **`hera-16/letteral`** を選択
3. 「Import」をクリック

### 3️⃣ プロジェクト設定

#### Framework Preset
- **自動検出**: Next.js (そのままでOK)

#### Root Directory
- **ルートディレクトリ**: `.` (デフォルトのまま)

#### Build and Output Settings
- **Build Command**: `npm run build` (自動設定)
- **Output Directory**: `.next` (自動設定)
- **Install Command**: `npm install` (自動設定)

### 4️⃣ 環境変数の設定

**重要**: バックエンドAPIのURLを設定してください。

| Key | Value | 説明 |
|-----|-------|------|
| `NEXT_PUBLIC_API_URL` | `http://localhost:8080/api` | バックエンドAPI URL (後で変更可能) |

> **注意**: バックエンドをまだデプロイしていない場合は、ローカルの`http://localhost:8080/api`のままでも一旦デプロイできます。バックエンドをデプロイ後に変更してください。

### 5️⃣ デプロイ実行

1. **「Deploy」** ボタンをクリック
2. ビルドプロセスを待つ (1-3分)
3. デプロイ完了! 🎉

---

## 🌐 デプロイ後のURL

デプロイが完了すると、以下のようなURLが発行されます:

```
https://letteral.vercel.app
```

または

```
https://letteral-<random>.vercel.app
```

---

## 🔧 バックエンドのデプロイ

フロントエンドが動作するには、バックエンドAPIも公開する必要があります。

### オプション1: Railway (おすすめ・無料)

1. [Railway.app](https://railway.app/) にアクセス
2. GitHubでサインイン
3. 「New Project」→「Deploy from GitHub repo」
4. `hera-16/letteral` の `backend` ディレクトリを選択
5. 環境変数を設定:
   - `JWT_SECRET`: (backend/.envと同じ値)
   - `SPRING_DATASOURCE_URL`: Railway提供のMySQLを使用
   - `SPRING_DATASOURCE_USERNAME`: (Railwayが自動設定)
   - `SPRING_DATASOURCE_PASSWORD`: (Railwayが自動設定)
6. デプロイ完了後、URLをコピー (例: `https://letteral-backend.railway.app`)

### オプション2: Heroku / Render / AWS

詳細は [CUSTOM_DOMAIN_GUIDE.md](CUSTOM_DOMAIN_GUIDE.md) を参照。

---

## 🔄 バックエンドURL更新

バックエンドをデプロイしたら、Vercelの環境変数を更新:

1. Vercelダッシュボード → プロジェクト選択
2. **「Settings」** → **「Environment Variables」**
3. `NEXT_PUBLIC_API_URL` を編集
   - 新しい値: `https://letteral-backend.railway.app/api` (例)
4. **「Save」** をクリック
5. **「Deployments」** → 最新のデプロイメント → **「Redeploy」**

---

## ✅ 動作確認

1. Vercelから発行されたURLにアクセス
2. ログイン画面が表示されるか確認
3. アカウント作成・ログインを試す
4. チャット機能が動作するか確認

---

## 🐛 トラブルシューティング

### ビルドエラーが出る場合

- **エラー**: `Module not found`
  - **解決**: `package.json` の依存関係が正しいか確認
  - Vercelダッシュボードで「Redeploy」を試す

### バックエンドに接続できない場合

- **エラー**: `Network Error` / `CORS error`
  - **解決**: バックエンドの `CORS_ALLOWED_ORIGINS` 環境変数にVercelのURLを追加
  ```env
  CORS_ALLOWED_ORIGINS=https://letteral.vercel.app,http://localhost:3000
  ```

### 環境変数が反映されない場合

- Vercelダッシュボード → Settings → Environment Variables で確認
- 変更後は必ず **Redeploy** が必要

---

## 🎯 次のステップ

1. ✅ フロントエンドをVercelにデプロイ
2. ⏳ バックエンドをRailway/Herokuにデプロイ
3. ⏳ 環境変数を更新してRedeployy
4. ⏳ カスタムドメイン設定 (オプション)
5. ⏳ SSL証明書の確認 (Vercelは自動)

---

**現在のステータス**: フロントエンドの準備完了 ✅  
**次の作業**: Vercelアカウント作成 → プロジェクトインポート → デプロイ 🚀

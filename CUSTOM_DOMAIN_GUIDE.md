# 任意のURLでアプリを公開する方法

GitHubリポジトリは `github.com` のURLですが、実際のアプリケーションは**独自ドメイン**で公開できます!

## 🌐 公開方法の選択肢

### オプション1: GitHub Pages (静的サイト) - 無料 ⭐

**対象**: フロントエンドのみ (Next.js静的エクスポート)

**独自ドメイン例**: `https://your-app.com`

**特徴:**
- ✅ 完全無料
- ✅ HTTPS自動対応
- ✅ 独自ドメイン設定可能
- ❌ バックエンドAPIは別途必要

**手順:**

1. **Next.jsを静的エクスポート**
```json
// next.config.ts
const nextConfig = {
  output: 'export',
  distDir: 'out'
};
```

2. **GitHubでPages設定**
```bash
# ビルド
npm run build

# gh-pagesブランチにデプロイ
git subtree push --prefix out origin gh-pages
```

3. **Settings → Pages**
   - Source: Deploy from a branch
   - Branch: gh-pages
   - Custom domain: `your-app.com`

**URL例:**
- デフォルト: `https://hera-16.github.io/letteral`
- 独自ドメイン: `https://your-app.com` (DNS設定後)

---

### オプション2: Vercel - 無料〜 ⭐⭐⭐ (推奨)

**対象**: Next.jsフロントエンド + サーバーレスAPI

**独自ドメイン例**: `https://chatapp.vercel.app` → `https://your-app.com`

**特徴:**
- ✅ Next.js最適化済み
- ✅ 自動デプロイ (GitHubプッシュで自動反映)
- ✅ 無料プランあり
- ✅ 独自ドメイン対応
- ✅ HTTPS自動設定
- ❌ バックエンドは別途必要 (Spring Bootは動かない)

**手順:**

1. **Vercelアカウント作成**
   - https://vercel.com にアクセス
   - "Sign up with GitHub" でログイン

2. **リポジトリをインポート**
   - "New Project" をクリック
   - `hera-16/letteral` を選択
   - Root Directory: `./` (そのまま)
   - Framework Preset: Next.js

3. **環境変数を設定**
   ```
   NEXT_PUBLIC_API_URL=https://your-backend-url.com/api
   ```

4. **デプロイ**
   - "Deploy" をクリック
   - 完了後: `https://letteral-xxx.vercel.app` が発行される

5. **独自ドメイン設定**
   - Settings → Domains
   - 独自ドメインを入力 (例: `chatapp.com`)
   - DNS設定の指示に従う

**URL例:**
- デフォルト: `https://letteral.vercel.app`
- 独自ドメイン: `https://your-app.com`

---

### オプション3: Netlify - 無料〜 ⭐⭐

**対象**: フロントエンド + サーバーレス関数

**独自ドメイン例**: `https://chatapp.netlify.app` → `https://your-app.com`

**特徴:**
- ✅ 自動デプロイ
- ✅ 無料プランあり
- ✅ 独自ドメイン対応
- ✅ フォーム機能など便利機能多数
- ❌ バックエンドは別途必要

**手順:**

1. **Netlifyアカウント作成**
   - https://netlify.com にアクセス
   - GitHubでログイン

2. **リポジトリをインポート**
   - "Add new site" → "Import an existing project"
   - GitHubから `hera-16/letteral` を選択

3. **ビルド設定**
   ```
   Build command: npm run build
   Publish directory: .next
   ```

4. **独自ドメイン設定**
   - Site settings → Domain management
   - "Add custom domain"

**URL例:**
- デフォルト: `https://letteral.netlify.app`
- 独自ドメイン: `https://your-app.com`

---

### オプション4: AWS / Azure / GCP - 従量課金 ⭐⭐⭐⭐

**対象**: フルスタック (フロントエンド + バックエンド + データベース)

**独自ドメイン例**: `https://your-app.com`

**特徴:**
- ✅ すべて自由にカスタマイズ可能
- ✅ Spring Bootも動かせる
- ✅ 本番環境向け
- ✅ スケーラブル
- ❌ 有料 (使った分だけ課金)
- ❌ 設定が複雑

#### AWS (Amazon Web Services)

**推奨構成:**
- **フロントエンド**: S3 + CloudFront
- **バックエンド**: EC2 / ECS / Elastic Beanstalk
- **データベース**: RDS (MySQL)
- **ドメイン**: Route 53

**URL例**: `https://your-app.com`

**手順概要:**
1. EC2インスタンスを作成
2. Spring Bootをデプロイ
3. RDSでMySQLセットアップ
4. S3でフロントエンドをホスティング
5. Route 53で独自ドメイン設定

#### Azure

**推奨構成:**
- **フロントエンド**: Azure Static Web Apps
- **バックエンド**: Azure App Service
- **データベース**: Azure Database for MySQL

**URL例**: `https://your-app.com`

#### Google Cloud Platform (GCP)

**推奨構成:**
- **フロントエンド**: Firebase Hosting
- **バックエンド**: Cloud Run / App Engine
- **データベース**: Cloud SQL (MySQL)

**URL例**: `https://your-app.com`

---

### オプション5: VPS (Virtual Private Server) - 月額固定

**対象**: フルスタック

**独自ドメイン例**: `https://your-app.com`

**特徴:**
- ✅ 完全な自由度
- ✅ 月額固定で安心
- ✅ Spring Bootも動かせる
- ❌ サーバー管理が必要
- ❌ セキュリティ対策が必要

#### 推奨VPSサービス

**1. ConoHa VPS** (日本) - 月額680円〜
- URL: https://www.conoha.jp/vps/
- 独自ドメイン: 対応
- SSL証明書: Let's Encrypt無料

**2. さくらのVPS** (日本) - 月額590円〜
- URL: https://vps.sakura.ad.jp/
- 独自ドメイン: 対応
- SSL証明書: 対応

**3. DigitalOcean** (海外) - 月額$4〜
- URL: https://www.digitalocean.com/
- 独自ドメイン: 対応
- SSL証明書: 対応

**手順概要:**
1. VPSを契約
2. Ubuntu/CentOSをインストール
3. Java、MySQL、Nginxをインストール
4. Spring BootとNext.jsをデプロイ
5. 独自ドメインを設定
6. Let's EncryptでHTTPS化

---

## 🎯 あなたのプロジェクトに最適な選択肢

### 無料で始めたい場合

**推奨: Vercel (フロントエンド) + Railway / Render (バックエンド)**

```
フロントエンド: https://your-app.vercel.app (または独自ドメイン)
バックエンド: https://your-api.railway.app
データベース: Railway内蔵MySQL (無料枠あり)
```

**手順:**

1. **バックエンドをRailwayにデプロイ**
   - https://railway.app にアクセス
   - GitHubでログイン
   - "New Project" → "Deploy from GitHub repo"
   - `hera-16/letteral` を選択
   - Root Directory: `backend/`
   - MySQLサービスを追加
   - 環境変数を設定

2. **フロントエンドをVercelにデプロイ**
   - https://vercel.com にアクセス
   - `hera-16/letteral` をインポート
   - 環境変数: `NEXT_PUBLIC_API_URL=https://your-app.railway.app/api`

3. **独自ドメイン設定** (オプション)
   - お名前.comなどでドメイン購入 (年間1,000円〜)
   - Vercelで独自ドメイン設定

**料金:**
- ✅ Vercel: 無料
- ✅ Railway: 無料枠あり ($5/月の無料クレジット)
- ドメイン: 年間1,000円〜

---

### 本格的に運用したい場合

**推奨: VPS (ConoHa / さくら) または AWS**

```
すべて: https://your-app.com
```

**料金:**
- VPS: 月額680円〜
- AWS: 従量課金 (月額$10〜50程度)

---

## 🛠️ 独自ドメインの取得方法

### ドメイン購入サービス

1. **お名前.com** (日本最大手)
   - URL: https://www.onamae.com/
   - 料金: 年間1円〜 (.com: 年間1,200円程度)

2. **ムームードメイン** (初心者向け)
   - URL: https://muumuu-domain.com/
   - 料金: 年間69円〜

3. **Google Domains**
   - URL: https://domains.google/
   - 料金: 年間$12〜

### ドメイン設定の流れ

1. **ドメイン購入**
   ```
   例: chatapp.com を購入
   ```

2. **DNS設定**
   ```
   # Vercelの場合
   CNAME: www → cname.vercel-dns.com
   A: @ → 76.76.21.21

   # VPSの場合
   A: @ → あなたのサーバーIPアドレス
   CNAME: www → chatapp.com
   ```

3. **SSL証明書** (HTTPS化)
   - Vercel/Netlify: 自動設定
   - VPS: Let's Encrypt (無料)

---

## 📝 推奨デプロイ構成

### パターンA: 完全無料で始める

```
フロントエンド: Vercel (無料)
バックエンド: Railway (無料枠)
データベース: Railway MySQL (無料枠)
ドメイン: Vercelのサブドメイン (letteral.vercel.app)
```

**料金: 0円/月**

### パターンB: 独自ドメインで公開

```
フロントエンド: Vercel (無料)
バックエンド: Railway (無料枠)
データベース: Railway MySQL (無料枠)
ドメイン: 独自ドメイン (chatapp.com)
```

**料金: 約100円/月 (ドメイン代)**

### パターンC: 本格運用

```
フロントエンド: Vercel (無料)
バックエンド: ConoHa VPS
データベース: VPS内MySQL
ドメイン: 独自ドメイン
```

**料金: 約780円/月 (VPS 680円 + ドメイン 100円)**

### パターンD: エンタープライズ

```
すべて: AWS / Azure
```

**料金: 月額$10〜100以上**

---

## 🚀 次のステップ

### 今すぐ無料で公開する場合

1. **Vercelアカウント作成**: https://vercel.com
2. **リポジトリをインポート**: `hera-16/letteral`
3. **デプロイ**: ボタン1つで完了
4. **公開URL取得**: `https://letteral-xxx.vercel.app`

### 独自ドメインで公開する場合

1. **ドメイン購入**: お名前.com等で購入
2. **Vercelにデプロイ**: 上記の手順
3. **ドメイン設定**: Vercel Settingsで設定
4. **DNS設定**: ドメイン管理画面で設定
5. **完了**: `https://your-app.com` でアクセス可能!

---

## 💡 よくある質問

**Q: GitHubのURLを変更できますか?**
A: いいえ、GitHubのURLは固定です。しかし、アプリ自体は独自ドメインで公開できます。

**Q: 無料で独自ドメインを使えますか?**
A: ドメイン購入費用(年間1,000円程度)が必要ですが、ホスティング自体は無料(Vercel等)です。

**Q: Spring Bootも無料で公開できますか?**
A: Railway / Render の無料枠で可能です。ただし制限があります。

**Q: どの方法が一番おすすめですか?**
A: 無料で始めるなら **Vercel + Railway**、本格運用なら **VPS** がおすすめです。

---

## 📞 サポート

詳細なデプロイ手順は `docs/DEPLOYMENT.md` を参照してください。

独自ドメインでの公開をサポートします!どの方法を選択するか教えていただければ、詳細な手順を案内します。

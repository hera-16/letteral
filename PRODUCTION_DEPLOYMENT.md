# 🚀 本番環境デプロイメントガイド

## 📊 アーキテクチャ概要

```
┌─────────────────┐
│   ユーザー      │
└────────┬────────┘
         │ HTTPS
         ▼
┌─────────────────┐
│  Vercel         │ ← フロントエンド (Next.js)
│  Next.js App    │
└────────┬────────┘
         │ REST API / WebSocket
         ▼
┌─────────────────┐
│  Railway        │ ← バックエンド (Spring Boot)
│  Spring Boot    │
└────────┬────────┘
         │ JDBC
         ▼
┌─────────────────┐
│  Railway MySQL  │ ← データベース
│  or PlanetScale │
└─────────────────┘
```

---

## 🗄️ データベース選択肢

### オプション1: Railway MySQL (推奨・無料枠あり)

#### 特徴
- ✅ 無料プラン: 512MB RAM、1GB ストレージ
- ✅ 自動バックアップ
- ✅ Spring Bootと同じプラットフォーム
- ✅ 簡単なセットアップ

#### セットアップ手順
1. [Railway.app](https://railway.app/) にログイン
2. 「New Project」→「Provision MySQL」
3. 環境変数が自動生成される:
   ```
   MYSQL_URL=mysql://root:password@containers-us-west-xxx.railway.app:6379/railway
   MYSQL_HOST=containers-us-west-xxx.railway.app
   MYSQL_PORT=6379
   MYSQL_USER=root
   MYSQL_PASSWORD=xxxxxxxxxx
   MYSQL_DATABASE=railway
   ```

4. Spring Bootの環境変数に設定:
   ```env
   SPRING_DATASOURCE_URL=jdbc:mysql://${MYSQL_HOST}:${MYSQL_PORT}/${MYSQL_DATABASE}?useSSL=true&requireSSL=true&serverTimezone=Asia/Tokyo
   SPRING_DATASOURCE_USERNAME=${MYSQL_USER}
   SPRING_DATASOURCE_PASSWORD=${MYSQL_PASSWORD}
   ```

---

### オプション2: PlanetScale (無料枠豊富)

#### 特徴
- ✅ 無料プラン: 5GB ストレージ、1億行/月読み取り
- ✅ サーバーレスMySQL
- ✅ 自動スケーリング
- ❌ WebSocket制限あり

#### セットアップ手順
1. [PlanetScale](https://planetscale.com/) でアカウント作成
2. 新しいデータベースを作成
3. 接続文字列を取得:
   ```
   mysql://username:password@aws.connect.psdb.cloud/database?sslaccept=strict
   ```

4. Spring Bootの環境変数:
   ```env
   SPRING_DATASOURCE_URL=jdbc:mysql://aws.connect.psdb.cloud/database?sslMode=VERIFY_IDENTITY&serverTimezone=Asia/Tokyo
   SPRING_DATASOURCE_USERNAME=username
   SPRING_DATASOURCE_PASSWORD=password
   ```

---

### オプション3: Amazon RDS (有料・本格運用向け)

#### 特徴
- ✅ 高可用性・自動バックアップ
- ✅ スケーラブル
- ❌ 有料 (~$15/月〜)
- ❌ 複雑なセットアップ

#### セットアップ手順
1. AWS RDSでMySQL 8.0インスタンスを作成
2. セキュリティグループでポート3306を開放
3. エンドポイントを取得:
   ```
   database-1.xxxxxxxxxxxx.ap-northeast-1.rds.amazonaws.com:3306
   ```

4. Spring Bootの環境変数:
   ```env
   SPRING_DATASOURCE_URL=jdbc:mysql://database-1.xxxxxxxxxxxx.ap-northeast-1.rds.amazonaws.com:3306/chatapp?useSSL=true&requireSSL=true&serverTimezone=Asia/Tokyo
   SPRING_DATASOURCE_USERNAME=admin
   SPRING_DATASOURCE_PASSWORD=yourpassword
   ```

---

## 🔧 バックエンドデプロイ (Railway)

### 1️⃣ Railwayにデプロイ

1. **プロジェクト作成**
   ```bash
   # GitHubからデプロイ
   Railway Dashboard → New Project → Deploy from GitHub repo
   → Select: hera-16/letteral
   ```

2. **ルートディレクトリ設定**
   ```
   Root Directory: /backend
   ```

3. **環境変数設定**
   ```env
   # JWT設定
   JWT_SECRET=your-production-jwt-secret-here
   JWT_EXPIRATION=86400000
   
   # データベース設定 (Railwayが自動生成)
   SPRING_DATASOURCE_URL=${{MYSQL_URL}}
   SPRING_DATASOURCE_USERNAME=${{MYSQL_USER}}
   SPRING_DATASOURCE_PASSWORD=${{MYSQL_PASSWORD}}
   
   # CORS設定
   CORS_ALLOWED_ORIGINS=https://letteral.vercel.app,https://letteral-*.vercel.app
   
   # サーバー設定
   SERVER_PORT=8080
   ```

4. **デプロイ実行**
   - 自動でビルド・デプロイが開始されます
   - URLが発行されます: `https://letteral-production.up.railway.app`

---

## 🌐 フロントエンドデプロイ (Vercel)

### 環境変数更新

Vercel Dashboardで環境変数を更新:

```env
NEXT_PUBLIC_API_URL=https://letteral-production.up.railway.app/api
```

その後、Redeployを実行。

---

## 📋 デプロイ後のチェックリスト

### バックエンド確認

```bash
# ヘルスチェック
curl https://letteral-production.up.railway.app/actuator/health

# ログイン動作確認
curl -X POST https://letteral-production.up.railway.app/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser","password":"testpass"}'
```

### データベース確認

```bash
# Railwayダッシュボード → MySQL → Dataタブ
# または
# Railway CLIでログイン
railway login
railway link
railway connect mysql
```

### フロントエンド確認

1. https://letteral.vercel.app にアクセス
2. 新規登録を試す
3. ログインを試す
4. チャット機能を確認

---

## 🔒 セキュリティ設定

### 1. CORS設定の厳格化

`application.properties`:
```properties
# 本番環境では具体的なドメインのみ許可
app.cors.allowed-origins=${CORS_ALLOWED_ORIGINS:https://letteral.vercel.app}
```

### 2. SSL/TLS強制

Railway・Vercelは自動でHTTPSを提供します。

### 3. JWT秘密鍵の強化

```bash
# 64バイトのランダムな秘密鍵を生成
openssl rand -base64 64
```

### 4. データベース接続のSSL化

```properties
spring.datasource.url=jdbc:mysql://host:port/db?useSSL=true&requireSSL=true
```

---

## 💰 料金プラン比較

| サービス | プラン | 料金 | 特徴 |
|---------|--------|------|------|
| **Vercel** | Hobby | 無料 | 100GB帯域/月 |
| **Railway** | Trial | $5クレジット | 500時間/月実行時間 |
| **Railway MySQL** | 含まれる | 無料 | 512MB RAM、1GB ストレージ |
| **PlanetScale** | Free | 無料 | 5GB ストレージ |
| **Amazon RDS** | db.t3.micro | ~$15/月 | 1GB RAM、20GB ストレージ |

### 推奨: 無料構成
- **フロントエンド**: Vercel (無料)
- **バックエンド**: Railway ($5クレジット/月)
- **データベース**: Railway MySQL (無料) または PlanetScale (無料)

**合計コスト**: 実質無料〜$5/月

---

## 🚨 トラブルシューティング

### データベース接続エラー

**エラー**: `Communications link failure`

**解決策**:
1. データベースのホスト・ポートを確認
2. SSL設定を確認 (`useSSL=true`)
3. タイムゾーン設定 (`serverTimezone=Asia/Tokyo`)
4. ファイアウォールの確認

### CORS エラー

**エラー**: `Access-Control-Allow-Origin` エラー

**解決策**:
1. バックエンドの環境変数 `CORS_ALLOWED_ORIGINS` にVercelのURLを追加
2. ワイルドカード使用: `https://*.vercel.app`
3. Spring Bootを再起動

### WebSocket接続エラー

**エラー**: WebSocketが接続できない

**解決策**:
1. Railwayは WebSocket をサポート
2. STOMP設定を確認
3. CORSにWebSocketエンドポイントを追加

---

## 📈 スケーリング戦略

### 初期段階 (〜100ユーザー)
- Railway Starter ($5/月)
- Railway MySQL (512MB)

### 成長期 (100〜1,000ユーザー)
- Railway Pro ($20/月)
- PlanetScale Scaler ($29/月)

### 本格運用 (1,000〜10,000ユーザー)
- AWS ECS / Kubernetes
- Amazon RDS (Multi-AZ)
- Redis (キャッシング)

---

## 🎯 次のステップ

1. ✅ Railwayでバックエンド+MySQLをデプロイ
2. ✅ Vercelの環境変数を更新
3. ✅ 動作確認
4. ⏳ カスタムドメイン設定
5. ⏳ 監視・ログ設定 (Sentry, LogRocket)
6. ⏳ バックアップ自動化

---

**作成日**: 2025年10月15日  
**対象**: 本番環境デプロイメント

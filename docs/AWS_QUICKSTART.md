# AWS デプロイ クイックスタートガイド

> 最短でLetteralをAWSにデプロイするための簡易ガイド

## 📋 前提条件

- AWSアカウント（作成済み）
- AWS CLI（インストール・設定済み）
- Docker（インストール・起動済み）
- Git

## 🚀 5ステップでデプロイ

### ステップ1: 設定ファイルの準備

```bash
# プロジェクトディレクトリに移動
cd "c:\Users\User\OneDrive\hera-16\チャレキャラ"

# デプロイスクリプトを実行（初回は設定ファイルが自動生成される）
# Windows
.\deploy-to-aws.ps1 -Environment dev -Step setup

# Mac/Linux
./deploy-to-aws.sh dev setup
```

実行すると `.aws-deploy-config-dev.env` ファイルが作成されます。

### ステップ2: 設定ファイルの編集

`.aws-deploy-config-dev.env` を開いて、以下の項目を編集してください：

```env
# AWS設定
AWS_REGION=ap-northeast-1
AWS_ACCOUNT_ID=123456789012  # ← あなたのAWSアカウントIDに変更

# RDS設定（RDS作成後に編集）
RDS_ENDPOINT=letteral-dev-db.xxxxxxxxxxxx.ap-northeast-1.rds.amazonaws.com  # ← RDSのエンドポイント
RDS_PASSWORD=YOUR_STRONG_PASSWORD  # ← RDSで設定したパスワード

# JWT Secret（強力なランダム文字列を生成）
JWT_SECRET=YOUR_JWT_SECRET_HERE  # ← 以下のコマンドで生成
```

#### AWSアカウントIDの確認方法

```bash
aws sts get-caller-identity --query Account --output text
```

#### JWT Secretの生成方法

**Windows PowerShell:**
```powershell
$bytes = New-Object byte[] 64
[Security.Cryptography.RandomNumberGenerator]::Create().GetBytes($bytes)
[Convert]::ToBase64String($bytes)
```

**Mac/Linux:**
```bash
openssl rand -base64 64 | tr -d '\n'
```

### ステップ3: RDSデータベースの作成

AWSコンソールで以下を実行：

1. **RDSサービスを開く**
2. **「データベースの作成」をクリック**
3. **設定:**
   - エンジン: MySQL 8.0.33
   - テンプレート: 無料利用枠（開発用）
   - DBインスタンス識別子: `letteral-dev-db`
   - マスターユーザー名: `admin`
   - マスターパスワード: **強力なパスワード（メモしておく）**
   - 初期データベース名: `letteral_dev`
   - パブリックアクセス: はい（初期セットアップ用）

4. **「データベースの作成」をクリック**（⏱️ 約10分）

5. **作成完了後、エンドポイントをコピー**
   - 例: `letteral-dev-db.xxxxxxxxxxxx.ap-northeast-1.rds.amazonaws.com`

6. **設定ファイルを再編集**
   - `.aws-deploy-config-dev.env` の `RDS_ENDPOINT` と `RDS_PASSWORD` を更新

### ステップ4: AWSリソースのセットアップ

```bash
# ECR、ECS、CloudWatch Logsを自動セットアップ
# Windows
.\deploy-to-aws.ps1 -Environment dev -Step setup

# Mac/Linux
./deploy-to-aws.sh dev setup
```

このコマンドで以下が作成されます：
- ✅ ECRリポジトリ（Dockerイメージ保存用）
- ✅ ECSクラスター（コンテナ実行環境）
- ✅ CloudWatch Logsグループ（ログ記録）

### ステップ5: デプロイ実行

```bash
# ビルド → プッシュ → デプロイを一括実行
# Windows
.\deploy-to-aws.ps1 -Environment dev -Step all

# Mac/Linux
./deploy-to-aws.sh dev all
```

⏱️ 初回は約15-20分かかります。

## ✅ 動作確認

デプロイが完了したら、以下で動作確認：

### 1. ヘルスチェック

```bash
# ALBのDNS名を確認
aws elbv2 describe-load-balancers \
  --query 'LoadBalancers[?contains(LoadBalancerName, `letteral`)].DNSName' \
  --output text

# ヘルスチェックエンドポイントにアクセス
curl http://YOUR_ALB_DNS/actuator/health

# 期待される出力:
# {"status":"UP"}
```

### 2. ユーザー登録のテスト

```bash
curl -X POST http://YOUR_ALB_DNS/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "Password123!",
    "displayName": "テストユーザー"
  }'
```

### 3. ログの確認

```bash
# リアルタイムでログを確認
aws logs tail /ecs/letteral-backend --follow --region ap-northeast-1
```

## 🔄 更新デプロイ

コードを変更した後、再デプロイする方法：

```bash
# ビルド → プッシュ → デプロイ
# Windows
.\deploy-to-aws.ps1 -Environment dev -Step all

# Mac/Linux
./deploy-to-aws.sh dev all
```

または、各ステップを個別に実行：

```bash
# 1. ビルド
.\deploy-to-aws.ps1 -Environment dev -Step build

# 2. ECRにプッシュ
.\deploy-to-aws.ps1 -Environment dev -Step push

# 3. ECSにデプロイ
.\deploy-to-aws.ps1 -Environment dev -Step deploy
```

## 🛠️ トラブルシューティング

### 問題: タスクが起動しない

**確認方法:**
```bash
# ECSタスクの状態を確認
aws ecs describe-services \
  --cluster letteral-dev-cluster \
  --services letteral-backend-service \
  --region ap-northeast-1
```

**よくある原因:**
1. RDSのセキュリティグループでECSからのアクセスが拒否されている
2. 環境変数の設定ミス（特にデータベース接続情報）
3. ECRからイメージをプルできない

**解決策:**
RDSのセキュリティグループに以下のインバウンドルールを追加：
- タイプ: MySQL/Aurora
- ポート: 3306
- ソース: ECSタスクのセキュリティグループ

### 問題: データベース接続エラー

**確認ポイント:**
1. RDSのエンドポイントが正しいか
2. パスワードが正しいか
3. データベース名が `letteral_dev` になっているか
4. セキュリティグループでポート3306が開いているか

### 問題: ECRにプッシュできない

**確認:**
```bash
# ECRにログインできるか確認
aws ecr get-login-password --region ap-northeast-1
```

**解決策:**
```bash
# 再度ログイン
aws ecr get-login-password --region ap-northeast-1 | \
  docker login --username AWS --password-stdin \
  YOUR_ACCOUNT_ID.dkr.ecr.ap-northeast-1.amazonaws.com
```

## 💰 コスト管理

### 使わない時は停止

```bash
# ECSタスクを0に設定
aws ecs update-service \
  --cluster letteral-dev-cluster \
  --service letteral-backend-service \
  --desired-count 0 \
  --region ap-northeast-1

# RDSを停止（最大7日間）
aws rds stop-db-instance \
  --db-instance-identifier letteral-dev-db
```

### 再開

```bash
# RDSを起動
aws rds start-db-instance \
  --db-instance-identifier letteral-dev-db

# ECSタスクを再開
aws ecs update-service \
  --cluster letteral-dev-cluster \
  --service letteral-backend-service \
  --desired-count 2 \
  --region ap-northeast-1
```

### 月次コスト概算（開発環境）

- RDS db.t3.micro: $0（無料枠）
- ECS Fargate 2タスク: $30-40
- ALB: $20-25
- CloudWatch Logs: $5
- **合計: 約$55-70/月**

## 📚 詳細ガイド

より詳しい情報は以下を参照してください：

- [AWS初心者向けガイド](./AWS_BEGINNER_GUIDE.md) - 画像付き詳細手順
- [AWSデプロイガイド](./AWS_DEPLOYMENT.md) - 技術的な詳細
- [AWSインフラ設計書](./AWS_INFRASTRUCTURE_DESIGN.md) - アーキテクチャ詳細

## 🆘 サポート

問題が発生した場合：

1. **CloudWatch Logsを確認**
   ```bash
   aws logs tail /ecs/letteral-backend --follow --region ap-northeast-1
   ```

2. **ECSタスクのステータスを確認**
   - AWSコンソール → ECS → クラスター → タスク

3. **RDSの接続テスト**
   ```bash
   mysql -h YOUR_RDS_ENDPOINT -u admin -p letteral_dev
   ```

---

**最終更新**: 2025-01-18
**バージョン**: 1.0.0

# Letteral AWS Infrastructure - Terraform

このディレクトリには、LetteralプラットフォームのAWSインフラストラクチャをコードとして管理するためのTerraformファイルが含まれています。

## 📁 ディレクトリ構成

```
terraform/
├── main.tf                 # メイン設定ファイル
├── variables.tf            # 変数定義
├── outputs.tf             # 出力値定義
├── terraform.tfvars       # 変数値（Git除外）
├── environments/          # 環境別設定
│   ├── prod/             # 本番環境
│   │   ├── terraform.tfvars
│   │   └── backend.tf
│   └── staging/          # ステージング環境
│       ├── terraform.tfvars
│       └── backend.tf
└── modules/              # 再利用可能なモジュール
    ├── vpc/             # VPCとネットワーク
    ├── security_groups/ # セキュリティグループ
    ├── rds/            # RDS MySQL
    ├── elasticache/    # ElastiCache Redis
    ├── ecs/            # ECS Fargate
    ├── alb/            # Application Load Balancer
    ├── s3/             # S3バケット
    └── cloudfront/     # CloudFront CDN
```

## 🚀 使用方法

### 前提条件

1. **Terraform** (>= 1.5.0) のインストール
2. **AWS CLI** の設定
3. **AWS認証情報** の設定

```bash
# AWS CLIのインストール確認
aws --version

# AWS認証情報の設定
aws configure
```

### 初期セットアップ

#### 1. S3バックエンドの作成

Terraformの状態ファイルを管理するためのS3バケットとDynamoDBテーブルを作成します。

```bash
# S3バケット作成
aws s3 mb s3://letteral-terraform-state --region ap-northeast-1

# バージョニング有効化
aws s3api put-bucket-versioning \
  --bucket letteral-terraform-state \
  --versioning-configuration Status=Enabled

# 暗号化有効化
aws s3api put-bucket-encryption \
  --bucket letteral-terraform-state \
  --server-side-encryption-configuration '{
    "Rules": [{
      "ApplyServerSideEncryptionByDefault": {
        "SSEAlgorithm": "AES256"
      }
    }]
  }'

# DynamoDBテーブル作成（ロック用）
aws dynamodb create-table \
  --table-name letteral-terraform-lock \
  --attribute-definitions AttributeName=LockID,AttributeType=S \
  --key-schema AttributeName=LockID,KeyType=HASH \
  --provisioned-throughput ReadCapacityUnits=1,WriteCapacityUnits=1 \
  --region ap-northeast-1
```

#### 2. 環境変数ファイルの作成

```bash
# terraform.tfvarsファイルを作成
cp terraform.tfvars.example terraform.tfvars

# 必要な値を設定
vim terraform.tfvars
```

**terraform.tfvars の例:**

```hcl
environment = "prod"
aws_region  = "ap-northeast-1"
domain_name = "letteral.example.com"

# Database
db_username = "letteral_admin"
db_password = "your-strong-password-here"  # AWS Secrets Managerから取得推奨

# Security
jwt_secret = "your-jwt-secret-here"  # AWS Secrets Managerから取得推奨

# ACM Certificate ARN (事前に作成)
acm_certificate_arn = "arn:aws:acm:us-east-1:123456789012:certificate/xxxxx"
```

### Terraform実行

#### 初期化

```bash
cd terraform
terraform init
```

#### プランの確認

```bash
terraform plan
```

#### インフラのデプロイ

```bash
terraform apply
```

#### 環境別デプロイ

```bash
# 本番環境
terraform apply -var-file="environments/prod/terraform.tfvars"

# ステージング環境
terraform apply -var-file="environments/staging/terraform.tfvars"
```

#### 特定モジュールのみデプロイ

```bash
terraform apply -target=module.vpc
terraform apply -target=module.rds
```

#### インフラの削除

```bash
terraform destroy
```

## 📦 モジュール詳細

### VPC モジュール

**作成されるリソース:**
- VPC (10.0.0.0/16)
- パブリックサブネット × 3 (各AZ)
- プライベートサブネット × 3 (各AZ)
- データベースサブネット × 3 (各AZ)
- インターネットゲートウェイ
- NATゲートウェイ × 3 (各AZ)
- ルートテーブル
- VPCエンドポイント (S3)

### RDS モジュール

**作成されるリソース:**
- RDS MySQL 8.0 (Multi-AZ)
- DBサブネットグループ
- DBパラメータグループ
- セキュリティグループ
- CloudWatch アラーム

**主要設定:**
- インスタンスクラス: db.t3.medium (本番: db.r6g.large)
- ストレージ: 100GB (gp3, 自動拡張有効)
- バックアップ: 7日間保持
- 暗号化: KMS

### ECS モジュール

**作成されるリソース:**
- ECSクラスター (Fargate)
- タスク定義
- ECSサービス
- Auto Scaling設定
- IAMロール
- CloudWatch ログ

**主要設定:**
- CPU: 1 vCPU
- メモリ: 2 GB
- タスク数: 2-10 (Auto Scaling)
- ヘルスチェック: /actuator/health

### ALB モジュール

**作成されるリソース:**
- Application Load Balancer
- ターゲットグループ
- リスナー (HTTP/HTTPS)
- セキュリティグループ

**主要設定:**
- SSL/TLS証明書 (ACM)
- HTTPS強制リダイレクト
- ヘルスチェック間隔: 30秒

## 🔐 セキュリティ

### Secrets Manager統合

機密情報はAWS Secrets Managerで管理することを推奨します。

```bash
# JWT Secretの作成
aws secretsmanager create-secret \
  --name letteral/prod/jwt-secret \
  --secret-string "your-jwt-secret-here" \
  --region ap-northeast-1

# DB認証情報の作成
aws secretsmanager create-secret \
  --name letteral/prod/db-credentials \
  --secret-string '{"username":"letteral_admin","password":"your-password"}' \
  --region ap-northeast-1
```

### IAM最小権限

各リソースには最小限の権限のみを付与しています。

- **ECS Task Execution Role**: ECRプル、CloudWatch Logsのみ
- **ECS Task Role**: S3、RDS、SES、Secrets Managerへの限定的アクセス

## 📊 コスト管理

### コスト見積もり

```bash
# Terraform Cost Estimation (Infracost使用)
infracost breakdown --path .
```

### 推定月額コスト

| リソース | スペック | 月額 (USD) |
|---------|---------|-----------|
| ECS Fargate | 2タスク | $60 |
| RDS | db.t3.medium | $100 |
| ElastiCache | cache.t3.micro | $20 |
| ALB | | $25 |
| NAT Gateway | 3個 | $100 |
| CloudWatch | | $10 |
| **合計** | | **$315/月** |

### コスト最適化

1. **Reserved Instances**: RDS (30%割引)
2. **Savings Plans**: Fargate (20%割引)
3. **NAT Gateway削減**: 1個のみ使用（開発環境）

## 🔄 CI/CD統合

GitHub Actionsとの連携例:

```yaml
# .github/workflows/terraform.yml
name: Terraform

on:
  push:
    branches: [main]
    paths: ['terraform/**']

jobs:
  terraform:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: hashicorp/setup-terraform@v2

      - name: Terraform Init
        run: terraform init

      - name: Terraform Plan
        run: terraform plan -out=tfplan

      - name: Terraform Apply
        if: github.ref == 'refs/heads/main'
        run: terraform apply tfplan
```

## 🛠 トラブルシューティング

### 問題: Terraform state lock取得失敗

```bash
# ロックの強制解除（注意: 他の操作が実行中でないことを確認）
terraform force-unlock <LOCK_ID>
```

### 問題: RDS作成に時間がかかる

Multi-AZ RDSの作成には10-20分かかることがあります。

### 問題: ECS タスクが起動しない

1. CloudWatch Logsを確認
2. セキュリティグループのルールを確認
3. タスク定義の環境変数を確認

```bash
# ECS タスクログの確認
aws logs tail /aws/ecs/letteral-backend-prod --follow
```

## 📚 参考資料

- [Terraform AWS Provider Documentation](https://registry.terraform.io/providers/hashicorp/aws/latest/docs)
- [AWS ECS Best Practices](https://docs.aws.amazon.com/AmazonECS/latest/bestpracticesguide/)
- [AWS Well-Architected Framework](https://aws.amazon.com/architecture/well-architected/)

## 📝 変更履歴

- **v1.0.0** (2025-01-XX): 初回リリース
  - VPC、RDS、ECS、ALBの基本構成
  - Multi-AZ対応
  - Auto Scaling設定

## 🤝 コントリビューション

インフラの変更は必ず以下の手順で:

1. `terraform plan` で変更内容を確認
2. プルリクエスト作成
3. レビュー承認後、`terraform apply`

## 📄 ライセンス

Proprietary - Letteral Team

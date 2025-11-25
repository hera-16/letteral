# Letteral AWS デプロイ手順書

本書は、LetteralプラットフォームをAWS環境にデプロイするための完全な手順を記載しています。

## 目次

1. [前提条件](#前提条件)
2. [事前準備](#事前準備)
3. [AWSリソースのプロビジョニング](#awsリソースのプロビジョニング)
4. [アプリケーションのデプロイ](#アプリケーションのデプロイ)
5. [動作確認](#動作確認)
6. [トラブルシューティング](#トラブルシューティング)
7. [ロールバック手順](#ロールバック手順)

---

## 前提条件

### 必要なツール

- **AWS CLI** (>= 2.0)
- **Terraform** (>= 1.5.0)
- **Docker** (>= 20.10)
- **Git**
- **Java 17** (ローカルビルド用)
- **Maven** (>= 3.9)

### AWSアカウント要件

- AWSアカウント（管理者権限推奨）
- AWS CLIの認証情報設定済み
- 使用リージョン: `ap-northeast-1` (東京)

### 推奨スペック

- **RDS**: db.t3.medium以上
- **ECS Fargate**: 1vCPU / 2GB RAM × 2タスク以上
- **ElastiCache**: cache.t3.micro以上

---

## 事前準備

### 1. AWSアカウントの準備

```bash
# AWS CLIの設定確認
aws configure list

# 認証情報の設定（未設定の場合）
aws configure
# AWS Access Key ID: YOUR_ACCESS_KEY
# AWS Secret Access Key: YOUR_SECRET_KEY
# Default region name: ap-northeast-1
# Default output format: json
```

### 2. ドメインの準備

#### Route 53でホストゾーンを作成

```bash
# ホストゾーンの作成
aws route53 create-hosted-zone \
  --name letteral.example.com \
  --caller-reference $(date +%s)

# ネームサーバーの確認（ドメインレジストラに設定）
aws route53 get-hosted-zone --id /hostedzone/YOUR_ZONE_ID
```

#### ACM証明書の発行

```bash
# 証明書のリクエスト
aws acm request-certificate \
  --domain-name letteral.example.com \
  --subject-alternative-names *.letteral.example.com \
  --validation-method DNS \
  --region us-east-1  # CloudFront用はus-east-1

# 証明書のARNを記録
export ACM_CERT_ARN="arn:aws:acm:us-east-1:123456789012:certificate/xxxxx"
```

DNS検証レコードをRoute 53に追加して証明書を検証してください。

### 3. シークレット情報の生成

#### JWT Secretの生成

```bash
# 強力なランダム文字列を生成（256bit）
JWT_SECRET=$(openssl rand -base64 64 | tr -d '\n')
echo "JWT_SECRET=$JWT_SECRET"
```

#### データベースパスワードの生成

```bash
# 強力なランダムパスワードを生成
DB_PASSWORD=$(openssl rand -base64 32 | tr -d '\n' | head -c 32)
echo "DB_PASSWORD=$DB_PASSWORD"
```

#### AWS Secrets Managerに保存

```bash
# JWT Secretの保存
aws secretsmanager create-secret \
  --name letteral/prod/jwt-secret \
  --secret-string "$JWT_SECRET" \
  --region ap-northeast-1

# データベース認証情報の保存
aws secretsmanager create-secret \
  --name letteral/prod/db-credentials \
  --secret-string "{\"username\":\"letteral_admin\",\"password\":\"$DB_PASSWORD\"}" \
  --region ap-northeast-1
```

---

## AWSリソースのプロビジョニング

### Terraform を使用したインフラ構築

#### 1. Terraformの初期化

```bash
cd terraform

# S3バックエンドの作成
aws s3 mb s3://letteral-terraform-state --region ap-northeast-1
aws s3api put-bucket-versioning \
  --bucket letteral-terraform-state \
  --versioning-configuration Status=Enabled

# DynamoDBロックテーブルの作成
aws dynamodb create-table \
  --table-name letteral-terraform-lock \
  --attribute-definitions AttributeName=LockID,AttributeType=S \
  --key-schema AttributeName=LockID,KeyType=HASH \
  --provisioned-throughput ReadCapacityUnits=1,WriteCapacityUnits=1 \
  --region ap-northeast-1

# Terraform初期化
terraform init
```

#### 2. 環境変数の設定

`terraform.tfvars` を作成:

```hcl
environment = "prod"
aws_region  = "ap-northeast-1"
domain_name = "letteral.example.com"

# VPC
vpc_cidr = "10.0.0.0/16"

# Database
db_instance_class = "db.t3.medium"
db_name           = "letteral_prod"
db_username       = "letteral_admin"
db_password       = "YOUR_DB_PASSWORD_HERE"  # Secrets Managerから取得
rds_multi_az      = true

# Redis
redis_node_type      = "cache.t3.micro"
redis_num_cache_nodes = 2

# ECS
ecs_task_cpu      = 1024
ecs_task_memory   = 2048
ecs_desired_count = 2
ecs_min_capacity  = 2
ecs_max_capacity  = 10

# Security
jwt_secret          = "YOUR_JWT_SECRET_HERE"  # Secrets Managerから取得
acm_certificate_arn = "YOUR_ACM_CERT_ARN"
```

#### 3. インフラのデプロイ

```bash
# プランの確認
terraform plan -out=tfplan

# インフラのデプロイ（約20-30分）
terraform apply tfplan

# 出力値の確認
terraform output
```

出力例:
```
alb_dns_name = "letteral-alb-xxxxx.ap-northeast-1.elb.amazonaws.com"
rds_endpoint = "letteral-db.xxxxx.ap-northeast-1.rds.amazonaws.com:3306"
redis_endpoint = "letteral-redis.xxxxx.cache.amazonaws.com:6379"
cloudfront_domain = "d1234567890abc.cloudfront.net"
ecr_repository_url = "123456789012.dkr.ecr.ap-northeast-1.amazonaws.com/letteral-backend"
```

---

## アプリケーションのデプロイ

### 1. ECRへのDockerイメージプッシュ

#### ローカルビルドとプッシュ

```bash
# ECRにログイン
aws ecr get-login-password --region ap-northeast-1 | \
  docker login --username AWS --password-stdin \
  123456789012.dkr.ecr.ap-northeast-1.amazonaws.com

# ECRリポジトリURLを取得
ECR_REPO=$(terraform output -raw ecr_repository_url)

# Dockerイメージのビルド
cd backend
docker build -t letteral-backend:latest -f Dockerfile.prod .

# タグ付け
docker tag letteral-backend:latest $ECR_REPO:latest
docker tag letteral-backend:latest $ECR_REPO:v1.0.0

# プッシュ
docker push $ECR_REPO:latest
docker push $ECR_REPO:v1.0.0
```

### 2. ECS タスク定義の作成

`ecs-task-definition.json` を作成:

```json
{
  "family": "letteral-backend-task",
  "networkMode": "awsvpc",
  "requiresCompatibilities": ["FARGATE"],
  "cpu": "1024",
  "memory": "2048",
  "executionRoleArn": "arn:aws:iam::123456789012:role/ecsTaskExecutionRole",
  "taskRoleArn": "arn:aws:iam::123456789012:role/ecsTaskRole",
  "containerDefinitions": [
    {
      "name": "letteral-backend",
      "image": "123456789012.dkr.ecr.ap-northeast-1.amazonaws.com/letteral-backend:latest",
      "portMappings": [
        {
          "containerPort": 8080,
          "protocol": "tcp"
        }
      ],
      "environment": [
        {
          "name": "SPRING_PROFILES_ACTIVE",
          "value": "prod"
        },
        {
          "name": "SPRING_DATASOURCE_URL",
          "value": "jdbc:mysql://YOUR_RDS_ENDPOINT:3306/letteral_prod?useSSL=true&serverTimezone=Asia/Tokyo"
        },
        {
          "name": "SPRING_REDIS_HOST",
          "value": "YOUR_REDIS_ENDPOINT"
        }
      ],
      "secrets": [
        {
          "name": "JWT_SECRET",
          "valueFrom": "arn:aws:secretsmanager:ap-northeast-1:123456789012:secret:letteral/prod/jwt-secret"
        },
        {
          "name": "SPRING_DATASOURCE_USERNAME",
          "valueFrom": "arn:aws:secretsmanager:ap-northeast-1:123456789012:secret:letteral/prod/db-credentials:username::"
        },
        {
          "name": "SPRING_DATASOURCE_PASSWORD",
          "valueFrom": "arn:aws:secretsmanager:ap-northeast-1:123456789012:secret:letteral/prod/db-credentials:password::"
        }
      ],
      "logConfiguration": {
        "logDriver": "awslogs",
        "options": {
          "awslogs-group": "/aws/ecs/letteral-backend-prod",
          "awslogs-region": "ap-northeast-1",
          "awslogs-stream-prefix": "ecs"
        }
      },
      "healthCheck": {
        "command": ["CMD-SHELL", "curl -f http://localhost:8080/actuator/health || exit 1"],
        "interval": 30,
        "timeout": 5,
        "retries": 3,
        "startPeriod": 60
      }
    }
  ]
}
```

タスク定義を登録:

```bash
aws ecs register-task-definition \
  --cli-input-json file://ecs-task-definition.json
```

### 3. ECS サービスの作成

```bash
# サービスの作成
aws ecs create-service \
  --cluster letteral-prod-cluster \
  --service-name letteral-backend-service \
  --task-definition letteral-backend-task \
  --desired-count 2 \
  --launch-type FARGATE \
  --network-configuration "awsvpcConfiguration={subnets=[subnet-xxxxx,subnet-yyyyy],securityGroups=[sg-xxxxx],assignPublicIp=DISABLED}" \
  --load-balancers "targetGroupArn=arn:aws:elasticloadbalancing:ap-northeast-1:123456789012:targetgroup/letteral-tg/xxxxx,containerName=letteral-backend,containerPort=8080" \
  --health-check-grace-period-seconds 60
```

### 4. Auto Scaling の設定

```bash
# Auto Scalingターゲットの登録
aws application-autoscaling register-scalable-target \
  --service-namespace ecs \
  --resource-id service/letteral-prod-cluster/letteral-backend-service \
  --scalable-dimension ecs:service:DesiredCount \
  --min-capacity 2 \
  --max-capacity 10

# CPU使用率ベースのスケーリングポリシー
aws application-autoscaling put-scaling-policy \
  --service-namespace ecs \
  --resource-id service/letteral-prod-cluster/letteral-backend-service \
  --scalable-dimension ecs:service:DesiredCount \
  --policy-name cpu-scaling-policy \
  --policy-type TargetTrackingScaling \
  --target-tracking-scaling-policy-configuration file://scaling-policy.json
```

`scaling-policy.json`:

```json
{
  "TargetValue": 70.0,
  "PredefinedMetricSpecification": {
    "PredefinedMetricType": "ECSServiceAverageCPUUtilization"
  },
  "ScaleOutCooldown": 60,
  "ScaleInCooldown": 300
}
```

---

## 動作確認

### 1. ヘルスチェック

```bash
# ALB経由でヘルスチェック
ALB_DNS=$(terraform output -raw alb_dns_name)
curl https://$ALB_DNS/actuator/health

# 期待される出力
# {"status":"UP"}
```

### 2. API動作確認

```bash
# ユーザー登録
curl -X POST https://$ALB_DNS/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "Password123!"
  }'

# ログイン
curl -X POST https://$ALB_DNS/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "Password123!"
  }'
```

### 3. CloudWatch Logs確認

```bash
# ECSタスクログの確認
aws logs tail /aws/ecs/letteral-backend-prod --follow

# RDSログの確認
aws rds describe-db-log-files \
  --db-instance-identifier letteral-prod-db

# ALBアクセスログの確認（S3）
aws s3 ls s3://letteral-alb-logs/
```

### 4. モニタリング

CloudWatchダッシュボードで以下を確認:

- **ECS メトリクス**: CPU/メモリ使用率、タスク数
- **ALB メトリクス**: リクエスト数、レスポンスタイム、エラー率
- **RDS メトリクス**: 接続数、CPU使用率、ディスク使用量
- **Redis メトリクス**: CPU使用率、接続数、キャッシュヒット率

---

## トラブルシューティング

### ECSタスクが起動しない

#### 原因1: ECRからイメージをプルできない

```bash
# ECS Task Execution Roleの権限を確認
aws iam get-role-policy \
  --role-name ecsTaskExecutionRole \
  --policy-name ECSTaskExecutionRolePolicy
```

必要な権限:
- `ecr:GetAuthorizationToken`
- `ecr:BatchCheckLayerAvailability`
- `ecr:GetDownloadUrlForLayer`
- `ecr:BatchGetImage`

#### 原因2: Secrets Managerにアクセスできない

```bash
# タスクロールに権限を追加
aws iam put-role-policy \
  --role-name ecsTaskRole \
  --policy-name SecretsManagerAccess \
  --policy-document file://secrets-policy.json
```

#### 原因3: ネットワーク設定の問題

```bash
# セキュリティグループのルールを確認
aws ec2 describe-security-groups --group-ids sg-xxxxx

# サブネットのルートテーブルを確認
aws ec2 describe-route-tables --filters "Name=association.subnet-id,Values=subnet-xxxxx"
```

### データベース接続エラー

```bash
# RDSセキュリティグループの確認
aws rds describe-db-instances \
  --db-instance-identifier letteral-prod-db \
  --query 'DBInstances[0].VpcSecurityGroups'

# ECSからRDSへの接続テスト
# ECSタスク内で実行
mysql -h YOUR_RDS_ENDPOINT -u letteral_admin -p letteral_prod
```

### パフォーマンス問題

#### CPU使用率が高い

```bash
# タスクのCPU/メモリを増やす
aws ecs update-service \
  --cluster letteral-prod-cluster \
  --service letteral-backend-service \
  --task-definition letteral-backend-task:2  # CPU/メモリを増やした新しいタスク定義
```

#### データベース接続数が多い

```bash
# RDS接続数の確認
aws cloudwatch get-metric-statistics \
  --namespace AWS/RDS \
  --metric-name DatabaseConnections \
  --dimensions Name=DBInstanceIdentifier,Value=letteral-prod-db \
  --start-time 2024-01-01T00:00:00Z \
  --end-time 2024-01-01T23:59:59Z \
  --period 3600 \
  --statistics Average
```

application.propertiesでコネクションプールを調整:

```properties
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.minimum-idle=2
```

---

## ロールバック手順

### 方法1: 以前のタスク定義に戻す

```bash
# タスク定義のリビジョン一覧を確認
aws ecs list-task-definitions --family-prefix letteral-backend-task

# 以前のリビジョンに更新
aws ecs update-service \
  --cluster letteral-prod-cluster \
  --service letteral-backend-service \
  --task-definition letteral-backend-task:1  # 以前のリビジョン
```

### 方法2: 以前のDockerイメージに戻す

```bash
# 以前のイメージタグを使用
ECR_REPO=$(terraform output -raw ecr_repository_url)

# タスク定義を更新（イメージタグを変更）
# ecs-task-definition.json のimageフィールドを編集
# "image": "$ECR_REPO:v1.0.0"  # 以前のバージョン

# タスク定義を再登録
aws ecs register-task-definition \
  --cli-input-json file://ecs-task-definition.json

# サービスを更新
aws ecs update-service \
  --cluster letteral-prod-cluster \
  --service letteral-backend-service \
  --force-new-deployment
```

### 方法3: Blue/Green Deployment（推奨）

CodeDeployを使用したBlue/Greenデプロイメントでは、自動的にロールバックが可能です。

---

## セキュリティベストプラクティス

### 1. IAM権限の最小化

- ECS Task Execution Role: ECR pull、CloudWatch Logsのみ
- ECS Task Role: 必要なAWSサービスへの最小限のアクセス

### 2. ネットワークセキュリティ

- RDSとRedisはプライベートサブネットに配置
- セキュリティグループで厳密にアクセス制御
- NACLで追加の防御層

### 3. データ暗号化

- RDS: KMS暗号化有効
- S3: SSE-S3またはSSE-KMS
- EBS: KMS暗号化有効
- 転送時: TLS 1.2以上

### 4. 監査とコンプライアンス

- CloudTrail: 全API操作の記録
- VPC Flow Logs: ネットワークトラフィックの記録
- AWS Config: リソース構成の記録

---

## 次のステップ

1. **モニタリングとアラート設定**: CloudWatchアラームの設定
2. **バックアップ戦略の実装**: RDSスナップショット、S3レプリケーション
3. **災害復旧計画の策定**: DRリージョンの構築
4. **パフォーマンス最適化**: CloudFrontキャッシュ、RDS Read Replica
5. **コスト最適化**: Reserved Instances、Savings Plansの購入

---

## サポート

問題が発生した場合:

1. CloudWatch Logsを確認
2. AWS X-Rayで分散トレーシング
3. チームのSlackチャンネルで相談
4. AWS Supportに問い合わせ（Enterprise Support契約の場合）

---

## 変更履歴

- **v1.0.0** (2025-01-18): 初版作成
  - AWS ECS/RDS/ElastiCacheを使用した本番環境デプロイ手順

---

## 参考資料

- [AWS ECS Documentation](https://docs.aws.amazon.com/ecs/)
- [AWS RDS Best Practices](https://docs.aws.amazon.com/AmazonRDS/latest/UserGuide/CHAP_BestPractices.html)
- [AWS Well-Architected Framework](https://aws.amazon.com/architecture/well-architected/)
- [Terraform AWS Provider](https://registry.terraform.io/providers/hashicorp/aws/latest/docs)

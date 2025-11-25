#!/bin/bash

###############################################################################
# Letteral AWS デプロイ自動化スクリプト
#
# このスクリプトは、LetteralプラットフォームをAWSにデプロイするプロセスを
# 自動化します。
#
# 使い方:
#   ./deploy-to-aws.sh [環境] [ステップ]
#
# 環境:
#   dev  - 開発環境
#   prod - 本番環境
#
# ステップ:
#   setup    - 初期セットアップ（AWSリソースの作成）
#   build    - Dockerイメージのビルド
#   push     - ECRへのプッシュ
#   deploy   - ECSへのデプロイ
#   all      - すべてのステップを実行
#
# 例:
#   ./deploy-to-aws.sh dev all
#   ./deploy-to-aws.sh prod deploy
###############################################################################

set -e  # エラーが発生したら即座に終了

# カラー出力
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# ロギング関数
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 使い方を表示
show_usage() {
    echo "使い方: $0 [環境] [ステップ]"
    echo ""
    echo "環境:"
    echo "  dev  - 開発環境"
    echo "  prod - 本番環境"
    echo ""
    echo "ステップ:"
    echo "  setup    - 初期セットアップ（AWSリソースの作成）"
    echo "  build    - Dockerイメージのビルド"
    echo "  push     - ECRへのプッシュ"
    echo "  deploy   - ECSへのデプロイ"
    echo "  all      - すべてのステップを実行"
    echo ""
    echo "例:"
    echo "  $0 dev all"
    echo "  $0 prod deploy"
}

# 引数のチェック
if [ $# -lt 2 ]; then
    show_usage
    exit 1
fi

ENVIRONMENT=$1
STEP=$2

# 環境の検証
if [ "$ENVIRONMENT" != "dev" ] && [ "$ENVIRONMENT" != "prod" ]; then
    log_error "無効な環境: $ENVIRONMENT"
    show_usage
    exit 1
fi

# ステップの検証
if [ "$STEP" != "setup" ] && [ "$STEP" != "build" ] && [ "$STEP" != "push" ] && [ "$STEP" != "deploy" ] && [ "$STEP" != "all" ]; then
    log_error "無効なステップ: $STEP"
    show_usage
    exit 1
fi

log_info "=== Letteral AWSデプロイスクリプト ==="
log_info "環境: $ENVIRONMENT"
log_info "ステップ: $STEP"
echo ""

# 設定ファイルの読み込み
CONFIG_FILE=".aws-deploy-config-${ENVIRONMENT}.env"

if [ ! -f "$CONFIG_FILE" ]; then
    log_warning "設定ファイルが見つかりません: $CONFIG_FILE"
    log_info "設定ファイルを作成しています..."

    cat > "$CONFIG_FILE" << EOF
# AWS設定
AWS_REGION=ap-northeast-1
AWS_ACCOUNT_ID=123456789012

# ECR設定
ECR_REPOSITORY_NAME=letteral-backend

# ECS設定
ECS_CLUSTER_NAME=letteral-${ENVIRONMENT}-cluster
ECS_SERVICE_NAME=letteral-backend-service
ECS_TASK_FAMILY=letteral-backend-task

# RDS設定
RDS_ENDPOINT=letteral-${ENVIRONMENT}-db.xxxxxxxxxxxx.${AWS_REGION}.rds.amazonaws.com
RDS_PORT=3306
RDS_DATABASE=letteral_${ENVIRONMENT}
RDS_USERNAME=admin
RDS_PASSWORD=CHANGE_ME

# アプリケーション設定
JWT_SECRET=CHANGE_ME
CORS_ALLOWED_ORIGINS=http://localhost:3000

# タスク設定
TASK_CPU=1024
TASK_MEMORY=2048
DESIRED_COUNT=2
EOF

    log_warning "設定ファイルを作成しました: $CONFIG_FILE"
    log_warning "このファイルを編集して、正しい値を設定してください。"
    exit 1
fi

# 設定ファイルの読み込み
source "$CONFIG_FILE"

log_info "設定ファイルを読み込みました: $CONFIG_FILE"
echo ""

# AWS CLIの確認
check_aws_cli() {
    log_info "AWS CLIの確認..."

    if ! command -v aws &> /dev/null; then
        log_error "AWS CLIがインストールされていません"
        log_info "インストール方法: https://aws.amazon.com/cli/"
        exit 1
    fi

    log_success "AWS CLI: $(aws --version)"

    # AWS認証情報の確認
    if ! aws sts get-caller-identity &> /dev/null; then
        log_error "AWS認証情報が設定されていません"
        log_info "設定方法: aws configure"
        exit 1
    fi

    log_success "AWS認証: OK"
    aws sts get-caller-identity
    echo ""
}

# Dockerの確認
check_docker() {
    log_info "Dockerの確認..."

    if ! command -v docker &> /dev/null; then
        log_error "Dockerがインストールされていません"
        log_info "インストール方法: https://www.docker.com/get-started"
        exit 1
    fi

    log_success "Docker: $(docker --version)"

    # Dockerが起動しているか確認
    if ! docker info &> /dev/null; then
        log_error "Dockerが起動していません"
        log_info "Docker Desktopを起動してください"
        exit 1
    fi

    log_success "Docker: 起動中"
    echo ""
}

# ECRリポジトリの作成
setup_ecr() {
    log_info "ECRリポジトリのセットアップ..."

    # リポジトリが存在するか確認
    if aws ecr describe-repositories --repository-names "$ECR_REPOSITORY_NAME" --region "$AWS_REGION" &> /dev/null; then
        log_success "ECRリポジトリは既に存在します: $ECR_REPOSITORY_NAME"
    else
        log_info "ECRリポジトリを作成しています..."
        aws ecr create-repository \
            --repository-name "$ECR_REPOSITORY_NAME" \
            --region "$AWS_REGION" \
            --image-scanning-configuration scanOnPush=true \
            --encryption-configuration encryptionType=AES256

        log_success "ECRリポジトリを作成しました: $ECR_REPOSITORY_NAME"
    fi

    echo ""
}

# ECSクラスターの作成
setup_ecs_cluster() {
    log_info "ECSクラスターのセットアップ..."

    # クラスターが存在するか確認
    if aws ecs describe-clusters --clusters "$ECS_CLUSTER_NAME" --region "$AWS_REGION" | grep -q "ACTIVE"; then
        log_success "ECSクラスターは既に存在します: $ECS_CLUSTER_NAME"
    else
        log_info "ECSクラスターを作成しています..."
        aws ecs create-cluster \
            --cluster-name "$ECS_CLUSTER_NAME" \
            --region "$AWS_REGION"

        log_success "ECSクラスターを作成しました: $ECS_CLUSTER_NAME"
    fi

    echo ""
}

# CloudWatch Logsグループの作成
setup_cloudwatch_logs() {
    log_info "CloudWatch Logsグループのセットアップ..."

    LOG_GROUP="/ecs/letteral-backend"

    # ロググループが存在するか確認
    if aws logs describe-log-groups --log-group-name-prefix "$LOG_GROUP" --region "$AWS_REGION" | grep -q "$LOG_GROUP"; then
        log_success "CloudWatch Logsグループは既に存在します: $LOG_GROUP"
    else
        log_info "CloudWatch Logsグループを作成しています..."
        aws logs create-log-group \
            --log-group-name "$LOG_GROUP" \
            --region "$AWS_REGION"

        log_success "CloudWatch Logsグループを作成しました: $LOG_GROUP"
    fi

    echo ""
}

# Dockerイメージのビルド
build_docker_image() {
    log_info "Dockerイメージをビルドしています..."

    cd backend

    docker build \
        -t "$ECR_REPOSITORY_NAME:latest" \
        -t "$ECR_REPOSITORY_NAME:$(git rev-parse --short HEAD)" \
        -f Dockerfile.prod \
        .

    cd ..

    log_success "Dockerイメージのビルドが完了しました"
    echo ""
}

# ECRへのプッシュ
push_to_ecr() {
    log_info "ECRにログインしています..."

    # ECRにログイン
    aws ecr get-login-password --region "$AWS_REGION" | \
        docker login --username AWS --password-stdin \
        "${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com"

    log_success "ECRにログインしました"

    # イメージにタグ付け
    ECR_URI="${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com/${ECR_REPOSITORY_NAME}"

    log_info "イメージにタグを付けています..."
    docker tag "$ECR_REPOSITORY_NAME:latest" "$ECR_URI:latest"
    docker tag "$ECR_REPOSITORY_NAME:$(git rev-parse --short HEAD)" "$ECR_URI:$(git rev-parse --short HEAD)"

    # ECRにプッシュ
    log_info "ECRにプッシュしています..."
    docker push "$ECR_URI:latest"
    docker push "$ECR_URI:$(git rev-parse --short HEAD)"

    log_success "ECRへのプッシュが完了しました"
    echo ""
}

# ECSタスク定義の更新
update_task_definition() {
    log_info "ECSタスク定義を更新しています..."

    ECR_URI="${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com/${ECR_REPOSITORY_NAME}"

    # タスク定義JSONの生成
    TASK_DEFINITION=$(cat <<EOF
{
  "family": "${ECS_TASK_FAMILY}",
  "networkMode": "awsvpc",
  "requiresCompatibilities": ["FARGATE"],
  "cpu": "${TASK_CPU}",
  "memory": "${TASK_MEMORY}",
  "executionRoleArn": "arn:aws:iam::${AWS_ACCOUNT_ID}:role/ecsTaskExecutionRole",
  "containerDefinitions": [
    {
      "name": "letteral-backend",
      "image": "${ECR_URI}:latest",
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
          "value": "jdbc:mysql://${RDS_ENDPOINT}:${RDS_PORT}/${RDS_DATABASE}?useSSL=false&serverTimezone=Asia/Tokyo"
        },
        {
          "name": "SPRING_DATASOURCE_USERNAME",
          "value": "${RDS_USERNAME}"
        },
        {
          "name": "SPRING_DATASOURCE_PASSWORD",
          "value": "${RDS_PASSWORD}"
        },
        {
          "name": "JWT_SECRET",
          "value": "${JWT_SECRET}"
        },
        {
          "name": "CORS_ALLOWED_ORIGINS",
          "value": "${CORS_ALLOWED_ORIGINS}"
        }
      ],
      "logConfiguration": {
        "logDriver": "awslogs",
        "options": {
          "awslogs-group": "/ecs/letteral-backend",
          "awslogs-region": "${AWS_REGION}",
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
EOF
)

    # タスク定義を登録
    echo "$TASK_DEFINITION" > /tmp/task-definition.json
    aws ecs register-task-definition \
        --cli-input-json file:///tmp/task-definition.json \
        --region "$AWS_REGION" > /dev/null

    rm /tmp/task-definition.json

    log_success "ECSタスク定義を更新しました"
    echo ""
}

# ECSサービスの更新
update_ecs_service() {
    log_info "ECSサービスを更新しています..."

    # サービスを強制的に新しいデプロイメントで更新
    aws ecs update-service \
        --cluster "$ECS_CLUSTER_NAME" \
        --service "$ECS_SERVICE_NAME" \
        --task-definition "$ECS_TASK_FAMILY" \
        --desired-count "$DESIRED_COUNT" \
        --force-new-deployment \
        --region "$AWS_REGION" > /dev/null

    log_success "ECSサービスを更新しました"

    log_info "デプロイメントが完了するまで待機しています..."
    aws ecs wait services-stable \
        --cluster "$ECS_CLUSTER_NAME" \
        --services "$ECS_SERVICE_NAME" \
        --region "$AWS_REGION"

    log_success "デプロイメントが完了しました！"
    echo ""
}

# デプロイメント情報の表示
show_deployment_info() {
    log_info "=== デプロイメント情報 ==="

    # サービスの情報を取得
    SERVICE_INFO=$(aws ecs describe-services \
        --cluster "$ECS_CLUSTER_NAME" \
        --services "$ECS_SERVICE_NAME" \
        --region "$AWS_REGION")

    RUNNING_COUNT=$(echo "$SERVICE_INFO" | jq -r '.services[0].runningCount')
    DESIRED_COUNT=$(echo "$SERVICE_INFO" | jq -r '.services[0].desiredCount')

    echo "クラスター: $ECS_CLUSTER_NAME"
    echo "サービス: $ECS_SERVICE_NAME"
    echo "実行中のタスク数: $RUNNING_COUNT / $DESIRED_COUNT"
    echo ""

    log_info "最新のログを確認するには:"
    echo "aws logs tail /ecs/letteral-backend --follow --region $AWS_REGION"
    echo ""
}

# メイン処理
main() {
    case $STEP in
        setup)
            check_aws_cli
            check_docker
            setup_ecr
            setup_ecs_cluster
            setup_cloudwatch_logs
            log_success "セットアップが完了しました！"
            ;;

        build)
            check_docker
            build_docker_image
            log_success "ビルドが完了しました！"
            ;;

        push)
            check_aws_cli
            check_docker
            push_to_ecr
            log_success "プッシュが完了しました！"
            ;;

        deploy)
            check_aws_cli
            update_task_definition
            update_ecs_service
            show_deployment_info
            log_success "デプロイが完了しました！"
            ;;

        all)
            check_aws_cli
            check_docker
            setup_ecr
            setup_ecs_cluster
            setup_cloudwatch_logs
            build_docker_image
            push_to_ecr
            update_task_definition
            update_ecs_service
            show_deployment_info
            log_success "すべての処理が完了しました！"
            ;;
    esac
}

# スクリプト実行
main

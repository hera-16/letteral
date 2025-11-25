###############################################################################
# ECS Service Linked Role セットアップスクリプト
#
# このスクリプトは、ECSクラスター作成に必要なサービスリンクロールを
# 作成します。
#
# 使い方:
#   .\setup-ecs-service-role.ps1
###############################################################################

$ErrorActionPreference = "Stop"

# ロギング関数
function Write-Info {
    param([string]$Message)
    Write-Host "[INFO] $Message" -ForegroundColor Blue
}

function Write-Success {
    param([string]$Message)
    Write-Host "[SUCCESS] $Message" -ForegroundColor Green
}

function Write-Warning {
    param([string]$Message)
    Write-Host "[WARNING] $Message" -ForegroundColor Yellow
}

function Write-Error {
    param([string]$Message)
    Write-Host "[ERROR] $Message" -ForegroundColor Red
}

Write-Info "=== ECS Service Linked Role セットアップ ==="
Write-Host ""

# AWS CLIの確認
Write-Info "AWS CLIの確認..."
try {
    $awsVersion = aws --version 2>&1
    Write-Success "AWS CLI: $awsVersion"
}
catch {
    Write-Error "AWS CLIがインストールされていません"
    Write-Info "インストール方法: https://aws.amazon.com/cli/"
    exit 1
}

# AWS認証情報の確認
Write-Info "AWS認証情報の確認..."
try {
    $identity = aws sts get-caller-identity | ConvertFrom-Json
    Write-Success "AWS認証: OK"
    Write-Host "Account: $($identity.Account)"
    Write-Host "UserId: $($identity.UserId)"
    Write-Host ""
}
catch {
    Write-Error "AWS認証情報が設定されていません"
    Write-Info "設定方法: aws configure"
    exit 1
}

# ECSサービスリンクロールの確認と作成
Write-Info "ECSサービスリンクロールの確認..."

try {
    # 既存のロールを確認
    $roles = aws iam list-roles --query "Roles[?RoleName=='AWSServiceRoleForECS']" | ConvertFrom-Json

    if ($roles.Count -gt 0) {
        Write-Success "ECSサービスリンクロールは既に存在します"
        Write-Host "Role ARN: $($roles[0].Arn)"
    }
    else {
        Write-Info "ECSサービスリンクロールを作成しています..."

        # サービスリンクロールを作成
        $result = aws iam create-service-linked-role --aws-service-name ecs.amazonaws.com | ConvertFrom-Json

        Write-Success "ECSサービスリンクロールを作成しました"
        Write-Host "Role ARN: $($result.Role.Arn)"
    }
}
catch {
    $errorMessage = $_.Exception.Message

    if ($errorMessage -like "*already exists*" -or $errorMessage -like "*has been taken*") {
        Write-Success "ECSサービスリンクロールは既に存在します"
    }
    else {
        Write-Error "エラーが発生しました: $errorMessage"
        exit 1
    }
}

Write-Host ""

# ECSタスク実行ロールの確認と作成
Write-Info "ECSタスク実行ロールの確認..."

$executionRoleName = "ecsTaskExecutionRole"

try {
    # 既存のロールを確認
    aws iam get-role --role-name $executionRoleName | Out-Null
    Write-Success "ECSタスク実行ロールは既に存在します"
}
catch {
    Write-Info "ECSタスク実行ロールを作成しています..."

    # 信頼ポリシーの定義
    $trustPolicy = @{
        Version = "2012-10-17"
        Statement = @(
            @{
                Effect = "Allow"
                Principal = @{
                    Service = "ecs-tasks.amazonaws.com"
                }
                Action = "sts:AssumeRole"
            }
        )
    } | ConvertTo-Json -Depth 10 -Compress

    # ロールを作成
    aws iam create-role `
        --role-name $executionRoleName `
        --assume-role-policy-document $trustPolicy `
        --description "ECS Task Execution Role for Letteral" | Out-Null

    # 必要なポリシーをアタッチ
    aws iam attach-role-policy `
        --role-name $executionRoleName `
        --policy-arn "arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy" | Out-Null

    # Secrets Managerへのアクセスポリシーを追加
    $secretsPolicy = @{
        Version = "2012-10-17"
        Statement = @(
            @{
                Effect = "Allow"
                Action = @(
                    "secretsmanager:GetSecretValue"
                )
                Resource = @(
                    "arn:aws:secretsmanager:*:*:secret:letteral/*"
                )
            },
            @{
                Effect = "Allow"
                Action = @(
                    "kms:Decrypt"
                )
                Resource = "*"
            }
        )
    } | ConvertTo-Json -Depth 10 -Compress

    aws iam put-role-policy `
        --role-name $executionRoleName `
        --policy-name "SecretsManagerAccess" `
        --policy-document $secretsPolicy | Out-Null

    Write-Success "ECSタスク実行ロールを作成しました"
}

Write-Host ""
Write-Success "=== セットアップが完了しました ==="
Write-Info "これで ECS クラスターを作成できます"
Write-Host ""
Write-Info "次のステップ:"
Write-Host "1. .\deploy-to-aws.ps1 -Environment prod -Step setup"
Write-Host ""

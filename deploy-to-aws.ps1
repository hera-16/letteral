###############################################################################
# Letteral AWS デプロイ自動化スクリプト (PowerShell版)
#
# このスクリプトは、LetteralプラットフォームをAWSにデプロイするプロセスを
# 自動化します。
#
# 使い方:
#   .\deploy-to-aws.ps1 -Environment <環境> -Step <ステップ>
#
# 環境:
#   dev  - 開発環境
#   prod - 本番環境
#
# ステップ:
#   setup    - 初期セットアップ(AWSリソースの作成)
#   build    - Dockerイメージのビルド
#   push     - ECRへのプッシュ
#   deploy   - ECSへのデプロイ
#   all      - すべてのステップを実行
#
# 例:
#   .\deploy-to-aws.ps1 -Environment dev -Step all
#   .\deploy-to-aws.ps1 -Environment prod -Step deploy
###############################################################################

param(
    [Parameter(Mandatory=$true)]
    [ValidateSet("dev", "prod")]
    [string]$Environment,

    [Parameter(Mandatory=$true)]
    [ValidateSet("setup", "build", "push", "deploy", "all")]
    [string]$Step
)

# エラーが発生したら停止
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

Write-Info "=== Letteral AWSデプロイスクリプト ==="
Write-Info "環境: $Environment"
Write-Info "ステップ: $Step"
Write-Host ""

# 設定ファイルの読み込み
$ConfigFile = ".aws-deploy-config-$Environment.env"

if (-not (Test-Path $ConfigFile)) {
    Write-Warning "設定ファイルが見つかりません: $ConfigFile"
    Write-Info "設定ファイルを作成しています..."

    $ConfigContent = @"
# AWS設定
AWS_REGION=ap-northeast-1
AWS_ACCOUNT_ID=123456789012

# ECR設定
ECR_REPOSITORY_NAME=letteral-backend

# ECS設定
ECS_CLUSTER_NAME=letteral-$Environment-cluster
ECS_SERVICE_NAME=letteral-backend-service
ECS_TASK_FAMILY=letteral-backend-task

# RDS設定
RDS_ENDPOINT=letteral-$Environment-db.xxxxxxxxxxxx.ap-northeast-1.rds.amazonaws.com
RDS_PORT=3306
RDS_DATABASE=letteral_$Environment
RDS_USERNAME=admin
RDS_PASSWORD=CHANGE_ME

# アプリケーション設定
JWT_SECRET=CHANGE_ME
CORS_ALLOWED_ORIGINS=http://localhost:3000

# タスク設定
TASK_CPU=1024
TASK_MEMORY=2048
DESIRED_COUNT=2
"@

    Set-Content -Path $ConfigFile -Value $ConfigContent
    Write-Warning "設定ファイルを作成しました: $ConfigFile"
    Write-Warning "このファイルを編集して、正しい値を設定してください。"
    exit 1
}

# 設定ファイルの読み込み
$Config = @{}
Get-Content $ConfigFile | ForEach-Object {
    if ($_ -match '^([^#][^=]+)=(.*)$') {
        $Config[$matches[1].Trim()] = $matches[2].Trim()
    }
}

Write-Info "設定ファイルを読み込みました: $ConfigFile"
Write-Host ""

# AWS CLIの確認
function Test-AwsCli {
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
}

# Dockerの確認
function Test-Docker {
    Write-Info "Dockerの確認..."

    try {
        $dockerVersion = docker --version
        Write-Success "Docker: $dockerVersion"
    }
    catch {
        Write-Error "Dockerがインストールされていません"
        Write-Info "インストール方法: https://www.docker.com/get-started"
        exit 1
    }

    # Dockerが起動しているか確認
    try {
        docker info | Out-Null
        Write-Success "Docker: 起動中"
        Write-Host ""
    }
    catch {
        Write-Error "Dockerが起動していません"
        Write-Info "Docker Desktopを起動してください"
        exit 1
    }
}

# ECSサービスリンクロールの確認
function Test-ECSServiceRole {
    Write-Info "ECSサービスリンクロールの確認..."

    try {
        $roles = aws iam list-roles --query "Roles[?RoleName=='AWSServiceRoleForECS']" | ConvertFrom-Json

        if ($roles.Count -gt 0) {
            Write-Success "ECSサービスリンクロールが存在します"
            return $true
        }
        else {
            Write-Warning "ECSサービスリンクロールが存在しません"
            return $false
        }
    }
    catch {
        Write-Warning "ECSサービスリンクロールの確認中にエラーが発生しました"
        return $false
    }
}

# ECSサービスリンクロールの作成
function Setup-ECSServiceRole {
    Write-Info "ECSサービスリンクロールを作成しています..."

    try {
        $result = aws iam create-service-linked-role --aws-service-name ecs.amazonaws.com | ConvertFrom-Json
        Write-Success "ECSサービスリンクロールを作成しました"
        Write-Host "Role ARN: $($result.Role.Arn)"
    }
    catch {
        $errorMessage = $_.Exception.Message
        if ($errorMessage -like "*already exists*" -or $errorMessage -like "*has been taken*") {
            Write-Success "ECSサービスリンクロールは既に存在します"
        }
        else {
            throw $_
        }
    }

    Write-Host ""
}

# ECSタスク実行ロールの確認と作成
function Setup-ECSTaskExecutionRole {
    Write-Info "ECSタスク実行ロールの確認..."

    $executionRoleName = "ecsTaskExecutionRole"
    $accountId = $Config['AWS_ACCOUNT_ID']

    try {
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

        # CloudWatch Logsへのアクセスポリシーを追加
        $logsPolicy = @{
            Version = "2012-10-17"
            Statement = @(
                @{
                    Effect = "Allow"
                    Action = @(
                        "logs:CreateLogGroup",
                        "logs:CreateLogStream",
                        "logs:PutLogEvents"
                    )
                    Resource = "arn:aws:logs:*:${accountId}:log-group:/ecs/*"
                }
            )
        } | ConvertTo-Json -Depth 10 -Compress

        aws iam put-role-policy `
            --role-name $executionRoleName `
            --policy-name "CloudWatchLogsAccess" `
            --policy-document $logsPolicy | Out-Null

        Write-Success "ECSタスク実行ロールを作成しました"
    }

    Write-Host ""
}

# ECRリポジトリの作成
function Setup-ECR {
    Write-Info "ECRリポジトリのセットアップ..."

    $repoName = $Config['ECR_REPOSITORY_NAME']
    $region = $Config['AWS_REGION']

    # リポジトリが存在するか確認
    try {
        aws ecr describe-repositories --repository-names $repoName --region $region | Out-Null
        Write-Success "ECRリポジトリは既に存在します: $repoName"
    }
    catch {
        Write-Info "ECRリポジトリを作成しています..."
        aws ecr create-repository `
            --repository-name $repoName `
            --region $region `
            --image-scanning-configuration scanOnPush=true `
            --encryption-configuration encryptionType=AES256 | Out-Null

        Write-Success "ECRリポジトリを作成しました: $repoName"
    }

    Write-Host ""
}

# ECSクラスターの作成
function Setup-ECSCluster {
    Write-Info "ECSクラスターのセットアップ..."

    $clusterName = $Config['ECS_CLUSTER_NAME']
    $region = $Config['AWS_REGION']

    # クラスターが存在するか確認
    try {
        $clusters = aws ecs describe-clusters --clusters $clusterName --region $region | ConvertFrom-Json
        if ($clusters.clusters[0].status -eq "ACTIVE") {
            Write-Success "ECSクラスターは既に存在します: $clusterName"
        }
        else {
            throw "Cluster not active"
        }
    }
    catch {
        Write-Info "ECSクラスターを作成しています..."
        aws ecs create-cluster `
            --cluster-name $clusterName `
            --region $region | Out-Null

        Write-Success "ECSクラスターを作成しました: $clusterName"
    }

    Write-Host ""
}

# CloudWatch Logsグループの作成
function Setup-CloudWatchLogs {
    Write-Info "CloudWatch Logsグループのセットアップ..."

    $logGroup = "/ecs/letteral-backend"
    $region = $Config['AWS_REGION']

    # ロググループが存在するか確認
    try {
        $logGroups = aws logs describe-log-groups --log-group-name-prefix $logGroup --region $region | ConvertFrom-Json
        if ($logGroups.logGroups.Count -gt 0) {
            Write-Success "CloudWatch Logsグループは既に存在します: $logGroup"
        }
        else {
            throw "Log group not found"
        }
    }
    catch {
        Write-Info "CloudWatch Logsグループを作成しています..."
        aws logs create-log-group `
            --log-group-name $logGroup `
            --region $region | Out-Null

        Write-Success "CloudWatch Logsグループを作成しました: $logGroup"
    }

    Write-Host ""
}

# Dockerイメージのビルド
function Build-DockerImage {
    Write-Info "Dockerイメージをビルドしています..."

    $repoName = $Config['ECR_REPOSITORY_NAME']

    Push-Location backend

    try {
        # Git commit hashを取得（存在する場合）
        try {
            $gitHash = git rev-parse --short HEAD 2>$null
        }
        catch {
            $gitHash = "latest"
        }

        docker build `
            -t "${repoName}:latest" `
            -t "${repoName}:${gitHash}" `
            -f Dockerfile.prod `
            .

        Write-Success "Dockerイメージのビルドが完了しました"
    }
    finally {
        Pop-Location
    }

    Write-Host ""
}

# ECRへのプッシュ
function Push-ToECR {
    Write-Info "ECRにログインしています..."

    $accountId = $Config['AWS_ACCOUNT_ID']
    $region = $Config['AWS_REGION']
    $repoName = $Config['ECR_REPOSITORY_NAME']

    # ECRにログイン
    $password = aws ecr get-login-password --region $region
    $password | docker login --username AWS --password-stdin "${accountId}.dkr.ecr.${region}.amazonaws.com"

    Write-Success "ECRにログインしました"

    # イメージにタグ付け
    $ecrUri = "${accountId}.dkr.ecr.${region}.amazonaws.com/${repoName}"

    # Git commit hashを取得
    try {
        $gitHash = git rev-parse --short HEAD 2>$null
    }
    catch {
        $gitHash = "latest"
    }

    Write-Info "イメージにタグを付けています..."
    docker tag "${repoName}:latest" "${ecrUri}:latest"
    docker tag "${repoName}:${gitHash}" "${ecrUri}:${gitHash}"

    # ECRにプッシュ
    Write-Info "ECRにプッシュしています..."
    docker push "${ecrUri}:latest"
    docker push "${ecrUri}:${gitHash}"

    Write-Success "ECRへのプッシュが完了しました"
    Write-Host ""
}

# ECSタスク定義の更新
function Update-TaskDefinition {
    Write-Info "ECSタスク定義を更新しています..."

    $accountId = $Config['AWS_ACCOUNT_ID']
    $region = $Config['AWS_REGION']
    $repoName = $Config['ECR_REPOSITORY_NAME']
    $taskFamily = $Config['ECS_TASK_FAMILY']
    $taskCpu = $Config['TASK_CPU']
    $taskMemory = $Config['TASK_MEMORY']

    $ecrUri = "${accountId}.dkr.ecr.${region}.amazonaws.com/${repoName}"

    # タスク定義JSONの生成
    $taskDefinition = @{
        family = $taskFamily
        networkMode = "awsvpc"
        requiresCompatibilities = @("FARGATE")
        cpu = $taskCpu
        memory = $taskMemory
        executionRoleArn = "arn:aws:iam::${accountId}:role/ecsTaskExecutionRole"
        containerDefinitions = @(
            @{
                name = "letteral-backend"
                image = "${ecrUri}:latest"
                portMappings = @(
                    @{
                        containerPort = 8080
                        protocol = "tcp"
                    }
                )
                environment = @(
                    @{ name = "SPRING_PROFILES_ACTIVE"; value = "prod" }
                    @{ name = "SPRING_DATASOURCE_URL"; value = "jdbc:mysql://$($Config['RDS_ENDPOINT']):$($Config['RDS_PORT'])/$($Config['RDS_DATABASE'])?useSSL=false&serverTimezone=Asia/Tokyo" }
                    @{ name = "SPRING_DATASOURCE_USERNAME"; value = $Config['RDS_USERNAME'] }
                    @{ name = "SPRING_DATASOURCE_PASSWORD"; value = $Config['RDS_PASSWORD'] }
                    @{ name = "JWT_SECRET"; value = $Config['JWT_SECRET'] }
                    @{ name = "CORS_ALLOWED_ORIGINS"; value = $Config['CORS_ALLOWED_ORIGINS'] }
                )
                logConfiguration = @{
                    logDriver = "awslogs"
                    options = @{
                        "awslogs-group" = "/ecs/letteral-backend"
                        "awslogs-region" = $region
                        "awslogs-stream-prefix" = "ecs"
                    }
                }
                healthCheck = @{
                    command = @("CMD-SHELL", "curl -f http://localhost:8080/actuator/health || exit 1")
                    interval = 30
                    timeout = 5
                    retries = 3
                    startPeriod = 60
                }
            }
        )
    }

    # タスク定義を一時ファイルに保存
    $taskDefFile = "$env:TEMP\task-definition.json"
    $taskDefinition | ConvertTo-Json -Depth 10 | Set-Content -Path $taskDefFile

    # タスク定義を登録
    aws ecs register-task-definition `
        --cli-input-json "file://$taskDefFile" `
        --region $region | Out-Null

    Remove-Item -Path $taskDefFile

    Write-Success "ECSタスク定義を更新しました"
    Write-Host ""
}

# ECSサービスの更新
function Update-ECSService {
    Write-Info "ECSサービスを更新しています..."

    $clusterName = $Config['ECS_CLUSTER_NAME']
    $serviceName = $Config['ECS_SERVICE_NAME']
    $taskFamily = $Config['ECS_TASK_FAMILY']
    $desiredCount = $Config['DESIRED_COUNT']
    $region = $Config['AWS_REGION']

    # サービスを強制的に新しいデプロイメントで更新
    aws ecs update-service `
        --cluster $clusterName `
        --service $serviceName `
        --task-definition $taskFamily `
        --desired-count $desiredCount `
        --force-new-deployment `
        --region $region | Out-Null

    Write-Success "ECSサービスを更新しました"

    Write-Info "デプロイメントが完了するまで待機しています..."
    aws ecs wait services-stable `
        --cluster $clusterName `
        --services $serviceName `
        --region $region

    Write-Success "デプロイメントが完了しました!"
    Write-Host ""
}

# デプロイメント情報の表示
function Show-DeploymentInfo {
    Write-Info "=== デプロイメント情報 ==="

    $clusterName = $Config['ECS_CLUSTER_NAME']
    $serviceName = $Config['ECS_SERVICE_NAME']
    $region = $Config['AWS_REGION']

    # サービスの情報を取得
    $serviceInfo = aws ecs describe-services `
        --cluster $clusterName `
        --services $serviceName `
        --region $region | ConvertFrom-Json

    $runningCount = $serviceInfo.services[0].runningCount
    $desiredCount = $serviceInfo.services[0].desiredCount

    Write-Host "クラスター: $clusterName"
    Write-Host "サービス: $serviceName"
    Write-Host "実行中のタスク数: $runningCount / $desiredCount"
    Write-Host ""

    Write-Info "最新のログを確認するには:"
    Write-Host "aws logs tail /ecs/letteral-backend --follow --region $region"
    Write-Host ""
}

# メイン処理
try {
    switch ($Step) {
        "setup" {
            Test-AwsCli
            Test-Docker

            # ECSサービスリンクロールの確認と作成
            if (-not (Test-ECSServiceRole)) {
                Setup-ECSServiceRole
            }

            # ECSタスク実行ロールの確認と作成
            Setup-ECSTaskExecutionRole

            Setup-ECR
            Setup-ECSCluster
            Setup-CloudWatchLogs
            Write-Success "セットアップが完了しました!"
        }

        "build" {
            Test-Docker
            Build-DockerImage
            Write-Success "ビルドが完了しました!"
        }

        "push" {
            Test-AwsCli
            Test-Docker
            Push-ToECR
            Write-Success "プッシュが完了しました!"
        }

        "deploy" {
            Test-AwsCli
            Update-TaskDefinition
            Update-ECSService
            Show-DeploymentInfo
            Write-Success "デプロイが完了しました!"
        }

        "all" {
            Test-AwsCli
            Test-Docker

            # ECSサービスリンクロールの確認と作成
            if (-not (Test-ECSServiceRole)) {
                Setup-ECSServiceRole
            }

            # ECSタスク実行ロールの確認と作成
            Setup-ECSTaskExecutionRole

            Setup-ECR
            Setup-ECSCluster
            Setup-CloudWatchLogs
            Build-DockerImage
            Push-ToECR
            Update-TaskDefinition
            Update-ECSService
            Show-DeploymentInfo
            Write-Success "すべての処理が完了しました!"
        }
    }
}
catch {
    Write-Error "エラーが発生しました: $_"
    exit 1
}

# Letteral AWS インフラ構成設計書

## 目次
1. [概要](#概要)
2. [アーキテクチャ図](#アーキテクチャ図)
3. [AWSサービス構成](#awsサービス構成)
4. [ネットワーク設計](#ネットワーク設計)
5. [セキュリティ設計](#セキュリティ設計)
6. [スケーリング設計](#スケーリング設計)
7. [コスト見積もり](#コスト見積もり)
8. [災害復旧・バックアップ](#災害復旧バックアップ)

---

## 概要

### システム名
**Letteral** - 企業向け匿名進捗共有プラットフォーム

### インフラ要件
- **可用性**: 99.9% (月間ダウンタイム < 43分)
- **スケーラビリティ**: 1,000 → 10,000 ユーザーまで対応
- **セキュリティ**: 企業データ保護、匿名性保証
- **パフォーマンス**: API応答時間 < 200ms
- **リージョン**: ap-northeast-1 (東京)

---

## アーキテクチャ図

```
                                    Internet
                                       ↓
                              [Route 53 DNS]
                                       ↓
                           [CloudFront CDN (SSL)]
                                       ↓
                    ┌──────────────────┴──────────────────┐
                    ↓                                     ↓
              [S3 Bucket]                          [ALB (Public)]
           (Static Assets)                         SSL Termination
                                                          ↓
                                              ┌───────────┴───────────┐
                                              ↓                       ↓
                                    [ECS Service - Backend]  [ECS Service - Worker]
                                    (Auto Scaling 2-10)      (Auto Scaling 1-3)
                                              ↓                       ↓
                                    ┌─────────┴───────────────────────┘
                                    ↓
                        ┌───────────┴───────────┐
                        ↓                       ↓
                  [RDS MySQL]           [ElastiCache Redis]
                 (Multi-AZ)             (Cluster Mode)
                        ↓
                  [S3 Backup]
```

---

## AWSサービス構成

### 1. ネットワーク層

#### VPC (Virtual Private Cloud)
- **CIDR**: 10.0.0.0/16
- **Availability Zones**: 3つ (ap-northeast-1a, 1c, 1d)
- **サブネット構成**:
  - Public Subnet: 10.0.1.0/24, 10.0.2.0/24, 10.0.3.0/24
  - Private Subnet (App): 10.0.11.0/24, 10.0.12.0/24, 10.0.13.0/24
  - Private Subnet (DB): 10.0.21.0/24, 10.0.22.0/24, 10.0.23.0/24

#### Route 53
- **ホストゾーン**: letteral.example.com
- **レコード**:
  - A record → CloudFront Distribution
  - CNAME → API (api.letteral.example.com → ALB)

#### CloudFront
- **ディストリビューション**: グローバル配信
- **オリジン**:
  - S3 (フロントエンド静的ファイル)
  - ALB (APIエンドポイント)
- **キャッシュポリシー**:
  - 静的ファイル: TTL 24時間
  - API: キャッシュなし
- **SSL/TLS**: ACM証明書

### 2. コンピューティング層

#### ECS (Elastic Container Service)
- **起動タイプ**: Fargate (サーバーレス)
- **クラスター**: letteral-prod-cluster

##### Backend Service
- **タスク定義**:
  - CPU: 1 vCPU (1024 units)
  - メモリ: 2 GB
  - コンテナ: letteral-backend:latest (ECRから)
- **タスク数**:
  - 最小: 2
  - 最大: 10
  - 希望: 2
- **Auto Scaling**:
  - ターゲット: CPU使用率 70%
  - メトリクス: リクエスト数/分
- **ヘルスチェック**: /actuator/health

##### Worker Service (非同期処理)
- **タスク定義**:
  - CPU: 0.5 vCPU (512 units)
  - メモリ: 1 GB
- **タスク数**: 1-3
- **処理内容**:
  - メール送信
  - レポート生成
  - データ集計

#### ECR (Elastic Container Registry)
- **リポジトリ**:
  - letteral-backend
  - letteral-worker
- **イメージタグ戦略**:
  - latest
  - {git-commit-sha}
  - v{version}

### 3. データベース層

#### RDS MySQL
- **エンジン**: MySQL 8.0.33
- **インスタンスタイプ**: db.t3.medium (開発), db.r6g.large (本番)
- **Multi-AZ**: 有効 (高可用性)
- **ストレージ**:
  - タイプ: gp3
  - サイズ: 100 GB (初期)
  - 自動拡張: 有効 (最大 500 GB)
- **バックアップ**:
  - 保持期間: 7日間
  - バックアップウィンドウ: 03:00-04:00 JST
  - スナップショット: 日次
- **暗号化**: KMS (AWS管理キー)
- **パラメータグループ**:
  - character_set_server: utf8mb4
  - collation_server: utf8mb4_unicode_ci
  - max_connections: 200

#### ElastiCache Redis
- **エンジン**: Redis 7.0
- **ノードタイプ**: cache.t3.micro (開発), cache.r6g.large (本番)
- **クラスターモード**: 有効
- **レプリケーション**: 2ノード (プライマリ + レプリカ)
- **自動フェイルオーバー**: 有効
- **用途**:
  - セッション管理
  - APIレスポンスキャッシュ
  - レート制限カウンタ

### 4. ストレージ層

#### S3
##### letteral-uploads-prod
- **用途**: ユーザーアップロードファイル (アバター等)
- **バージョニング**: 有効
- **暗号化**: SSE-S3
- **ライフサイクル**:
  - 90日後 → Glacier (低頻度アクセス)
  - 365日後 → 削除
- **CORS設定**: CloudFrontオリジンのみ

##### letteral-static-assets
- **用途**: フロントエンド静的ファイル
- **CloudFront連携**: 有効
- **バージョニング**: 有効

##### letteral-backups
- **用途**: RDSバックアップ、ログアーカイブ
- **暗号化**: SSE-KMS
- **ライフサイクル**:
  - 30日後 → Glacier
  - 365日後 → Deep Archive

### 5. ロードバランサー

#### ALB (Application Load Balancer)
- **スキーム**: Internet-facing
- **リスナー**:
  - HTTP:80 → HTTPS:443 リダイレクト
  - HTTPS:443 → ECS Backend Service
- **SSL証明書**: ACM
- **ターゲットグループ**:
  - ヘルスチェックパス: /actuator/health
  - ヘルスチェック間隔: 30秒
  - 異常判定: 2回連続失敗
- **スティッキーセッション**: 有効 (Cookie-based)

### 6. 監視・ログ

#### CloudWatch
- **ログ**:
  - ECS タスクログ: /aws/ecs/letteral-backend
  - RDS ログ: /aws/rds/letteral-prod
  - ALB アクセスログ: S3保存
- **メトリクス**:
  - CPU/メモリ使用率
  - リクエスト数/分
  - エラー率
  - データベース接続数
- **アラーム**:
  - CPU使用率 > 80%
  - エラー率 > 5%
  - RDS接続数 > 150
  - ディスク使用率 > 80%

#### X-Ray
- **分散トレーシング**: 有効
- **APIレスポンス分析**: 有効

### 7. セキュリティ

#### IAM (Identity and Access Management)
##### Roles
- **ECSTaskExecutionRole**: ECR pull, CloudWatch Logs書き込み
- **ECSTaskRole**: S3アクセス, RDS接続, SES送信
- **LambdaExecutionRole**: ログ記録, VPCアクセス

#### Security Groups
##### ALB-SG
- Inbound: 80, 443 (0.0.0.0/0)
- Outbound: All (ECS-SG)

##### ECS-SG
- Inbound: 8080 (ALB-SG)
- Outbound: 3306 (RDS-SG), 6379 (Redis-SG), 443 (Internet)

##### RDS-SG
- Inbound: 3306 (ECS-SG)
- Outbound: None

##### Redis-SG
- Inbound: 6379 (ECS-SG)
- Outbound: None

#### Secrets Manager
- **シークレット**:
  - letteral/prod/db-credentials
  - letteral/prod/jwt-secret
  - letteral/prod/redis-password
  - letteral/prod/ses-credentials

#### WAF (Web Application Firewall)
- **ルール**:
  - SQLインジェクション防止
  - XSS防止
  - レート制限 (100 req/min/IP)
  - 地理的制限 (日本のみ許可)

### 8. CI/CD

#### CodePipeline
- **ソース**: GitHub (main ブランチ)
- **ビルド**: CodeBuild
- **デプロイ**: ECS (Blue/Green)

#### CodeBuild
- **ビルド環境**: aws/codebuild/standard:7.0
- **ビルドステップ**:
  1. Maven ビルド
  2. Docker イメージビルド
  3. ECR プッシュ
  4. ECS タスク定義更新

---

## ネットワーク設計

### VPC詳細設計

```
VPC: letteral-prod-vpc (10.0.0.0/16)

AZ-1 (ap-northeast-1a):
  - Public Subnet:  10.0.1.0/24  → ALB
  - Private Subnet: 10.0.11.0/24 → ECS Tasks
  - DB Subnet:      10.0.21.0/24 → RDS Primary

AZ-2 (ap-northeast-1c):
  - Public Subnet:  10.0.2.0/24  → ALB
  - Private Subnet: 10.0.12.0/24 → ECS Tasks
  - DB Subnet:      10.0.22.0/24 → RDS Standby

AZ-3 (ap-northeast-1d):
  - Public Subnet:  10.0.3.0/24  → ALB
  - Private Subnet: 10.0.13.0/24 → ECS Tasks
  - DB Subnet:      10.0.23.0/24 → ElastiCache
```

### ルートテーブル

#### Public Route Table
- 0.0.0.0/0 → Internet Gateway
- 10.0.0.0/16 → Local

#### Private Route Table
- 0.0.0.0/0 → NAT Gateway
- 10.0.0.0/16 → Local

#### DB Route Table
- 10.0.0.0/16 → Local (インターネットアクセス不要)

---

## セキュリティ設計

### データ保護
1. **転送時暗号化**:
   - HTTPS/TLS 1.2以上
   - RDS: SSL接続必須
2. **保存時暗号化**:
   - RDS: KMS暗号化
   - S3: SSE-S3 / SSE-KMS
   - EBS: KMS暗号化

### アクセス制御
1. **最小権限の原則**: IAM Role
2. **ネットワーク分離**: VPC, Security Group
3. **認証・認可**: JWT, RBAC

### コンプライアンス
- **個人情報保護**: 匿名化、暗号化
- **監査ログ**: CloudTrail (全API操作記録)
- **脆弱性スキャン**: Amazon Inspector

---

## スケーリング設計

### 水平スケーリング

#### ECS Auto Scaling
- **メトリクス**: CPU使用率, リクエスト数
- **スケールアウト**: CPU > 70% → タスク数 +1
- **スケールイン**: CPU < 30% (5分継続) → タスク数 -1
- **クールダウン**: 300秒

#### RDS
- **Read Replica**: 読み取り負荷分散 (最大 5台)
- **Aurora移行**: 将来的な選択肢

### 垂直スケーリング
- **ECS**: タスクCPU/メモリ増加
- **RDS**: インスタンスタイプ変更 (Blue/Green Deployment)

---

## コスト見積もり

### 月額コスト試算 (東京リージョン)

#### 小規模構成 (〜500ユーザー)
| サービス | スペック | 月額 (USD) |
|---------|---------|-----------|
| ECS Fargate | 2タスク × 1vCPU/2GB | $60 |
| RDS MySQL | db.t3.medium Multi-AZ | $100 |
| ElastiCache | cache.t3.micro | $20 |
| ALB | 100GB/月 | $25 |
| CloudFront | 100GB/月 | $10 |
| S3 | 50GB保存 + 転送 | $5 |
| Route 53 | 1ホストゾーン | $1 |
| CloudWatch | ログ + メトリクス | $10 |
| **合計** | | **$231/月** |

#### 中規模構成 (〜5,000ユーザー)
| サービス | スペック | 月額 (USD) |
|---------|---------|-----------|
| ECS Fargate | 5タスク × 1vCPU/2GB | $150 |
| RDS MySQL | db.r6g.large Multi-AZ | $350 |
| ElastiCache | cache.r6g.large レプリカ | $200 |
| ALB | 1TB/月 | $50 |
| CloudFront | 1TB/月 | $85 |
| S3 | 500GB保存 + 転送 | $25 |
| Route 53 | 1ホストゾーン | $1 |
| CloudWatch | ログ + メトリクス | $50 |
| WAF | 1 ACL + ルール | $30 |
| **合計** | | **$941/月** |

### コスト最適化施策
1. **Reserved Instances**: RDS (1年契約で30%割引)
2. **Savings Plans**: ECS Fargate (1年契約で20%割引)
3. **S3 Lifecycle**: 古いデータをGlacierへ
4. **CloudFront**: キャッシュ最適化でオリジンリクエスト削減

---

## 災害復旧・バックアップ

### RTO/RPO目標
- **RTO (Recovery Time Objective)**: 1時間
- **RPO (Recovery Point Objective)**: 5分

### バックアップ戦略

#### RDS
- **自動バックアップ**: 日次スナップショット (7日保持)
- **手動スナップショット**: デプロイ前に取得
- **Point-in-Time Recovery**: 5分ごと (35日間)
- **クロスリージョンコピー**: us-west-2 (週次)

#### S3
- **バージョニング**: 有効
- **クロスリージョンレプリケーション**: us-west-2

#### ElastiCache
- **自動スナップショット**: 日次 (7日保持)

### 災害復旧手順

#### シナリオ1: AZ障害
- **対応**: Multi-AZ自動フェイルオーバー (RDS, ALB)
- **影響**: なし (自動復旧)

#### シナリオ2: リージョン障害
1. Route 53フェイルオーバー → DRリージョン
2. RDSスナップショットからリストア
3. ECSタスク起動 (事前定義)
4. S3レプリカへ切り替え

#### シナリオ3: データ破損
1. RDS Point-in-Time Recoveryで復元
2. 新しいRDSインスタンス起動
3. アプリケーション接続先変更
4. 検証後、本番切り替え

---

## デプロイメント戦略

### Blue/Green Deployment
1. **Green環境構築**: 新バージョンのECSタスク起動
2. **ヘルスチェック**: 正常性確認
3. **トラフィック切り替え**: ALBターゲットグループ変更
4. **Blue環境維持**: 30分間 (ロールバック用)
5. **Blue環境削除**: 問題なければ削除

### ロールバック手順
- ALBターゲットグループを旧バージョンに戻す (30秒以内)

---

## まとめ

このインフラ構成により、以下を実現します:

- **高可用性**: Multi-AZ構成により99.9%以上の可用性
- **スケーラビリティ**: Auto Scalingにより需要に応じた自動拡張
- **セキュリティ**: 多層防御、暗号化、最小権限の原則
- **コスト効率**: 使用量ベースの課金、Reserved Instances活用
- **運用性**: CloudWatch監視、自動バックアップ、Blue/Greenデプロイ

次のステップ: [Terraformによるインフラコード化](./terraform/) を参照してください。

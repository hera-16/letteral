# AWS初心者向け Letteralデプロイ完全ガイド

> AWSを初めて使う方でも、このガイドに従えばLetteralプラットフォームをAWSにデプロイできます。

## 📋 目次

1. [はじめに](#はじめに)
2. [全体の流れ](#全体の流れ)
3. [事前準備（ローカル環境）](#事前準備ローカル環境)
4. [ステップ1: AWSアカウントの作成](#ステップ1-awsアカウントの作成)
5. [ステップ2: AWS CLIのセットアップ](#ステップ2-aws-cliのセットアップ)
6. [ステップ3: 必要なツールのインストール](#ステップ3-必要なツールのインストール)
7. [ステップ4: データベースのセットアップ（RDS）](#ステップ4-データベースのセットアップrds)
8. [ステップ5: アプリケーションのデプロイ（ECS）](#ステップ5-アプリケーションのデプロイecs)
9. [ステップ6: 動作確認](#ステップ6-動作確認)
10. [トラブルシューティング](#トラブルシューティング)
11. [コスト管理](#コスト管理)

---

## はじめに

### このガイドの対象者

- AWSを初めて使う方
- クラウドデプロイの経験がない方
- Letteralプラットフォームを本番環境にデプロイしたい方

### デプロイ後に実現できること

- インターネット経由でどこからでもアクセス可能
- 自動的にスケールする本番環境
- データベースの自動バックアップ
- SSL/HTTPS対応

### 必要な時間

- **初回**: 約3-4時間
- **2回目以降**: 約1時間

### 必要な費用（月額概算）

- **開発/テスト環境**: 約$100-150/月（約15,000円）
- **小規模本番環境**: 約$200-300/月（約30,000円）
- **中規模本番環境**: 約$500-1,000/月（約75,000円）

💡 **重要**: AWS無料枠を活用すれば、最初の12ヶ月間は一部無料で使えます！

---

## 全体の流れ

```
1. AWSアカウント作成 (30分)
   ↓
2. AWS CLIインストール (15分)
   ↓
3. 必要なツールのインストール (30分)
   ↓
4. データベース作成 (RDS) (45分)
   ↓
5. アプリケーションをコンテナ化 (30分)
   ↓
6. ECSでアプリケーション起動 (60分)
   ↓
7. ドメイン設定とSSL証明書 (30分)
   ↓
8. 動作確認 (15分)
```

---

## 事前準備（ローカル環境）

### 確認事項

以下がローカルで動作していることを確認してください：

```bash
# Javaのバージョン確認
java -version
# → Java 17以上が必要

# Mavenの確認
mvn -version

# Dockerの確認
docker --version

# Node.jsの確認
node --version
# → Node.js 18以上が必要
```

---

## ステップ1: AWSアカウントの作成

### 1.1 AWSアカウントの登録

1. **AWSの公式サイトにアクセス**
   - URL: https://aws.amazon.com/jp/
   - 「無料で始める」ボタンをクリック

2. **アカウント情報の入力**
   - メールアドレス
   - パスワード（英数字・記号を含む強力なもの）
   - AWSアカウント名（例: letteral-production）

3. **連絡先情報の入力**
   - 氏名
   - 電話番号
   - 住所

4. **支払い情報の登録**
   - クレジットカード情報を入力
   - ⚠️ 無料枠を超えた場合のみ課金されます

5. **本人確認**
   - 電話またはSMSで確認コードを受け取る
   - 受け取ったコードを入力

6. **サポートプランの選択**
   - 「ベーシックサポート（無料）」を選択
   - ✅ 完了！アカウントが有効化されるまで数分待ちます

### 1.2 IAMユーザーの作成（セキュリティのため）

⚠️ **重要**: ルートユーザーは使わず、IAMユーザーを作成します

1. **AWSマネジメントコンソールにログイン**
   - URL: https://console.aws.amazon.com/

2. **IAMサービスを開く**
   - 検索バーで「IAM」と入力
   - 「IAM」サービスをクリック

3. **ユーザーの作成**
   - 左メニューから「ユーザー」をクリック
   - 「ユーザーを追加」ボタンをクリック

4. **ユーザー情報の入力**
   ```
   ユーザー名: letteral-admin
   アクセスの種類:
   ✅ プログラムによるアクセス（AWS CLI用）
   ✅ AWSマネジメントコンソールへのアクセス
   ```

5. **アクセス許可の設定**
   - 「既存のポリシーを直接アタッチ」を選択
   - 「AdministratorAccess」を検索してチェック
   - （本番環境では最小権限の原則に従ってください）

6. **認証情報の保存**
   - ⚠️ **重要**: 以下の情報をメモ帳に保存してください
   ```
   アクセスキーID: AKIAIOSFODNN7EXAMPLE
   シークレットアクセスキー: wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY
   パスワード: (設定したパスワード)
   サインインURL: https://YOUR_ACCOUNT_ID.signin.aws.amazon.com/console
   ```

---

## ステップ2: AWS CLIのセットアップ

AWS CLIは、コマンドラインからAWSを操作するためのツールです。

### 2.1 AWS CLIのインストール

#### Windows

```powershell
# PowerShellを管理者として開く

# インストーラーをダウンロード
msiexec.exe /i https://awscli.amazonaws.com/AWSCLIV2.msi

# インストールの確認
aws --version
# → aws-cli/2.x.x ...
```

#### macOS

```bash
# ターミナルを開く

# Homebrewでインストール
brew install awscli

# インストールの確認
aws --version
```

#### Linux (Ubuntu/Debian)

```bash
# ターミナルを開く

# インストール
curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip"
unzip awscliv2.zip
sudo ./aws/install

# インストールの確認
aws --version
```

### 2.2 AWS CLIの設定

```bash
# AWS CLIの設定を開始
aws configure

# 以下を入力（ステップ1.2で保存した情報を使用）
AWS Access Key ID [None]: AKIAIOSFODNN7EXAMPLE
AWS Secret Access Key [None]: wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY
Default region name [None]: ap-northeast-1
Default output format [None]: json
```

### 2.3 動作確認

```bash
# 現在のユーザー情報を表示
aws sts get-caller-identity

# 出力例:
# {
#     "UserId": "AIDAXXXXXXXXXXXXX",
#     "Account": "123456789012",
#     "Arn": "arn:aws:iam::123456789012:user/letteral-admin"
# }
```

✅ この出力が表示されれば成功です！

---

## ステップ3: 必要なツールのインストール

### 3.1 Dockerのインストール

#### Windows

1. Docker Desktopをダウンロード
   - URL: https://www.docker.com/products/docker-desktop
2. インストーラーを実行
3. PCを再起動
4. Docker Desktopを起動

```powershell
# 動作確認
docker --version
docker run hello-world
```

#### macOS

```bash
# Homebrewでインストール
brew install --cask docker

# Docker Desktopを起動
open -a Docker

# 動作確認
docker --version
```

### 3.2 Gitのインストール（未インストールの場合）

```bash
# Windows (PowerShell)
winget install --id Git.Git -e --source winget

# macOS
brew install git

# Linux
sudo apt-get install git
```

---

## ステップ4: データベースのセットアップ（RDS）

### 4.1 RDS（データベース）の作成

1. **AWSコンソールでRDSを開く**
   - 検索バーで「RDS」と入力
   - 「RDS」サービスをクリック

2. **データベースの作成開始**
   - 「データベースの作成」ボタンをクリック

3. **エンジンの選択**
   ```
   エンジンタイプ: MySQL
   バージョン: MySQL 8.0.33
   ```

4. **テンプレートの選択**
   ```
   テンプレート: 「無料利用枠」（開発/テスト用）
   または
   テンプレート: 「本番稼働用」（本番環境用）
   ```

5. **設定**
   ```
   DBインスタンス識別子: letteral-prod-db
   マスターユーザー名: admin
   マスターパスワード: （強力なパスワードを設定）
   パスワードの確認: （再入力）
   ```

   💡 **パスワードの例**: `LetTeral#2025!Db@Secure`

   ⚠️ **重要**: このパスワードは後で使うのでメモしてください！

6. **インスタンスの設定**
   ```
   【開発/テスト環境】
   DBインスタンスクラス: db.t3.micro（無料枠対象）
   ストレージタイプ: 汎用SSD (gp3)
   割り当てられたストレージ: 20 GB

   【本番環境】
   DBインスタンスクラス: db.t3.medium
   ストレージタイプ: 汎用SSD (gp3)
   割り当てられたストレージ: 100 GB
   ストレージの自動スケーリング: 有効
   ```

7. **接続設定**
   ```
   コンピューティングリソース: 「EC2コンピューティングリソースに接続しない」
   VPC: （デフォルトVPC）
   パブリックアクセス: 「はい」（初期設定用、後で変更可能）
   VPCセキュリティグループ: 新規作成
   セキュリティグループ名: letteral-db-sg
   ```

8. **データベース認証**
   ```
   データベース認証オプション: パスワード認証
   ```

9. **追加設定を展開**
   ```
   初期データベース名: letteral_prod
   DBパラメータグループ: default.mysql8.0
   バックアップ:
   ✅ 自動バックアップの有効化
   バックアップ保持期間: 7日
   暗号化: 有効
   ```

10. **作成**
    - 「データベースの作成」ボタンをクリック
    - ⏱️ 作成完了まで約10-15分かかります

### 4.2 セキュリティグループの設定

データベースが作成されたら、セキュリティグループを設定します。

1. **RDSダッシュボードでデータベースをクリック**

2. **エンドポイントをメモ**
   ```
   エンドポイント: letteral-prod-db.xxxxxxxxxxxx.ap-northeast-1.rds.amazonaws.com
   letteral-prod-db.c9qsu08245i5.ap-northeast-1.rds.amazonaws.com
   ポート: 3306
   ```
   ⚠️ **重要**: このエンドポイントは後で使います！

3. **セキュリティグループの編集**
   - 「接続とセキュリティ」タブを開く
   - セキュリティグループ名をクリック
   - 「インバウンドルール」タブを選択
   - 「インバウンドルールを編集」をクリック

4. **一時的にローカルからの接続を許可**
   ```
   タイプ: MySQL/Aurora
   プロトコル: TCP
   ポート範囲: 3306
   ソース: マイIP（自分のIPアドレスが自動入力される）
   説明: Local development access
   ```
   - 「ルールを保存」をクリック

### 4.3 データベースの初期化

```bash
# MySQLクライアントがない場合はインストール
# Windows: https://dev.mysql.com/downloads/installer/
# macOS: brew install mysql-client
# Linux: sudo apt-get install mysql-client

# データベースに接続
mysql -h letteral-prod-db.c9qsu08245i5.ap-northeast-1.rds.amazonaws.com \
  -P 3306 \
  -u admin \
  -p letteral_prod

# パスワードを入力

# 接続成功したら以下のコマンドで確認
SHOW DATABASES;
# → letteral_prod が表示されればOK

# 切断
EXIT;
```

✅ データベースのセットアップ完了！

---

## ステップ5: アプリケーションのデプロイ（ECS）

### 5.1 ECR（コンテナレジストリ）の作成

ECRは、Dockerイメージを保存する場所です。

1. **AWSコンソールでECRを開く**
   - 検索バーで「ECR」と入力
   - 「Elastic Container Registry」をクリック

2. **リポジトリの作成**
   - 「リポジトリを作成」ボタンをクリック
   ```
   可視性設定: プライベート
   リポジトリ名: letteral-backend
   タグのイミュータビリティ: 無効
   イメージスキャン: 有効
   暗号化: AES-256
   ```
   - 「リポジトリを作成」をクリック

3. **リポジトリURIをメモ**
   ```
   リポジトリURI: 123456789012.dkr.ecr.ap-northeast-1.amazonaws.com/letteral-backend

   187038479922.dkr.ecr.ap-northeast-1.amazonaws.com/letteral-backend
   ```

### 5.2 Dockerイメージのビルドとプッシュ

```bash
# プロジェクトディレクトリに移動
cd "c:\Users\User\OneDrive\hera-16\チャレキャラ"

# ECRにログイン
aws ecr get-login-password --region ap-northeast-1 | docker login --username AWS --password-stdin 123456789012.dkr.ecr.ap-northeast-1.amazonaws.com

# 成功メッセージ: Login Succeeded

# バックエンドのDockerイメージをビルド
cd backend
docker build -t letteral-backend:latest -f Dockerfile.prod .

# ⏱️ ビルドに5-10分かかります

# ECRにタグ付け
docker tag letteral-backend:latest 123456789012.dkr.ecr.ap-northeast-1.amazonaws.com/letteral-backend:latest

# ECRにプッシュ
docker push 123456789012.dkr.ecr.ap-northeast-1.amazonaws.com/letteral-backend:latest

# ⏱️ プッシュに3-5分かかります
```

### 5.3 ECS クラスターの作成

1. **AWSコンソールでECSを開く**
   - 検索バーで「ECS」と入力
   - 「Elastic Container Service」をクリック

2. **クラスターの作成**
   - 「クラスターの作成」ボタンをクリック
   ```
   クラスター名: letteral-prod-cluster
   ネットワーキング:
   ✅ 新しいVPCを作成
   ```
   - 「作成」をクリック

### 5.4 タスク定義の作成

1. **タスク定義を作成**
   - 左メニューから「タスク定義」をクリック
   - 「新しいタスク定義の作成」をクリック

2. **タスク定義の設定**
   ```
   タスク定義ファミリー: letteral-backend-task

   起動タイプ: AWS Fargate

   オペレーティングシステム: Linux/X86_64

   タスクサイズ:
   CPU: 1 vCPU
   メモリ: 2 GB

   タスクロール: なし（後で設定）
   タスク実行ロール: ecsTaskExecutionRole（自動作成）
   ```

3. **コンテナの追加**
   ```
   コンテナ名: letteral-backend

   イメージURI: 123456789012.dkr.ecr.ap-northeast-1.amazonaws.com/letteral-backend:latest

   ポートマッピング:
   コンテナポート: 8080
   プロトコル: TCP
   アプリケーションプロトコル: HTTP
   ```

4. **環境変数の設定**
   - 「環境変数」セクションを展開
   ```
   キー: SPRING_PROFILES_ACTIVE
   値: prod

   キー: SPRING_DATASOURCE_URL
   値: jdbc:mysql://letteral-prod-db.xxxxxxxxxxxx.ap-northeast-1.rds.amazonaws.com:3306/letteral_prod?useSSL=false&serverTimezone=Asia/Tokyo

   キー: SPRING_DATASOURCE_USERNAME
   値: admin

   キー: SPRING_DATASOURCE_PASSWORD
   値: （RDSで設定したパスワード）

   キー: JWT_SECRET
   値: （以下のコマンドで生成）
   ```

   JWT Secretの生成（PowerShell）:
   ```powershell
   $bytes = New-Object byte[] 64
   [Security.Cryptography.RandomNumberGenerator]::Create().GetBytes($bytes)
   [Convert]::ToBase64String($bytes)
   ```

5. **ヘルスチェックの設定**
   ```
   ヘルスチェック:
   コマンド: CMD-SHELL,curl -f http://localhost:8080/actuator/health || exit 1
   間隔: 30秒
   タイムアウト: 5秒
   開始期間: 60秒
   再試行: 3回
   ```

6. **ログの設定**
   ```
   ログドライバー: awslogs
   ログオプション:
   awslogs-group: /ecs/letteral-backend
   awslogs-region: ap-northeast-1
   awslogs-stream-prefix: ecs
   ```

7. **作成**
   - 「作成」ボタンをクリック

### 5.5 Application Load Balancer（ALB）の作成

1. **EC2コンソールを開く**
   - 検索バーで「EC2」と入力
   - 左メニューから「ロードバランサー」をクリック

2. **ロードバランサーの作成**
   - 「ロードバランサーの作成」をクリック
   - 「Application Load Balancer」を選択

3. **基本設定**
   ```
   ロードバランサー名: letteral-alb
   スキーム: インターネット向け
   IPアドレスタイプ: IPv4
   ```

4. **ネットワークマッピング**
   ```
   VPC: letteral-prod-clusterのVPC
   アベイラビリティーゾーン:
   ✅ ap-northeast-1a
   ✅ ap-northeast-1c
   ```

5. **セキュリティグループ**
   - 「新しいセキュリティグループを作成」をクリック
   ```
   セキュリティグループ名: letteral-alb-sg
   説明: Security group for Letteral ALB

   インバウンドルール:
   タイプ: HTTP
   ポート: 80
   ソース: 0.0.0.0/0

   タイプ: HTTPS
   ポート: 443
   ソース: 0.0.0.0/0
   ```

6. **リスナーとルーティング**
   - 「ターゲットグループを作成」をクリック（新しいタブが開きます）

   ターゲットグループの設定:
   ```
   ターゲットタイプ: IPアドレス
   ターゲットグループ名: letteral-tg
   プロトコル: HTTP
   ポート: 8080
   VPC: letteral-prod-clusterのVPC
   プロトコルバージョン: HTTP1

   ヘルスチェック設定:
   ヘルスチェックパス: /actuator/health
   正常のしきい値: 2
   非正常のしきい値: 3
   タイムアウト: 5秒
   間隔: 30秒
   成功コード: 200
   ```

   - 「次へ」をクリック
   - 「ターゲットグループの作成」をクリック

7. **ALBに戻って設定を完了**
   - リスナーのセクションで、作成したターゲットグループを選択
   - 「ロードバランサーの作成」をクリック

8. **ALBのDNS名をメモ**
   ```
   DNS名: letteral-alb-xxxxxxxxxxxx.ap-northeast-1.elb.amazonaws.com
   letteral-alb-1556869628.ap-northeast-1.elb.amazonaws.com
   ```

### 5.6 ECS サービスの作成

1. **ECSクラスターを開く**
   - 「letteral-prod-cluster」をクリック
   - 「サービス」タブを選択
   - 「作成」ボタンをクリック

2. **デプロイ設定**
   ```
   起動タイプ: Fargate
   アプリケーションタイプ: サービス

   ファミリー: letteral-backend-task
   サービス名: letteral-backend-service
   タスクの数: 2
   ```

3. **ネットワーキング**
   ```
   VPC: letteral-prod-clusterのVPC
   サブネット: プライベートサブネットを選択
   セキュリティグループ: 新規作成

   新しいセキュリティグループの設定:
   セキュリティグループ名: letteral-ecs-sg

   インバウンドルール:
   タイプ: カスタムTCP
   ポート: 8080
   ソース: letteral-alb-sg（ALBのセキュリティグループ）
   ```

4. **ロードバランシング**
   ```
   ロードバランサーの種類: Application Load Balancer
   ロードバランサー: letteral-alb
   リスナー: 80:HTTP
   ターゲットグループ: letteral-tg
   ヘルスチェックの猶予期間: 60秒
   ```

5. **サービスの自動スケーリング**
   ```
   ✅ サービスの自動スケーリングを使用

   最小タスク数: 2
   最大タスク数: 10

   スケーリングポリシー:
   ポリシー名: cpu-scaling
   メトリクスタイプ: ECSServiceAverageCPUUtilization
   ターゲット値: 70
   ```

6. **作成**
   - 「作成」ボタンをクリック
   - ⏱️ サービスの起動に約5-10分かかります

### 5.7 動作確認

```bash
# ALBのDNS名でアクセス
curl http://letteral-alb-xxxxxxxxxxxx.ap-northeast-1.elb.amazonaws.com/actuator/health

# 期待される出力:
# {"status":"UP"}
```

または、ブラウザで以下にアクセス:
```
http://letteral-alb-xxxxxxxxxxxx.ap-northeast-1.elb.amazonaws.com/actuator/health
```

✅ `{"status":"UP"}` が表示されれば成功です！

---

## ステップ6: 動作確認

### 6.1 ユーザー登録のテスト

```bash
# ユーザー登録
curl -X POST http://letteral-alb-xxxxxxxxxxxx.ap-northeast-1.elb.amazonaws.com/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "Password123!",
    "displayName": "テストユーザー"
  }'

# 期待される出力:
# {"message":"User registered successfully"}
```

### 6.2 ログインのテスト

```bash
# ログイン
curl -X POST http://letteral-alb-xxxxxxxxxxxx.ap-northeast-1.elb.amazonaws.com/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "Password123!"
  }'

# 期待される出力:
# {"token":"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."}
```

### 6.3 CloudWatch Logsの確認

1. **CloudWatchコンソールを開く**
   - 検索バーで「CloudWatch」と入力
   - 左メニューから「ロググループ」をクリック

2. **ログストリームを確認**
   - `/ecs/letteral-backend` をクリック
   - 最新のログストリームをクリック
   - アプリケーションのログが表示されます

---

## トラブルシューティング

### 問題1: ECSタスクが起動しない

**症状**: サービスがタスクを起動できない

**確認方法**:
1. ECSコンソール → クラスター → サービス → 「タスク」タブ
2. 停止したタスクをクリック
3. 「停止理由」を確認

**よくある原因と解決策**:

#### 原因A: ECRからイメージをプルできない

```bash
# ECRへのアクセス権限を確認
aws ecr get-login-password --region ap-northeast-1

# 成功すればパスワードが表示される
```

解決策:
- IAMロール `ecsTaskExecutionRole` に ECRアクセス権限があるか確認
- ECRリポジトリが存在するか確認

#### 原因B: データベースに接続できない

**確認**:
- RDSのセキュリティグループでECSからのアクセスが許可されているか
- 環境変数のデータベースURLが正しいか

**解決策**:
1. RDSのセキュリティグループを開く
2. インバウンドルールに以下を追加:
   ```
   タイプ: MySQL/Aurora
   ポート: 3306
   ソース: letteral-ecs-sg（ECSのセキュリティグループ）
   ```

#### 原因C: メモリ不足

**症状**: タスクがOOM（Out of Memory）で停止

**解決策**:
1. タスク定義を開く
2. メモリを 2GB → 4GB に増やす
3. 新しいリビジョンを作成
4. サービスを更新して新しいタスク定義を使用

### 問題2: ALB経由でアクセスできない

**確認方法**:
```bash
# ターゲットグループのヘルスチェック状態を確認
aws elbv2 describe-target-health \
  --target-group-arn arn:aws:elasticloadbalancing:ap-northeast-1:123456789012:targetgroup/letteral-tg/xxxxx
```

**よくある原因**:

#### 原因A: ターゲットが登録されていない

**確認**: EC2コンソール → ターゲットグループ → ターゲット

**解決策**:
- ECSサービスの設定でターゲットグループが正しく設定されているか確認

#### 原因B: セキュリティグループの設定ミス

**確認**:
1. ALBのセキュリティグループ: 80, 443が許可されているか
2. ECSのセキュリティグループ: ALBからの8080が許可されているか

### 問題3: データベース接続エラー

**エラーメッセージ**:
```
Communications link failure
```

**解決策**:

1. **RDSが起動しているか確認**
   ```bash
   aws rds describe-db-instances \
     --db-instance-identifier letteral-prod-db \
     --query 'DBInstances[0].DBInstanceStatus'
   ```

2. **エンドポイントが正しいか確認**
   - ECSタスク定義の環境変数を確認
   - RDSのエンドポイントをコピー&ペーストで確認

3. **セキュリティグループを確認**
   - ECSのセキュリティグループIDをメモ
   - RDSのセキュリティグループでそのIDからのアクセスを許可

---

## コスト管理

### コストを抑えるためのヒント

#### 1. 開発環境は使わない時は停止

```bash
# ECSサービスのタスク数を0に設定
aws ecs update-service \
  --cluster letteral-prod-cluster \
  --service letteral-backend-service \
  --desired-count 0

# RDSを停止（最大7日間停止可能）
aws rds stop-db-instance \
  --db-instance-identifier letteral-prod-db
```

#### 2. AWS無料枠の活用

以下のサービスは12ヶ月間無料枠があります:
- **EC2**: 750時間/月（t2.micro/t3.micro）
- **RDS**: 750時間/月（db.t2.micro/db.t3.micro）
- **S3**: 5GB
- **ALB**: なし（有料）
- **ECS Fargate**: 初月のみ一部無料

#### 3. コスト監視アラートの設定

1. **Billing & Cost Managementを開く**
   - 検索バーで「Billing」と入力

2. **予算の作成**
   ```
   予算名: letteral-monthly-budget
   期間: 月次
   予算額: $100（または任意の金額）
   ```

3. **アラートの設定**
   ```
   しきい値: 80%（$80の時点でアラート）
   通知先: あなたのメールアドレス
   ```

### 月次コスト見積もり

#### 最小構成（開発/テスト）
```
RDS db.t3.micro (無料枠): $0
ECS Fargate 1タスク: $30
ALB: $25
CloudWatch Logs: $5
---
合計: 約$60/月
```

#### 推奨構成（小規模本番）
```
RDS db.t3.medium Multi-AZ: $100
ECS Fargate 2タスク: $60
ALB: $25
ElastiCache cache.t3.micro: $20
CloudWatch Logs: $10
S3: $5
---
合計: 約$220/月
```

---

## 次のステップ

### 1. 独自ドメインの設定

1. **Route 53でドメインを購入**（または既存ドメインを使用）
2. **ACMでSSL証明書を発行**
3. **ALBにHTTPSリスナーを追加**
4. **Route 53でAレコードを作成**

詳細は [AWS_DEPLOYMENT.md](./AWS_DEPLOYMENT.md) の「ドメインの準備」セクションを参照してください。

### 2. フロントエンドのデプロイ

1. **S3バケットを作成**
2. **Next.jsアプリをビルド**
   ```bash
   npm run build
   npm run export
   ```
3. **S3にアップロード**
4. **CloudFrontで配信**

### 3. CI/CDパイプラインの構築

GitHub ActionsまたはAWS CodePipelineを使用して、自動デプロイを設定できます。

### 4. 監視とアラートの強化

- CloudWatchダッシュボードの作成
- X-Rayによる分散トレーシング
- SNSによるアラート通知

---

## サポート・質問

### よくある質問

**Q: AWS無料枠はいつまで使えますか？**
A: アカウント作成から12ヶ月間です。一部のサービス（Lambda、DynamoDB等）は常時無料枠があります。

**Q: 本番環境に移行する際の注意点は？**
A:
- パブリックアクセスを無効化してVPNやPrivateLinkを使用
- Multi-AZ構成を有効化
- バックアップとスナップショットを定期的に取得
- セキュリティグループを最小権限に設定

**Q: コストが予想以上にかかっています**
A:
- Cost Explorerで詳細を確認
- 使っていないリソース（EBS、スナップショット等）を削除
- RDSとECSを停止（開発環境の場合）

### 参考リンク

- [AWS公式ドキュメント](https://docs.aws.amazon.com/ja_jp/)
- [AWS Well-Architected Framework](https://aws.amazon.com/jp/architecture/well-architected/)
- [AWS料金計算ツール](https://calculator.aws/)

---

## まとめ

お疲れ様でした！これでLetteralプラットフォームがAWS上で動作しています。

以下のことができるようになりました：
- ✅ AWSアカウントの作成とセットアップ
- ✅ RDSでのデータベース構築
- ✅ ECS Fargateでのアプリケーション実行
- ✅ ALBによる負荷分散
- ✅ 自動スケーリングの設定

次は、独自ドメインの設定とHTTPS化を行うことで、より本格的な本番環境を構築できます。

何か問題が発生した場合は、[トラブルシューティング](#トラブルシューティング)セクションを参照するか、CloudWatch Logsでエラー内容を確認してください。

---

📅 **最終更新**: 2025-01-18
📝 **バージョン**: 1.0.0
👤 **対象**: AWS初心者

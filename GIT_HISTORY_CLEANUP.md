# Git履歴クリーンアップガイド

## 🚨 問題の概要

以下の機密情報がGit履歴にコミットされています:

1. **JWT秘密鍵**: `mySecretKey123456789012345678901234567890`
   - 場所: `backend/src/main/resources/application.properties`
   - 深刻度: **中** (サンプル用の弱い鍵なので、本番で使用していなければ低リスク)

2. **データベースパスワード**: `chatapp_password`
   - 場所: `backend/src/main/resources/application.properties`
   - 深刻度: **低** (開発用のデフォルト値)

## 📊 リスク評価

### 🟢 安全な状況 (対処不要)
- ✅ サンプル用の秘密鍵を本番環境で使用していない
- ✅ 本番環境では環境変数で異なる秘密鍵を使用している
- ✅ データベースパスワードも開発用のデフォルト値
- ✅ 本番環境のデータベースは別の強力なパスワードを使用

### 🟡 注意が必要な状況
- ⚠️ 同じ秘密鍵を本番環境で使用していた場合
- ⚠️ 公開リポジトリとして運用する場合

### 🔴 危険な状況 (即座に対処が必要)
- 🚨 本番環境で同じJWT秘密鍵を使用している
- 🚨 本番データベースで同じパスワードを使用している
- 🚨 実際の本番環境の認証情報がコミットされている

## 🛠️ 対処方法

### オプション1: 何もしない (推奨: 低リスクの場合)

**条件:**
- 本番環境で異なる秘密鍵とパスワードを使用している
- これらの情報が開発用のサンプル値である

**理由:**
- Git履歴の書き換えは複雑で、他の開発者に影響を与える
- サンプル値なので実害がない
- 既に環境変数に移行済み

**推奨アクション:**
```bash
# 何もする必要はありません
# 既に環境変数方式に移行しているので、今後は安全です
```

### オプション2: リポジトリを新規作成 (推奨: 最もシンプル)

**条件:**
- まだ公開していない、または他の開発者がいない
- 履歴を保持する必要がない

**手順:**

1. **現在のコードをバックアップ**
```powershell
# 現在の作業ディレクトリをコピー
Copy-Item -Path . -Destination "..\チャレキャラ-backup" -Recurse
```

2. **GitHubで新しいリポジトリを作成**
   - GitHubにログイン
   - New repository をクリック
   - リポジトリ名を変更 (例: `letteral-v2`)

3. **新しいリポジトリに現在のコードをプッシュ**
```powershell
# .gitディレクトリを削除
Remove-Item -Path ".git" -Recurse -Force

# 新しいGitリポジトリを初期化
git init
git add .
git commit -m "Initial commit: Production-ready chat application with environment variables"

# 新しいリモートリポジトリに接続
git remote add origin https://github.com/hera-16/letteral-v2.git
git branch -M main
git push -u origin main
```

4. **古いリポジトリを削除またはプライベート化**
   - GitHubの Settings → Danger Zone → Delete repository

### オプション3: Git履歴を書き換える (上級者向け)

**条件:**
- 履歴を保持したい
- 機密情報を完全に削除したい
- Git履歴の書き換えに慣れている

**⚠️ 警告:**
- この操作は取り消せません
- 他の開発者がいる場合、全員が再クローンする必要があります
- バックアップを必ず取ってください

**手順:**

#### 方法A: BFG Repo-Cleaner (最も簡単)

1. **BFG Repo-Cleanerをダウンロード**
```powershell
# https://rtyley.github.io/bfg-repo-cleaner/ からダウンロード
# または
Invoke-WebRequest -Uri "https://repo1.maven.org/maven2/com/madgag/bfg/1.14.0/bfg-1.14.0.jar" -OutFile "bfg.jar"
```

2. **置換するテキストファイルを作成**
```powershell
# passwords.txt を作成
@"
mySecretKey123456789012345678901234567890
chatapp_password
"@ | Out-File -FilePath "passwords.txt" -Encoding UTF8
```

3. **BFGを実行**
```powershell
# 現在のコードをコミット
git add .
git commit -m "Latest changes"

# BFGで機密情報を置換
java -jar bfg.jar --replace-text passwords.txt .

# 履歴をクリーンアップ
git reflog expire --expire=now --all
git gc --prune=now --aggressive
```

4. **リモートに強制プッシュ**
```powershell
git push origin main --force
```

#### 方法B: git filter-repo

1. **git-filter-repoをインストール**
```powershell
# Python 3が必要
python -m pip install git-filter-repo
```

2. **application.propertiesを書き換え**
```powershell
# 機密情報を置換するスクリプトを作成
$script = @"
import sys
import re

data = sys.stdin.buffer.read()
try:
    text = data.decode('utf-8')
    text = re.sub(r'jwt\.secret=mySecretKey\d+', 
                  r'jwt.secret=\${JWT_SECRET:changeme}', text)
    text = re.sub(r'spring\.datasource\.password=chatapp_password', 
                  r'spring.datasource.password=\${SPRING_DATASOURCE_PASSWORD:password}', text)
    sys.stdout.buffer.write(text.encode('utf-8'))
except:
    sys.stdout.buffer.write(data)
"@
$script | Out-File -FilePath "filter.py" -Encoding UTF8

# 実行
git filter-repo --blob-callback "`$(python filter.py)" --force

# クリーンアップ
Remove-Item filter.py
```

3. **リモートに強制プッシュ**
```powershell
git remote add origin https://github.com/hera-16/letteral.git
git push origin main --force
```

## 🔐 今後の予防策

既に以下の対策を実装済みです:

- ✅ `.gitignore` に `backend/.env` を追加
- ✅ `application.properties` を環境変数ベースに変更
- ✅ `backend/.env.example` をテンプレートとして作成
- ✅ `backend/PRODUCTION_SETUP.md` でセキュリティガイドを作成

**追加の予防策:**

1. **Git hooks を設定** (コミット前チェック)
```powershell
# .git/hooks/pre-commit を作成
@"
#!/bin/sh
# 機密情報のチェック
if git diff --cached --name-only | grep -q "\.env$"; then
    echo "Error: .env file should not be committed!"
    exit 1
fi

if git diff --cached -G "password|secret|token" -- backend/src/main/resources/application.properties; then
    echo "Warning: application.properties contains potential secrets!"
    echo "Please use environment variables instead."
    exit 1
fi
"@ | Out-File -FilePath ".git/hooks/pre-commit" -Encoding UTF8
```

2. **GitHub Secret Scanning を有効化**
   - Settings → Code security and analysis → Secret scanning
   - GitHubが自動的に機密情報を検出

3. **定期的な監査**
```powershell
# リポジトリ内の機密情報をスキャン
git log --all -p | Select-String -Pattern "password|secret|token|key" -Context 2
```

## 📝 推奨アクション

あなたの場合、以下を推奨します:

### ✅ 即座に実行すべきこと

1. **本番環境の秘密鍵を確認**
```bash
# 本番環境で使用している秘密鍵を確認
# もし mySecretKey... を使用していたら、即座に変更!
```

2. **本番環境で新しい秘密鍵を生成して設定**
```powershell
# 新しい秘密鍵を生成
[Convert]::ToBase64String((1..64 | ForEach-Object { Get-Random -Minimum 0 -Maximum 256 }))

# backend/.env に設定
JWT_SECRET=<generated-key>
```

### 🤔 Git履歴の対処は?

**個人開発の場合:**
- **推奨: オプション2 (リポジトリ新規作成)** - 最もシンプルで安全

**チーム開発の場合:**
- **推奨: オプション1 (何もしない)** - サンプル値なので実害なし

**公開ポートフォリオの場合:**
- **推奨: オプション2 (リポジトリ新規作成)** - クリーンな履歴で公開

## ❓ よくある質問

**Q: 履歴を書き換えないとどうなりますか?**
A: サンプル値なので実害はありません。ただし、誰でも過去の履歴を見ることができます。

**Q: 既に公開されている場合は?**
A: サンプル値であれば問題ありません。本番環境で異なる秘密鍵を使用していることを確認してください。

**Q: 他の開発者がいる場合は?**
A: 履歴を書き換えると、全員が `git clone` し直す必要があります。事前に通知してください。

## 📞 サポート

不明な点があれば、以下を確認してください:
- [GitHub Docs: Removing sensitive data](https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/removing-sensitive-data-from-a-repository)
- [BFG Repo-Cleaner](https://rtyley.github.io/bfg-repo-cleaner/)
- [git-filter-repo](https://github.com/newren/git-filter-repo)

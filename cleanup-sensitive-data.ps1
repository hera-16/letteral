# Git履歴から機密情報を削除するスクリプト
# 警告: このスクリプトはGit履歴を書き換えます。必ずバックアップを取ってから実行してください。

Write-Host "==================================================" -ForegroundColor Yellow
Write-Host "Git履歴から機密情報を削除" -ForegroundColor Yellow
Write-Host "==================================================" -ForegroundColor Yellow
Write-Host ""
Write-Host "このスクリプトは以下の操作を行います:" -ForegroundColor Cyan
Write-Host "1. Git履歴から機密情報を含むファイルを削除/置換" -ForegroundColor Cyan
Write-Host "2. リモートリポジトリに強制プッシュ (--force)" -ForegroundColor Cyan
Write-Host ""
Write-Host "警告: この操作は取り消せません!" -ForegroundColor Red
Write-Host ""

$confirmation = Read-Host "続行しますか? (yes/no)"
if ($confirmation -ne "yes") {
    Write-Host "キャンセルしました。" -ForegroundColor Yellow
    exit
}

Write-Host ""
Write-Host "バックアップを作成しています..." -ForegroundColor Green
$backupPath = "..\チャレキャラ-backup-$(Get-Date -Format 'yyyyMMdd-HHmmss')"
Copy-Item -Path . -Destination $backupPath -Recurse -Force
Write-Host "バックアップ完了: $backupPath" -ForegroundColor Green

Write-Host ""
Write-Host "git-filter-repoをインストールしています..." -ForegroundColor Green

# git-filter-repoがインストールされているか確認
$filterRepoExists = $null
try {
    $filterRepoExists = Get-Command git-filter-repo -ErrorAction Stop
} catch {
    Write-Host "git-filter-repoが見つかりません。インストールします..." -ForegroundColor Yellow
    
    # Pythonがインストールされているか確認
    $pythonExists = $null
    try {
        $pythonExists = Get-Command python -ErrorAction Stop
        Write-Host "Python found: $(python --version)" -ForegroundColor Green
        
        # pipでインストール
        Write-Host "pip install git-filter-repo を実行中..." -ForegroundColor Cyan
        python -m pip install git-filter-repo
        
    } catch {
        Write-Host "エラー: Pythonがインストールされていません。" -ForegroundColor Red
        Write-Host "以下の手順で手動で対処してください:" -ForegroundColor Yellow
        Write-Host "1. Python 3をインストール: https://www.python.org/downloads/" -ForegroundColor Yellow
        Write-Host "2. pip install git-filter-repo を実行" -ForegroundColor Yellow
        Write-Host "3. このスクリプトを再度実行" -ForegroundColor Yellow
        exit 1
    }
}

Write-Host ""
Write-Host "application.propertiesから機密情報を削除しています..." -ForegroundColor Green

# 機密情報を置換するスクリプトを作成
$replaceScript = @"
import re
import sys

content = sys.stdin.buffer.read()
try:
    text = content.decode('utf-8')
    
    # JWT秘密鍵を環境変数に置換
    text = re.sub(
        r'jwt\.secret=mySecretKey\d+',
        'jwt.secret=\${JWT_SECRET:changeme-please-set-jwt-secret-environment-variable}',
        text
    )
    
    # データベースパスワードを環境変数に置換
    text = re.sub(
        r'spring\.datasource\.password=\w+',
        'spring.datasource.password=\${SPRING_DATASOURCE_PASSWORD:chatapp_password}',
        text
    )
    
    sys.stdout.buffer.write(text.encode('utf-8'))
except:
    sys.stdout.buffer.write(content)
"@

$replaceScript | Out-File -FilePath "replace-secrets.py" -Encoding UTF8

Write-Host ""
Write-Host "Git履歴を書き換えています..." -ForegroundColor Green
Write-Host "(これには数分かかる場合があります)" -ForegroundColor Yellow

# application.propertiesの内容を書き換え
git filter-repo --force --blob-callback "
if blob.original_id in get_file_changes(b'backend/src/main/resources/application.properties'):
    blob.data = re.sub(b'jwt\.secret=mySecretKey\d+', b'jwt.secret=\${JWT_SECRET:changeme}', blob.data)
    blob.data = re.sub(b'spring\.datasource\.password=\w+', b'spring.datasource.password=\${SPRING_DATASOURCE_PASSWORD:password}', blob.data)
" --force

Write-Host ""
Write-Host "履歴の書き換えが完了しました!" -ForegroundColor Green
Write-Host ""
Write-Host "次のステップ:" -ForegroundColor Cyan
Write-Host "1. 変更を確認: git log --oneline" -ForegroundColor Yellow
Write-Host "2. リモートリポジトリに強制プッシュ: git push origin main --force" -ForegroundColor Yellow
Write-Host ""
Write-Host "警告: --force プッシュは他の開発者に影響を与えます!" -ForegroundColor Red
Write-Host "チーム開発の場合は、事前に通知してください。" -ForegroundColor Red

# 一時ファイルを削除
Remove-Item -Path "replace-secrets.py" -ErrorAction SilentlyContinue

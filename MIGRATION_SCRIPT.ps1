# ============================================
# Next.js プロジェクト移行スクリプト
# OneDrive → 通常フォルダへの安全な移行
# ============================================

param(
    [string]$SourcePath = "C:\Users\User\OneDrive\hera-16\チャレキャラ",
    [string]$DestinationPath = "C:\dev\チャレキャラ",
    [switch]$SkipBackup,
    [switch]$AutoClean
)

$ErrorActionPreference = "Stop"

# ============================================
# カラー出力関数
# ============================================
function Write-ColorOutput {
    param([string]$Message, [string]$Color = "White")
    Write-Host $Message -ForegroundColor $Color
}

function Write-Success { param([string]$Message) Write-ColorOutput "✓ $Message" "Green" }
function Write-Warning { param([string]$Message) Write-ColorOutput "⚠ $Message" "Yellow" }
function Write-Error { param([string]$Message) Write-ColorOutput "✗ $Message" "Red" }
function Write-Info { param([string]$Message) Write-ColorOutput "ℹ $Message" "Cyan" }
function Write-Step { param([string]$Message) Write-ColorOutput "`n=== $Message ===" "Magenta" }

# ============================================
# 1. 事前チェック
# ============================================
Write-Step "事前チェック開始"

# ソースディレクトリの存在確認
if (-not (Test-Path $SourcePath)) {
    Write-Error "ソースディレクトリが見つかりません: $SourcePath"
    exit 1
}
Write-Success "ソースディレクトリ確認: $SourcePath"

# package.json の存在確認
if (-not (Test-Path "$SourcePath\package.json")) {
    Write-Error "package.json が見つかりません。Next.js プロジェクトではない可能性があります。"
    exit 1
}
Write-Success "Next.js プロジェクト確認"

# 十分なディスク容量の確認（10GB以上）
$DestDrive = Split-Path $DestinationPath -Qualifier
$FreeSpace = (Get-PSDrive $DestDrive.TrimEnd(':')).Free
if ($FreeSpace -lt 10GB) {
    Write-Warning "移行先ドライブの空き容量が少ないです: $([math]::Round($FreeSpace/1GB, 2)) GB"
    $continue = Read-Host "続行しますか? (y/n)"
    if ($continue -ne 'y') { exit 0 }
}
Write-Success "ディスク容量確認: $([math]::Round($FreeSpace/1GB, 2)) GB 利用可能"

# Git の状態確認
Push-Location $SourcePath
try {
    $gitStatus = git status --porcelain 2>$null
    if ($LASTEXITCODE -eq 0) {
        if ($gitStatus) {
            Write-Warning "コミットされていない変更があります:"
            Write-Host $gitStatus
            $continue = Read-Host "続行しますか? (y/n)"
            if ($continue -ne 'y') { exit 0 }
        } else {
            Write-Success "Git 状態確認: クリーン"
        }
    }
} catch {
    Write-Info "Git リポジトリではありません"
} finally {
    Pop-Location
}

# ============================================
# 2. バックアップ作成
# ============================================
if (-not $SkipBackup) {
    Write-Step "バックアップ作成"

    $BackupPath = "$SourcePath.backup_$(Get-Date -Format 'yyyyMMdd_HHmmss')"
    Write-Info "バックアップ先: $BackupPath"

    # 重要ファイルのみバックアップ（高速化）
    New-Item -ItemType Directory -Path $BackupPath -Force | Out-Null

    $BackupItems = @(
        "package.json",
        "package-lock.json",
        "next.config.ts",
        "tsconfig.json",
        ".env*",
        "src",
        "backend",
        ".git"
    )

    foreach ($item in $BackupItems) {
        $sourcePath = Join-Path $SourcePath $item
        if (Test-Path $sourcePath) {
            Copy-Item -Path $sourcePath -Destination $BackupPath -Recurse -Force
            Write-Success "バックアップ: $item"
        }
    }

    Write-Success "バックアップ完了: $BackupPath"
}

# ============================================
# 3. 移行先ディレクトリの準備
# ============================================
Write-Step "移行先ディレクトリの準備"

if (Test-Path $DestinationPath) {
    Write-Warning "移行先ディレクトリが既に存在します: $DestinationPath"
    $overwrite = Read-Host "上書きしますか? (y/n)"
    if ($overwrite -ne 'y') { exit 0 }
    Remove-Item -Path $DestinationPath -Recurse -Force
}

$DestParent = Split-Path $DestinationPath -Parent
if (-not (Test-Path $DestParent)) {
    New-Item -ItemType Directory -Path $DestParent -Force | Out-Null
    Write-Success "親ディレクトリ作成: $DestParent"
}

# ============================================
# 4. ファイルのコピー（除外リスト付き）
# ============================================
Write-Step "プロジェクトファイルのコピー"

$ExcludeDirs = @(
    'node_modules',
    '.next',
    'out',
    'build',
    'dist',
    'target',
    '.git',
    '.vscode',
    '.idea',
    'coverage',
    '*.backup_*'
)

Write-Info "除外ディレクトリ: $($ExcludeDirs -join ', ')"

# robocopy を使用した高速コピー
$RobocopyArgs = @(
    $SourcePath,
    $DestinationPath,
    '/MIR',  # ミラーコピー
    '/MT:8', # マルチスレッド
    '/R:2',  # リトライ回数
    '/W:5',  # リトライ間隔
    '/NDL',  # ディレクトリリストを表示しない
    '/NJH',  # ジョブヘッダーを表示しない
    '/NJS'   # ジョブサマリーを表示しない
)

# 除外パターンの追加
foreach ($exclude in $ExcludeDirs) {
    $RobocopyArgs += "/XD"
    $RobocopyArgs += $exclude
}

Write-Info "ファイルコピー開始（数分かかる場合があります）..."
$result = & robocopy @RobocopyArgs

# robocopy の終了コード確認（8未満は成功）
if ($LASTEXITCODE -lt 8) {
    Write-Success "ファイルコピー完了"
} else {
    Write-Error "ファイルコピー中にエラーが発生しました（終了コード: $LASTEXITCODE）"
    exit 1
}

# ============================================
# 5. .next と node_modules の完全削除
# ============================================
Write-Step "キャッシュディレクトリのクリーンアップ"

$CacheDirs = @(
    "$DestinationPath\.next",
    "$DestinationPath\node_modules",
    "$DestinationPath\.turbo",
    "$DestinationPath\backend\target"
)

foreach ($dir in $CacheDirs) {
    if (Test-Path $dir) {
        Remove-Item -Path $dir -Recurse -Force -ErrorAction SilentlyContinue
        Write-Success "削除: $dir"
    }
}

# ============================================
# 6. 環境変数ファイルの確認と作成
# ============================================
Write-Step "環境変数ファイルの確認"

$EnvFiles = @(
    @{Path="$DestinationPath\.env.local"; Required=$false},
    @{Path="$DestinationPath\backend\.env"; Required=$false}
)

foreach ($envFile in $EnvFiles) {
    if (Test-Path $envFile.Path) {
        Write-Success "環境ファイル存在: $($envFile.Path)"
    } elseif ($envFile.Required) {
        Write-Warning "必須環境ファイルが見つかりません: $($envFile.Path)"
    } else {
        Write-Info "環境ファイルなし（オプション）: $($envFile.Path)"
    }
}

# ============================================
# 7. package.json のパス参照チェック
# ============================================
Write-Step "設定ファイルのパス参照チェック"

Push-Location $DestinationPath
try {
    $PackageJson = Get-Content "package.json" -Raw | ConvertFrom-Json

    # scripts 内の絶対パス検出
    $hasAbsolutePath = $false
    foreach ($script in $PackageJson.scripts.PSObject.Properties) {
        if ($script.Value -match '[A-Z]:\\') {
            Write-Warning "絶対パスを検出: $($script.Name) = $($script.Value)"
            $hasAbsolutePath = $true
        }
    }

    if (-not $hasAbsolutePath) {
        Write-Success "package.json: パス参照問題なし"
    }
} finally {
    Pop-Location
}

# ============================================
# 8. npm install の実行
# ============================================
Write-Step "依存関係のインストール"

Push-Location $DestinationPath
try {
    Write-Info "npm install を実行中（数分かかります）..."

    # npm キャッシュのクリア
    npm cache clean --force 2>$null

    # npm install 実行
    $npmOutput = npm install 2>&1

    if ($LASTEXITCODE -eq 0) {
        Write-Success "npm install 完了"
    } else {
        Write-Error "npm install に失敗しました"
        Write-Host $npmOutput
        exit 1
    }
} finally {
    Pop-Location
}

# ============================================
# 9. ビルドテスト
# ============================================
Write-Step "ビルドテスト"

$buildTest = Read-Host "ビルドテストを実行しますか? (y/n)"
if ($buildTest -eq 'y') {
    Push-Location $DestinationPath
    try {
        Write-Info "npm run build を実行中..."

        $buildOutput = npm run build 2>&1

        if ($LASTEXITCODE -eq 0) {
            Write-Success "ビルド成功！"
        } else {
            Write-Error "ビルドに失敗しました。エラーログを確認してください。"
            Write-Host $buildOutput
        }
    } finally {
        Pop-Location
    }
}

# ============================================
# 10. 最終確認とクリーンアップ
# ============================================
Write-Step "移行完了サマリー"

Write-Success "プロジェクト移行が完了しました"
Write-Info "移行元: $SourcePath"
Write-Info "移行先: $DestinationPath"

if (-not $SkipBackup) {
    Write-Info "バックアップ: $BackupPath"
}

Write-Host ""
Write-ColorOutput "次のステップ:" "Yellow"
Write-Host "1. cd `"$DestinationPath`""
Write-Host "2. npm run dev"
Write-Host "3. ブラウザで http://localhost:3000 を開く"
Write-Host ""

# OneDrive 同期除外の推奨
Write-ColorOutput "重要: OneDrive 同期設定の確認" "Yellow"
Write-Host "移行元ディレクトリ ($SourcePath) は OneDrive の同期対象のままです。"
Write-Host "移行が完全に成功したら、OneDrive から除外するか削除することを推奨します。"
Write-Host ""

# 自動クリーンアップ
if ($AutoClean) {
    $clean = 'y'
} else {
    $clean = Read-Host "移行元の .next と node_modules を削除しますか? (y/n)"
}

if ($clean -eq 'y') {
    $CleanDirs = @(
        "$SourcePath\.next",
        "$SourcePath\node_modules"
    )

    foreach ($dir in $CleanDirs) {
        if (Test-Path $dir) {
            Remove-Item -Path $dir -Recurse -Force -ErrorAction SilentlyContinue
            Write-Success "クリーンアップ: $dir"
        }
    }
}

Write-Host ""
Write-Success "すべての処理が完了しました！"

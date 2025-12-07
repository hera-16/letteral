# 大容量アプリケーション Dドライブ移行ガイド

## 📊 現状分析

### Cドライブの使用状況
- **使用中**: 185.39 GB / 189.29 GB (98%)
- **空き**: わずか **3.9 GB** ⚠️
- **Dドライブの空き**: 278.92 GB ✅

### 主な容量消費元

#### AppData\Local (30.19GB)
| フォルダ | サイズ |
|---------|-------|
| Docker | 7.19 GB |
| Packages | 4.45 GB |
| Microsoft | 2.66 GB |
| LINE | 1.29 GB |
| Programs | 1.15 GB |

#### Program Files (41.25GB)
| アプリケーション | サイズ |
|----------------|-------|
| Common Files | 9.27 GB |
| Microsoft Visual Studio | 7.94 GB |
| Wolfram Research | 5.92 GB |
| Docker | 3.55 GB |
| Microsoft Office | 3.3 GB |

#### Program Files (x86) (22.19GB)
| アプリケーション | サイズ |
|----------------|-------|
| Microsoft SDKs | 5.52 GB |
| Microsoft | 4.2 GB |
| Windows Kits | 3.47 GB |
| Android SDK | 3.18 GB |

## 🎯 移行計画

以下の大容量アプリをDドライブに移行することで、約**21GB**の空き容量を確保できます：

1. **Docker Desktop** (10.74GB)
   - AppData\Local\Docker: 7.19GB
   - Program Files\Docker: 3.55GB

2. **Wolfram Research/Mathematica** (5.92GB)

3. **Android SDK** (3.18GB)

4. **LINE** (1.29GB)

移行後の予想：
- Cドライブの空き: **3.9GB → 約25GB** 🎉
- Dドライブの使用: 7.24GB → 約28GB

## 📋 移行手順

### ステップ1: 準備

#### 1.1 アプリケーションを終了

**必ず**以下のアプリケーションを終了してください：
- Docker Desktop（タスクトレイから「Quit Docker Desktop」）
- LINE
- Android Studio（使用している場合）

タスクマネージャーで以下のプロセスが終了していることを確認：
```powershell
Get-Process | Where-Object {$_.Name -like "*docker*" -or $_.Name -like "*line*"}
```

#### 1.2 バックアップ作成（推奨）

```powershell
# Dドライブにバックアップフォルダを作成
$backupDate = Get-Date -Format 'yyyyMMdd_HHmmss'
New-Item -ItemType Directory -Path "D:\Backup_CriticalApps_$backupDate" -Force
```

### ステップ2: 移行スクリプトの実行

#### 2.1 PowerShellを管理者として起動

1. **Windows検索で「PowerShell」を検索**
2. **右クリック → 「管理者として実行」**

#### 2.2 スクリプトを実行

```powershell
# プロジェクトフォルダに移動
cd "c:\Users\User\OneDrive\hera-16\チャレキャラ"

# 移行スクリプトを実行
.\migrate_apps_to_d.ps1
```

スクリプトの処理内容：
1. ✅ Dドライブに移行先フォルダを作成
2. 📦 各アプリケーションのフォルダをDドライブにコピー
3. 🗑️ 元のフォルダを削除
4. 🔗 シンボリックリンクを作成（透過的にアクセス可能）

#### 2.3 確認プロンプト

スクリプトは移行前に以下を表示します：
```
移行計画:
✓ Docker Desktop (AppData): 7.19 GB
✓ Wolfram Research: 5.92 GB
✓ Docker Desktop (Program Files): 3.55 GB
✓ Android SDK: 3.18 GB
✓ LINE: 1.29 GB

合計移行容量: 21.13 GB

この操作を実行しますか？ (Y/N)
```

**Y** を入力して Enter を押してください。

### ステップ3: 動作確認

移行完了後、各アプリケーションを起動して正常動作を確認：

#### 3.1 Docker Desktop

```powershell
# Docker Desktopを起動

# コマンドで動作確認
docker version
docker ps
docker images
```

#### 3.2 LINE

1. LINEアプリを起動
2. ログイン状態が保持されているか確認
3. トーク履歴が表示されるか確認

#### 3.3 Mathematica（使用している場合）

1. Mathematicaを起動
2. ライセンスが有効か確認
3. ノートブックが開けるか確認

#### 3.4 Android Studio（使用している場合）

```powershell
# Android Studioを起動
# Settings → Appearance & Behavior → System Settings → Android SDK
# SDK Locationが正しいか確認
```

### ステップ4: 容量確認

```powershell
# 容量を再確認
.\check_sizes.ps1
```

## 🔗 シンボリックリンクについて

このスクリプトは**シンボリックリンク**（SymLink）を使用します：

### シンボリックリンクとは？

- Windowsのショートカットのような仕組み
- 元のパス（例: `C:\Program Files\Docker`）は実際には`D:\Program Files\Docker`を指す
- アプリケーションからは**元の場所にあるように見える**
- 完全に透過的に動作（設定変更不要）

### シンボリックリンクの確認方法

```powershell
# リンクの詳細を確認
Get-Item "C:\Program Files\Docker" | Select-Object LinkType, Target
Get-Item "C:\Users\User\AppData\Local\Docker" | Select-Object LinkType, Target
```

出力例：
```
LinkType      Target
--------      ------
SymbolicLink  D:\Program Files\Docker
```

### エクスプローラーでの表示

エクスプローラーでシンボリックリンクフォルダには小さなショートカットアイコンが表示されます。

## 🔧 トラブルシューティング

### Docker Desktopが起動しない

#### 原因1: Docker サービスが起動していない

```powershell
# サービスの状態確認
Get-Service com.docker.service

# サービスを起動
Start-Service com.docker.service
```

#### 原因2: WSL2の設定が必要

Docker DesktopはWSL2を使用します。WSL2のデータは別の場所にあるため、追加の設定が必要な場合があります。

Docker Desktop の Settings → Resources で以下を確認：
- Disk image location
- WSL2 distributions

#### 解決策: データパスを明示的に設定

Docker Desktopの設定ファイルを編集：
```json
// C:\Users\User\AppData\Roaming\Docker\settings.json
{
  "dataFolder": "D:\\AppData\\Docker\\wsl\\data"
}
```

### LINEのトーク履歴が見つからない

#### 解決策1: LINE を再起動

```powershell
# LINEプロセスを終了
Stop-Process -Name LINE -Force

# LINEを再起動
Start-Process "C:\Users\User\AppData\Local\LINE\bin\LineLauncher.exe"
```

#### 解決策2: キャッシュをクリア

LINE設定 → トーク → トークルームの削除 → キャッシュデータを削除

### Android Studioが SDK を認識しない

#### 解決策: SDK Locationを再設定

1. Android Studio を起動
2. File → Settings (Ctrl+Alt+S)
3. Appearance & Behavior → System Settings → Android SDK
4. Android SDK Location を確認・再設定：
   ```
   C:\Program Files (x86)\Android\android-sdk
   ```

### シンボリックリンクが正しく作成されていない

```powershell
# リンクの確認
$link = Get-Item "C:\Program Files\Docker"
if ($link.Attributes -band [System.IO.FileAttributes]::ReparsePoint) {
    Write-Host "シンボリックリンクは正しく作成されています" -ForegroundColor Green
} else {
    Write-Host "シンボリックリンクが作成されていません" -ForegroundColor Red
}
```

### 完全に元に戻したい場合

各アプリケーションについて以下を実行：

```powershell
# 例: Docker Desktop (AppData) を元に戻す

# 1. シンボリックリンクを削除
Remove-Item "C:\Users\User\AppData\Local\Docker" -Force

# 2. Dドライブから元の場所にコピー
Copy-Item "D:\AppData\Docker" "C:\Users\User\AppData\Local\Docker" -Recurse
```

## 📌 追加の容量削減方法

### 1. 一時ファイルのクリーンアップ

```powershell
# Tempフォルダをクリーンアップ (0.69GB)
Remove-Item "C:\Users\User\AppData\Local\Temp\*" -Recurse -Force -ErrorAction SilentlyContinue

# Windowsの一時ファイル
Remove-Item "C:\Windows\Temp\*" -Recurse -Force -ErrorAction SilentlyContinue
```

### 2. Windows Updateのクリーンアップ

```powershell
# ディスククリーンアップ（管理者権限必須）
cleanmgr /sageset:1
cleanmgr /sagerun:1
```

または、設定アプリから：
1. 設定 → システム → ストレージ
2. 「一時ファイル」をクリック
3. 削除するファイルを選択
4. 「ファイルの削除」

### 3. Packagesフォルダの確認

`C:\Users\User\AppData\Local\Packages` (4.45GB) の内容確認：

```powershell
# Packagesフォルダの詳細
Get-ChildItem "C:\Users\User\AppData\Local\Packages" -Directory |
    ForEach-Object {
        $size = (Get-ChildItem $_.FullName -Recurse -ErrorAction SilentlyContinue |
                 Measure-Object -Property Length -Sum).Sum
        [PSCustomObject]@{
            Name = $_.Name
            SizeMB = [math]::Round($size/1MB, 2)
        }
    } |
    Where-Object {$_.SizeMB -gt 100} |
    Sort-Object SizeMB -Descending
```

不要なWindowsストアアプリをアンインストール：
1. 設定 → アプリ → インストールされているアプリ
2. 不要なアプリを選択して「アンインストール」

### 4. Microsoft Packages (2.66GB) の確認

```powershell
Get-ChildItem "C:\Users\User\AppData\Local\Microsoft" -Directory |
    ForEach-Object {
        $size = (Get-ChildItem $_.FullName -Recurse -ErrorAction SilentlyContinue |
                 Measure-Object -Property Length -Sum).Sum
        [PSCustomObject]@{
            Name = $_.Name
            SizeGB = [math]::Round($size/1GB, 2)
        }
    } |
    Where-Object {$_.SizeGB -gt 0.1} |
    Sort-Object SizeGB -Descending
```

### 5. Visual Studioのクリーンアップ

Visual Studio (7.94GB) を使用していない場合：

1. 設定 → アプリ → インストールされているアプリ
2. 「Visual Studio」を検索
3. アンインストール

または、一部のコンポーネントだけをアンインストール：
- Visual Studio Installer を起動
- 「変更」→ 不要なワークロードを削除

## ⚠️ 注意事項

### 移行してはいけないもの

以下は**移行しないでください**：

❌ `C:\Windows` - Windowsシステムフォルダ
❌ `C:\Users\User\AppData\Roaming\Microsoft\Windows` - Windows設定
❌ `C:\Program Files\WindowsApps` - Windowsストアアプリ
❌ Microsoft Office（再インストール推奨）
❌ Microsoft Visual Studio（再インストール推奨）

### バックアップの重要性

⚠️ **重要**: 移行前に必ずバックアップを取ってください

- 特に重要なデータは複数の場所にバックアップ
- OneDrive, Google Drive などのクラウドストレージも活用
- 外付けHDD/SSDへの定期バックアップを推奨

### 管理者権限

シンボリックリンクの作成には**管理者権限が必須**です。

### アプリケーションの終了

移行中は対象アプリケーションを**完全に終了**してください。
バックグラウンドプロセスもタスクマネージャーで確認。

## 📊 期待される効果

| 項目 | 移行前 | 移行後 | 変化 |
|------|--------|--------|------|
| Cドライブ空き | 3.9 GB | 約25 GB | **+21 GB** ✅ |
| Cドライブ使用率 | 98% | 87% | **-11%** ✅ |
| Dドライブ使用 | 7.24 GB | 約28 GB | +21 GB |
| Dドライブ使用率 | 2.5% | 9.8% | +7.3% |

## 📚 よくある質問

### Q1: シンボリックリンクはショートカットと同じ？

**A**: いいえ、異なります。
- ショートカット：アプリケーションレベルの参照
- シンボリックリンク：ファイルシステムレベルの透過的なリンク

アプリケーションからはシンボリックリンクは元のフォルダと**完全に同じ**に見えます。

### Q2: パフォーマンスへの影響は？

**A**: CドライブとDドライブが両方とも同じ物理ドライブ（SSD）上のパーティションであれば、パフォーマンスへの影響はほぼありません。

### Q3: Docker イメージやコンテナはどうなる？

**A**: Docker のデータは維持されます。移行前に存在していたイメージ、コンテナ、ボリュームはすべてDドライブに移動され、そのまま使用できます。

### Q4: LINEのトーク履歴は消える？

**A**: いいえ、トーク履歴は維持されます。移行スクリプトはデータを完全にコピーします。

### Q5: 移行後にアンインストールする場合は？

**A**: 通常通りアンインストールできます。ただし、Dドライブのデータは手動で削除する必要があります。

```powershell
# 例: Docker をアンインストール後
Remove-Item "D:\Program Files\Docker" -Recurse -Force
Remove-Item "D:\AppData\Docker" -Recurse -Force
```

### Q6: Windowsの更新で問題は起きない？

**A**: シンボリックリンクはWindowsの標準機能なので、Windows Updateで問題が起きることはありません。

### Q7: 再起動後も有効？

**A**: はい、シンボリックリンクは再起動後も維持されます。

## 🆘 サポート

問題が発生した場合：

1. **エラーメッセージを確認**
   - PowerShellのエラーメッセージをコピー
   - スクリーンショットを保存

2. **イベントビューアを確認**
   - Windows ログ → アプリケーション
   - Windows ログ → システム

3. **シンボリックリンクの状態確認**
   ```powershell
   Get-ChildItem "C:\Program Files" | Where-Object {$_.Attributes -band [System.IO.FileAttributes]::ReparsePoint}
   ```

## 📝 移行チェックリスト

移行前：
- [ ] Docker Desktop を終了
- [ ] LINE を終了
- [ ] Android Studio を終了（使用している場合）
- [ ] タスクマネージャーで対象プロセスが終了していることを確認
- [ ] 重要なデータのバックアップ作成
- [ ] PowerShellを管理者として起動

移行中：
- [ ] スクリプト実行
- [ ] エラーメッセージがないことを確認
- [ ] 完了メッセージを確認

移行後：
- [ ] Docker Desktop の起動確認
- [ ] docker version コマンドの実行
- [ ] LINE の起動とトーク履歴の確認
- [ ] Mathematica の起動確認（使用している場合）
- [ ] Android Studio の起動とSDK確認（使用している場合）
- [ ] シンボリックリンクの確認
- [ ] Cドライブの空き容量確認

---

**最終更新**: 2025-11-25
**対象システム**: Windows 10/11
**前提条件**: 管理者権限、PowerShell 5.1以上

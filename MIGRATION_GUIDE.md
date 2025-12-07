# VSCode/Claude データをDドライブへ移行するガイド

## 概要

Cドライブの空き容量が少ない（約244MB）ため、VSCodeとClaude関連のデータ（合計約7.38GB）をDドライブ（空き約299GB）に移行します。

## 移行されるデータ

| 項目 | 元の場所 | 移行先 | サイズ |
|------|----------|--------|--------|
| VSCode ユーザーデータ | `C:\Users\User\AppData\Roaming\Code` | `D:\DevTools\VSCode\Code` | 4.02 GB |
| VSCode 拡張機能 | `C:\Users\User\.vscode` | `D:\DevTools\VSCode\.vscode` | 3.28 GB |
| Claude 設定 | `C:\Users\User\.claude` | `D:\DevTools\Claude\.claude` | 76.07 MB |

## 移行方法

### 前提条件

1. **管理者権限が必要です**
2. **VSCodeとClaudeを完全に終了してください**

### 手順

#### 1. VSCodeとClaudeを終了

タスクマネージャーで以下のプロセスが終了していることを確認：
- `Code.exe`
- `claude.exe`

#### 2. PowerShellを管理者として起動

1. スタートメニューで「PowerShell」を検索
2. 右クリック → **「管理者として実行」** を選択

#### 3. 移行スクリプトを実行

```powershell
cd "C:\Users\User\OneDrive\hera-16\チャレキャラ"
.\migrate_to_d_drive.ps1
```

スクリプトは以下を自動実行します：
1. 各ディレクトリのバックアップを作成
2. Dドライブへデータをコピー
3. 元の場所にシンボリックリンクを作成

#### 4. 動作確認

1. VSCodeを起動
   - 拡張機能が正常に動作するか確認
   - 設定が保持されているか確認
   - ワークスペースが開けるか確認

2. Claudeを起動
   - 設定が保持されているか確認
   - 以前の会話履歴が表示されるか確認

## トラブルシューティング

### 問題が発生した場合

バックアップから復元するスクリプトを用意しています：

```powershell
cd "C:\Users\User\OneDrive\hera-16\チャレキャラ"
.\restore_from_backup.ps1
```

このスクリプトは移行前の状態に戻します。

### 手動での復元方法

1. シンボリックリンクを削除
```powershell
Remove-Item "C:\Users\User\AppData\Roaming\Code" -Force
Remove-Item "C:\Users\User\.vscode" -Force
Remove-Item "C:\Users\User\.claude" -Force
```

2. バックアップから復元
```powershell
# VSCode ユーザーデータ
Copy-Item "C:\Users\User\AppData\Roaming\Code_backup_*" "C:\Users\User\AppData\Roaming\Code" -Recurse -Force

# VSCode 拡張機能
Copy-Item "C:\Users\User\.vscode_backup_*" "C:\Users\User\.vscode" -Recurse -Force

# Claude 設定
Copy-Item "C:\Users\User\.claude_backup_*" "C:\Users\User\.claude" -Recurse -Force
```

## シンボリックリンクの確認

移行後、シンボリックリンクが正しく作成されているか確認できます：

```powershell
Get-Item "C:\Users\User\AppData\Roaming\Code" | Select-Object LinkType, Target
Get-Item "C:\Users\User\.vscode" | Select-Object LinkType, Target
Get-Item "C:\Users\User\.claude" | Select-Object LinkType, Target
```

出力例：
```
LinkType      Target
--------      ------
SymbolicLink  D:\DevTools\VSCode\Code
```

## バックアップの削除

移行が成功し、問題なく動作することを数日間確認した後、バックアップを削除できます：

```powershell
# バックアップ一覧表示
Get-ChildItem "C:\Users\User\AppData\Roaming" -Filter "Code_backup_*"
Get-ChildItem "C:\Users\User" -Filter ".vscode_backup_*"
Get-ChildItem "C:\Users\User" -Filter ".claude_backup_*"

# 削除（確認後）
Remove-Item "C:\Users\User\AppData\Roaming\Code_backup_*" -Recurse -Force
Remove-Item "C:\Users\User\.vscode_backup_*" -Recurse -Force
Remove-Item "C:\Users\User\.claude_backup_*" -Recurse -Force
```

## 期待される効果

- **Cドライブの空き容量**: 約244MB → 約7.6GB
- **アプリケーション動作**: 変更なし（透過的に動作）
- **設定・拡張機能**: すべて保持
- **パフォーマンス**: 影響なし（Dドライブも同じSSDの場合）

## 注意事項

1. **バックアップは重要**: 移行前に自動でバックアップが作成されますが、重要なデータは別途バックアップを推奨
2. **管理者権限**: シンボリックリンク作成には管理者権限が必須
3. **アプリケーション終了**: 移行中はVSCodeとClaudeを完全に終了してください
4. **OneDrive同期**: OneDriveの同期対象にはなりません（必要に応じて別途設定）

## よくある質問

### Q: シンボリックリンクとは？
A: ファイルシステムのショートカットのようなもので、元の場所にアクセスすると自動的にDドライブの実際のデータを参照します。アプリケーションからは透過的に動作します。

### Q: 移行後、VSCodeの更新は問題ない？
A: はい、問題ありません。VSCodeは通常通り更新でき、データもDドライブに保存されます。

### Q: Dドライブのデータを直接編集できる？
A: できますが、通常はCドライブのパス経由でアクセスすることを推奨します。

### Q: 元に戻したい場合は？
A: `restore_from_backup.ps1` を実行するか、バックアップから手動でコピーします。

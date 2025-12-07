# Next.js プロジェクト移行ガイド
## OneDrive から通常フォルダへの完全移行マニュアル

---

## 📋 目次

1. [問題の概要](#問題の概要)
2. [移行前の準備](#移行前の準備)
3. [自動移行スクリプトの使用](#自動移行スクリプトの使用)
4. [手動移行手順](#手動移行手順)
5. [移行後のチェックリスト](#移行後のチェックリスト)
6. [トラブルシューティング](#トラブルシューティング)
7. [よくある質問](#よくある質問)

---

## 🔍 問題の概要

### 発生している問題

```
ENOENT: no such file or directory, open
'C:\Users\User\OneDrive\hera-16\チャレキャラ\.next\server\app\favicon.ico\[__metadata_id__]\route\app-paths-manifest.json'
```

### 原因

Next.js 15 の App Router が生成する **メタデータルート** (`.metadata` や `[__metadata_id__]` ディレクトリ) が、OneDrive の同期機構と衝突しています。

**なぜ OneDrive で問題が起きるのか:**

1. **ファイル名の制約**: OneDrive は特殊文字 (`[`, `]`, `__`) を含むファイル名を正しく処理できない
2. **同期の遅延**: `.next` フォルダ内の高速な読み書きに OneDrive の同期が追いつかない
3. **ロックの競合**: ビルドプロセスとクラウド同期プロセスが同時にファイルアクセスを試みる

### 解決策

プロジェクトを **OneDrive 管理外の通常フォルダ** (例: `C:\dev\`) に移動します。

---

## ✅ 移行前の準備

### 1. 前提条件の確認

```powershell
# Node.js のバージョン確認（18.17 以降が必要）
node --version

# npm のバージョン確認
npm --version

# Git の状態確認
cd "C:\Users\User\OneDrive\hera-16\チャレキャラ"
git status
```

### 2. 現在のプロジェクトの状態確認

```powershell
# ビルドが通るか確認（エラーが出ても OK）
npm run build

# 依存関係の整合性確認
npm list --depth=0
```

### 3. 重要データのバックアップ

以下のファイル・フォルダをバックアップしてください:

- ✅ `package.json` / `package-lock.json`
- ✅ `src/` ディレクトリ
- ✅ `backend/` ディレクトリ
- ✅ `.env.local` / `backend/.env`
- ✅ `next.config.ts` / `tsconfig.json`
- ✅ `.git/` フォルダ（Git 履歴）

**バックアップ不要なもの:**
- ❌ `node_modules/` （再インストール可能）
- ❌ `.next/` （再生成可能）
- ❌ `backend/target/` （再ビルド可能）

---

## 🚀 自動移行スクリプトの使用

### スクリプトの実行

#### 基本的な使用方法

```powershell
# PowerShell を管理者権限で起動
# スクリプトを実行
.\MIGRATION_SCRIPT.ps1
```

#### カスタムオプション付き実行

```powershell
# 移行先を指定
.\MIGRATION_SCRIPT.ps1 -DestinationPath "D:\Projects\チャレキャラ"

# バックアップをスキップ（高速化）
.\MIGRATION_SCRIPT.ps1 -SkipBackup

# 自動クリーンアップ有効化（確認なしで旧ディレクトリをクリーン）
.\MIGRATION_SCRIPT.ps1 -AutoClean
```

### スクリプトの処理内容

スクリプトは以下の処理を自動で実行します:

1. ✅ **事前チェック**
   - ソースディレクトリの存在確認
   - package.json の確認
   - ディスク容量チェック
   - Git の状態確認

2. ✅ **バックアップ作成**
   - 重要ファイルを `.backup_YYYYMMDD_HHmmss` フォルダにコピー

3. ✅ **ファイルコピー**
   - `robocopy` を使用した高速コピー
   - `node_modules`, `.next`, `target` を除外

4. ✅ **キャッシュクリーンアップ**
   - `.next/`, `node_modules/`, `.turbo/` の削除

5. ✅ **環境ファイル確認**
   - `.env.local`, `backend/.env` の存在チェック

6. ✅ **依存関係インストール**
   - `npm install` の実行

7. ✅ **ビルドテスト（オプション）**
   - `npm run build` でエラーがないか確認

---

## 🛠️ 手動移行手順

スクリプトを使用しない場合の手動手順です。

### ステップ 1: 移行先ディレクトリの作成

```powershell
# 移行先ディレクトリを作成
New-Item -ItemType Directory -Path "C:\dev\チャレキャラ" -Force
```

### ステップ 2: ファイルのコピー

```powershell
# robocopy を使用した高速コピー
robocopy "C:\Users\User\OneDrive\hera-16\チャレキャラ" "C:\dev\チャレキャラ" /MIR /MT:8 /XD node_modules .next out build dist target .git .vscode .idea coverage

# または xcopy を使用
xcopy "C:\Users\User\OneDrive\hera-16\チャレキャラ" "C:\dev\チャレキャラ" /E /H /C /I /EXCLUDE:exclude.txt
```

### ステップ 3: 不要なキャッシュの削除

```powershell
cd "C:\dev\チャレキャラ"

# キャッシュディレクトリを削除
Remove-Item -Path ".next" -Recurse -Force -ErrorAction SilentlyContinue
Remove-Item -Path "node_modules" -Recurse -Force -ErrorAction SilentlyContinue
Remove-Item -Path ".turbo" -Recurse -Force -ErrorAction SilentlyContinue
Remove-Item -Path "backend\target" -Recurse -Force -ErrorAction SilentlyContinue
```

### ステップ 4: 依存関係の再インストール

```powershell
# npm キャッシュのクリア
npm cache clean --force

# 依存関係のインストール
npm install
```

### ステップ 5: 環境変数ファイルの確認

```powershell
# .env.local の存在確認
Test-Path ".env.local"

# backend/.env の存在確認
Test-Path "backend\.env"

# 必要に応じて作成
# New-Item -ItemType File -Path ".env.local"
```

### ステップ 6: ビルドテスト

```powershell
# Next.js のビルド
npm run build

# 成功したら .next フォルダが生成される
ls .next
```

### ステップ 7: 開発サーバーの起動

```powershell
npm run dev
```

ブラウザで [http://localhost:3000](http://localhost:3000) を開いて動作確認。

---

## 📝 移行後のチェックリスト

### 必須チェック項目

- [ ] **プロジェクトが正常にビルドできる**
  ```powershell
  npm run build
  ```

- [ ] **開発サーバーが起動する**
  ```powershell
  npm run dev
  ```

- [ ] **ブラウザでアプリケーションが表示される**
  - URL: http://localhost:3000

- [ ] **API エンドポイントが正常に動作する**
  - バックエンド起動確認
  ```powershell
  cd backend
  ../mvnw.cmd spring-boot:run
  ```

- [ ] **環境変数が正しく読み込まれている**
  - `.env.local` の内容確認
  - `backend/.env` の内容確認

- [ ] **Git 履歴が保持されている**
  ```powershell
  git log --oneline -10
  ```

### パフォーマンスチェック

- [ ] **ビルド時間の改善**
  ```powershell
  # ビルド時間を測定
  Measure-Command { npm run build }
  ```
  - OneDrive 配下: 通常 2-5 分
  - 通常フォルダ: 通常 30 秒 - 2 分

- [ ] **HMR（Hot Module Replacement）の動作確認**
  - ファイルを編集して即座に反映されるか確認

### 設定ファイルのチェック

#### next.config.ts

```typescript
// 絶対パスが含まれていないか確認
import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  // ✅ 相対パスのみ使用
  // ❌ 'C:\\Users\\...' のような絶対パスは NG
};
```

#### package.json

```json
{
  "scripts": {
    // ✅ 相対パスのみ
    "dev": "next dev --turbopack",

    // ❌ 絶対パスは NG
    // "dev": "C:\\Users\\...\\node_modules\\next\\dist\\bin\\next dev"
  }
}
```

#### tsconfig.json

```json
{
  "compilerOptions": {
    "baseUrl": ".",  // ✅ カレントディレクトリ基準
    "paths": {
      "@/*": ["./src/*"]  // ✅ 相対パス
    }
  }
}
```

---

## 🔧 トラブルシューティング

### 問題 1: `npm install` が失敗する

#### エラー例
```
npm ERR! code ENOENT
npm ERR! syscall open
npm ERR! path C:\dev\チャレキャラ\package.json
npm ERR! errno -4058
```

#### 解決方法

```powershell
# 作業ディレクトリを確認
pwd

# package.json が存在するか確認
Test-Path package.json

# npm キャッシュをクリア
npm cache clean --force

# node_modules と package-lock.json を削除して再試行
Remove-Item -Path "node_modules" -Recurse -Force
Remove-Item -Path "package-lock.json" -Force
npm install
```

---

### 問題 2: `npm run build` でメタデータエラーが残る

#### エラー例
```
Error: ENOENT: no such file or directory, open '.next\server\app\...\[__metadata_id__]\...'
```

#### 解決方法

```powershell
# .next を完全削除
Remove-Item -Path ".next" -Recurse -Force

# Next.js のキャッシュもクリア
Remove-Item -Path ".turbo" -Recurse -Force

# 再ビルド
npm run build
```

**それでも解決しない場合:**

```powershell
# Turbopack を無効化してビルド
npm run build -- --no-turbo

# または package.json を編集
# "build": "next build" （--turbopack を削除）
```

---

### 問題 3: 環境変数が読み込まれない

#### 症状
- API エンドポイントが 404 エラー
- データベース接続エラー

#### 解決方法

```powershell
# .env.local の確認
Get-Content .env.local

# バックエンド環境変数の確認
Get-Content backend\.env

# Next.js で環境変数を確認（ブラウザコンソール）
# console.log(process.env.NEXT_PUBLIC_API_URL)
```

**環境変数が未設定の場合:**

```powershell
# .env.local を作成
@"
NEXT_PUBLIC_API_URL=http://localhost:8080
NEXT_PUBLIC_WS_URL=ws://localhost:8080/ws
"@ | Out-File -FilePath ".env.local" -Encoding utf8
```

---

### 問題 4: バックエンド（Spring Boot）が起動しない

#### エラー例
```
Error: Could not find or load main class com.chatapp.ChatAppApplication
```

#### 解決方法

```powershell
cd backend

# Maven のクリーンビルド
..\mvnw.cmd clean compile

# target ディレクトリが生成されたか確認
Test-Path target

# アプリケーション起動
..\mvnw.cmd spring-boot:run
```

---

### 問題 5: ポート競合エラー

#### エラー例
```
Error: listen EADDRINUSE: address already in use :::3000
```

#### 解決方法

```powershell
# ポート 3000 を使用しているプロセスを確認
netstat -ano | findstr :3000

# プロセスを終了（PID を確認してから）
taskkill /F /PID <プロセスID>

# または別のポートで起動
npm run dev -- -p 3001
```

---

### 問題 6: Git 履歴が失われた

#### 確認方法
```powershell
git log
# fatal: not a git repository
```

#### 解決方法

```powershell
# .git フォルダをコピーし忘れた場合
# バックアップから復元
Copy-Item -Path "C:\Users\User\OneDrive\hera-16\チャレキャラ.backup_*\.git" -Destination "C:\dev\チャレキャラ\.git" -Recurse

# または移行元からコピー
Copy-Item -Path "C:\Users\User\OneDrive\hera-16\チャレキャラ\.git" -Destination "C:\dev\チャレキャラ\.git" -Recurse
```

---

### 問題 7: Windows の長いパス名エラー

#### エラー例
```
ENAMETOOLONG: name too long, open 'C:\dev\チャレキャラ\node_modules\...'
```

#### 解決方法

```powershell
# Windows の長いパス名サポートを有効化（管理者権限が必要）
New-ItemProperty -Path "HKLM:\SYSTEM\CurrentControlSet\Control\FileSystem" -Name "LongPathsEnabled" -Value 1 -PropertyType DWORD -Force

# コンピュータを再起動
Restart-Computer
```

---

### 問題 8: TypeScript のパス解決エラー

#### エラー例
```
Cannot find module '@/components/...' or its corresponding type declarations
```

#### 解決方法

```powershell
# tsconfig.json の確認
Get-Content tsconfig.json

# baseUrl と paths の設定を確認
# {
#   "compilerOptions": {
#     "baseUrl": ".",
#     "paths": {
#       "@/*": ["./src/*"]
#     }
#   }
# }

# TypeScript コンパイラのキャッシュクリア
Remove-Item -Path ".next" -Recurse -Force
Remove-Item -Path "tsconfig.tsbuildinfo" -Force

# 再ビルド
npm run build
```

---

## ❓ よくある質問

### Q1: 移行後、元の OneDrive フォルダは削除して良い？

**A:** 移行が完全に成功し、以下を確認してから削除してください:

1. ✅ `npm run build` が成功する
2. ✅ `npm run dev` で正常に起動する
3. ✅ Git 履歴が保持されている
4. ✅ 環境変数ファイルがコピーされている

削除前に最終バックアップを推奨します。

---

### Q2: OneDrive の同期を除外する設定はできる？

**A:** はい、OneDrive の設定で特定フォルダを同期除外できます:

1. タスクバーの OneDrive アイコンを右クリック
2. 「設定」→「同期とバックアップ」→「同期を管理」
3. 「チャレキャラ」フォルダのチェックを外す

ただし、プロジェクト全体を OneDrive から出す方が推奨されます。

---

### Q3: 移行後もビルドが遅い場合は？

**A:** 以下を試してください:

```powershell
# 1. Turbopack を有効化（Next.js 15 の新機能）
# package.json で既に設定済み
"dev": "next dev --turbopack",
"build": "next build --turbopack"

# 2. SWC を最新版に更新
npm install @swc/core@latest

# 3. ウイルススキャンの除外設定
# Windows Defender で node_modules と .next を除外

# 4. SSD の使用確認
Get-PhysicalDisk
```

---

### Q4: Docker を使用している場合の注意点は？

**A:** `docker-compose.yml` のボリュームマウントを確認してください:

```yaml
services:
  backend:
    volumes:
      # ❌ 絶対パスは移行前のまま
      - C:\Users\User\OneDrive\hera-16\チャレキャラ\backend:/app

      # ✅ 相対パスに変更
      - ./backend:/app
```

---

### Q5: プロジェクト名に日本語が含まれていても問題ない？

**A:** 基本的には問題ありませんが、以下に注意:

- ✅ Windows 10/11 の NTFS ファイルシステムは日本語をサポート
- ⚠️ 一部の古い npm パッケージは日本語パスで問題が起きる可能性あり
- ⚠️ CI/CD パイプライン（GitHub Actions など）で日本語パスが使えない場合あり

**推奨:** 可能であれば英数字のディレクトリ名を使用
```
C:\dev\チャレキャラ  →  C:\dev\chalechara
```

---

## 📊 移行前後の比較

| 項目 | OneDrive 配下 | 通常フォルダ | 改善率 |
|------|---------------|-------------|--------|
| `npm install` | 3-5 分 | 1-2 分 | **50-60%** |
| `npm run build` | 2-5 分 | 30 秒 - 2 分 | **60-75%** |
| HMR（変更反映） | 3-10 秒 | 1-2 秒 | **70-80%** |
| ファイル作成/削除 | 遅延あり | 即座 | **90%+** |
| ビルドエラー発生率 | 高 | 低 | - |

---

## 🎯 移行後の最終確認コマンド

すべての移行作業が完了したら、以下のコマンドで最終確認してください:

```powershell
# 1. ディレクトリ移動
cd "C:\dev\チャレキャラ"

# 2. 依存関係の確認
npm list --depth=0

# 3. ビルド確認
npm run build

# 4. 開発サーバー起動
npm run dev

# 5. 別ターミナルでバックエンド起動
cd backend
..\mvnw.cmd spring-boot:run

# 6. ブラウザで動作確認
# http://localhost:3000
# http://localhost:8080/actuator/health
```

---

## 📞 サポート情報

### 問題が解決しない場合

1. **ログの収集**
   ```powershell
   # ビルドログを保存
   npm run build > build.log 2>&1

   # エラー内容を確認
   Get-Content build.log
   ```

2. **環境情報の確認**
   ```powershell
   node --version
   npm --version
   Get-ComputerInfo | Select-Object CsName, OsArchitecture, OsVersion
   ```

3. **クリーンインストール**
   ```powershell
   Remove-Item -Path "node_modules" -Recurse -Force
   Remove-Item -Path ".next" -Recurse -Force
   Remove-Item -Path "package-lock.json" -Force
   npm cache clean --force
   npm install
   npm run build
   ```

---

## ✅ 移行完了チェックシート

最後に、このチェックシートを使って移行が完全に完了したか確認してください:

- [ ] プロジェクトファイルが `C:\dev\` 配下にコピーされた
- [ ] `.next` と `node_modules` が削除・再生成された
- [ ] `npm install` が正常に完了した
- [ ] `npm run build` が成功した
- [ ] `npm run dev` でアプリケーションが起動した
- [ ] ブラウザで http://localhost:3000 が表示された
- [ ] バックエンドが正常に起動した（該当する場合）
- [ ] 環境変数ファイルが正しくコピーされた
- [ ] Git 履歴が保持されている
- [ ] ビルド時間が改善した
- [ ] HMR（Hot Module Replacement）が高速になった
- [ ] OneDrive 配下のバックアップが作成された
- [ ] 移行元の不要なキャッシュを削除した

**すべてチェックが付いたら移行完了です！お疲れ様でした。**

---

## 📚 参考資料

- [Next.js App Router ドキュメント](https://nextjs.org/docs/app)
- [OneDrive とビルドツールの競合に関する Issue](https://github.com/vercel/next.js/issues/54566)
- [Windows 長いパス名サポート](https://learn.microsoft.com/ja-jp/windows/win32/fileio/maximum-file-path-limitation)
- [Node.js ベストプラクティス](https://github.com/goldbergyoni/nodebestpractices)

---

**作成日**: 2025-11-25
**対象バージョン**: Next.js 15.5.4, Node.js 18.17+
**最終更新**: 2025-11-25

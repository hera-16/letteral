# Letteral - 企業向け匿名進捗・目標開示プラットフォーム

> 匿名性を担保しながら、日々の目標と進捗、課題・悩みを安全に共有できる業務ツール

[![Next.js](https://img.shields.io/badge/Next.js-15.5.4-black?logo=next.js)](https://nextjs.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.6-brightgreen?logo=spring)](https://spring.io/projects/spring-boot)
[![TypeScript](https://img.shields.io/badge/TypeScript-5.0-blue?logo=typescript)](https://www.typescriptlang.org/)
[![Java](https://img.shields.io/badge/Java-17-orange?logo=openjdk)](https://openjdk.org/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

---

## 📋 目次

- [概要](#概要)
- [背景・課題](#背景課題)
- [提供価値](#提供価値)
- [主要機能](#主要機能)
- [技術スタック](#技術スタック)
- [クイックスタート](#クイックスタート)
- [ドキュメント](#ドキュメント)
- [ロードマップ](#ロードマップ)

---

## 概要

**Letteral** は、組織での日々の進捗共有・目標開示・悩み相談を匿名で安全に行える企業向けプラットフォームです。

従来のリアルタイムチャット機能を活かしつつ、**匿名性**と**階層的な公開範囲制御**を組み合わせることで、心理的安全性を保ちながら透明性のあるコミュニケーションを実現します。

### 対象ユーザー

- 中小〜大企業
- 学校法人・研究室
- 学生団体
- その他組織利用

---

## 背景・課題

現代の組織では、以下のような課題が存在します：

- **進捗・目標の共有が属人化**し、会議コストや心理的安全性の低さから本音が出にくい
- **評価・1on1の材料が散在**し、事実ベースの対話が難しい
- **若手・新卒・インターンの悩みが表出しづらい**（ハラスメント・労働環境などの早期検知も困難）

Letteralは、これらの課題を**匿名性**と**構造化された進捗記録**で解決します。

---

## 提供価値

### 👔 会社・人事
- 早期に組織課題を可視化し、離職リスクを低減
- 評価の透明性と記録性を向上

### 👨‍💼 管理職
- 1on1・評価面談で使える「週次ダイジェスト」と「貢献履歴」を自動生成
- 部下の状況をリアルタイムで把握

### 👤 従業員
- 匿名で安全に本音を共有
- OKR/KGI/KPIとのひも付けで成長実感を得られる

---

## 主要機能

### 🎯 匿名目標・進捗投稿

- **テンプレート投稿**: 今日の目標 / 達成度 / ブロッカー / 学び / 次の一手
- **OKR連携**: Objective/Key Resultsとタグで紐づけ、期間別に自動集計
- **スレッド＆リアクション**: 称賛・相談・補助リソース提案
- **添付機能**: 画像・リンク・ドキュメント、社内ナレッジとの連携

### 🏢 階層・公開範囲・ホスト権限

- **階層設定**: 会社 → 本部 → 部 → 課 → チーム → 同期（任意深度）
- **公開範囲**: 全社 / 部門内 / チーム内 / 同期のみ から選択
- **匿名度**: 完全匿名 / 仮名（固定ハンドル） / 実名 のポリシーをグループ単位で設定
- **ホスト（管理者）**: 階層作成、ポリシー設定、モデレーション、エクスポート権限

### 📊 評価・1on1サポート

- **週次／月次ダイジェスト**を自動生成（成果、貢献、阻害要因、サポート要請）
- **評価期間スナップショット**（投稿・達成率・他者からの称賛をまとめたPDF/CSVエクスポート）
- **1on1アジェンダ自動生成**（直近の課題・未解決スレッド・依頼事項）

### 💬 既存のコミュニケーション機能を維持

- **匿名グループチャット**: 若年層・学生ユーザーの流入を促進
- **個人チャット**: 1対1の相談窓口として継続利用
- **公開トピック**: 学習コミュニティ / 社外向け場にも転用可能（ホストがON/OFF）

---

## 技術スタック

### バックエンド

| 技術 | バージョン | 用途 |
|------|-----------|------|
| ![Java](https://img.shields.io/badge/-Java%2017-007396?logo=openjdk&logoColor=white) | 17 | 実行環境 |
| ![Spring Boot](https://img.shields.io/badge/-Spring%20Boot-6DB33F?logo=spring-boot&logoColor=white) | 3.5.6 | フレームワーク |
| ![Spring Security](https://img.shields.io/badge/-Spring%20Security-6DB33F?logo=spring&logoColor=white) | - | JWT認証・RBAC |
| ![Spring Data JPA](https://img.shields.io/badge/-Spring%20Data%20JPA-6DB33F?logo=spring&logoColor=white) | - | データアクセス層 |
| ![WebSocket](https://img.shields.io/badge/-WebSocket-010101?logo=socketdotio) | STOMP | リアルタイム通信 |
| ![MySQL](https://img.shields.io/badge/-MySQL-4479A1?logo=mysql&logoColor=white) | 8.0 | データベース |

### フロントエンド

| 技術 | バージョン | 用途 |
|------|-----------|------|
| ![Next.js](https://img.shields.io/badge/-Next.js-000000?logo=next.js) | 15.5.4 | フレームワーク |
| ![TypeScript](https://img.shields.io/badge/-TypeScript-3178C6?logo=typescript&logoColor=white) | 5.0 | 型安全性 |
| ![Tailwind CSS](https://img.shields.io/badge/-Tailwind-38B2AC?logo=tailwind-css&logoColor=white) | 3.0 | スタイリング |
| ![Axios](https://img.shields.io/badge/-Axios-5A29E4?logo=axios&logoColor=white) | - | HTTP クライアント |
| ![STOMP.js](https://img.shields.io/badge/-STOMP.js-010101?logo=socketdotio) | 1.6.1 | WebSocket クライアント |

---

## クイックスタート

### 📋 必要な環境

- **Java 17** 以上
- **Node.js 18** 以上
- **MySQL 8.0**
- **Maven**

### ⚡ 3ステップで起動

```bash
# 1️⃣ バックエンドを起動 (ターミナル1)
cd backend
mvnw.cmd spring-boot:run

# 2️⃣ フロントエンドを起動 (ターミナル2)
npm install
npm run dev

# 3️⃣ ブラウザでアクセス
# 👉 http://localhost:3000
```

### 🎬 初回セットアップ

1. **データベース作成**
```sql
CREATE DATABASE chatapp CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. **環境変数設定** (`backend/.env`)
```env
JWT_SECRET=your-secret-key-here
SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/chatapp
SPRING_DATASOURCE_USERNAME=root
SPRING_DATASOURCE_PASSWORD=your-password
```

3. **起動して完了!** 🎉

---

## プロジェクト構造

```
letteral/
├── backend/                    # Spring Boot バックエンド
│   ├── src/main/java/com/chatapp/
│   │   ├── controller/        # REST API コントローラー
│   │   ├── dto/              # データ転送オブジェクト
│   │   ├── model/            # JPA エンティティ
│   │   ├── repository/       # データアクセス層
│   │   ├── security/         # 認証・認可設定
│   │   ├── service/          # ビジネスロジック
│   │   └── websocket/        # WebSocket設定
│   └── pom.xml              # Maven 設定
│
├── src/                      # Next.js フロントエンド
│   ├── app/                 # App Router ページ
│   ├── components/          # React コンポーネント
│   └── services/           # API クライアント
│
├── docs/                    # ドキュメント
│   ├── API_REFERENCE.md
│   └── DEPLOYMENT.md
│
└── package.json            # Node.js 依存関係
```

---

## 非機能要件・アーキテクチャ

### セキュリティ・匿名性設計

- **匿名ID**は組織内でのみ一意。個人識別キーは暗号化保護し、運用者でも平文参照不可
- **通報・モデレーション**: NGワード辞書、レートリミット、凍結、アーカイブ
- **機密投稿**は「管理者にも実名非公開」モードをサポート（社外相談窓口にのみ開示など）
- **データ保持ポリシー**: 評価期間後の自動アーカイブ、個人情報削除請求に対応

### システム要件

- **テナント分離**: 会社単位のデータ分離（tenant_id）
- **RBAC**: オーナー / 管理者 / モデレーター / 一般
- **監査ログ**: エクスポート・ポリシー変更・通報対応
- **可用性**: SLA 99.9%、水平スケール対応
- **国際化**: UTF-8、タイムゾーン対応（Asia/Tokyo既定）

---

## 管理者コンソール（MVP）

- **階層マネジメント**: 組織ツリー作成・編集
- **ポリシー設定**: 匿名度 / 公開範囲 / 招待・参加要件
- **ダッシュボード**: 投稿数、反応率、悩み相談件数、OKR達成率、リスク兆候
- **エクスポート**: CSV/PDF、評価期間スナップショット

---

## 成功指標（KPI）

- 週次アクティブ率（WAU/登録者）≧ 60%
- 週次の進捗投稿率 ≧ 70%（対象者）
- 未解決ブロッカーの平均解消リードタイム 30%短縮
- 四半期の1on1準備時間 50%削減
- 匿名相談の早期検知件数（人事連携済）

---

## 収益化・料金案（参考）

- **フリーミアム**: 匿名チャットと小規模グループ（最大50名）
- **プロ**: 組織ツリー・管理者コンソール・エクスポート（月額¥300/人）
- **エンタープライズ**: SSO、専用サポート、データ保持ポリシー可変（見積）

---

## ロードマップ

### Q1（MVP）
- 匿名目標・進捗投稿
- 組織ツリー、ポリシー
- 週次ダイジェスト

### Q2
- 評価期間スナップショット
- OKR連携
- 管理者ダッシュボード

### Q3
- SSO
- アーカイブ/エクスポート強化
- 通報ワークフロー
- 外部連携（Slack等）

---

## ドキュメント

| ドキュメント | 説明 |
|------------|------|
| [API_REFERENCE.md](docs/API_REFERENCE.md) | API仕様書 |
| [DEPLOYMENT.md](docs/DEPLOYMENT.md) | デプロイ手順 |
| [FEATURE_REQUIREMENTS.md](docs/FEATURE_REQUIREMENTS.md) | 機能要件 |
| [PRODUCTION_SETUP.md](backend/PRODUCTION_SETUP.md) | 本番環境設定 |

---

## 競合比較

Letteralの差別化ポイント：

| 項目 | Letteral | OKRツール | HRスイート | スタンドアップBot |
|------|----------|-----------|------------|------------------|
| 匿名性 | ⭐⭐⭐ 完全匿名〜実名選択可 | ❌ 実名中心 | ❌ 実名中心 | ❌ 実名中心 |
| 日次記録 | ⭐⭐⭐ 日次〜週次 | △ 週次〜月次 | △ 週次〜四半期 | ⭐⭐⭐ 日次 |
| 階層公開制御 | ⭐⭐⭐ 柔軟な階層設定 | △ 限定的 | ⭐⭐ 組織連動 | ❌ なし |
| 評価連動 | ⭐⭐⭐ スナップショット | ⭐⭐⭐ OKRネイティブ | ⭐⭐⭐ 人事統合 | ❌ 弱い |

詳細は企画書の「競合比較表」を参照。

---

## リスクと対策

- **荒らし・誹謗中傷**: 自動検知、段階的制裁、教育コンテンツの提示
- **匿名依存による実名コミュニケーション希薄化**: 階層・場面に応じて実名切替を促すUI
- **評価での過度な数値化**: 定性コメントの重要性を示す設計と、偏り検知
- **導入初期の投稿ネタ不足**: テンプレ・運営ガイド・社内イベントと連携

---

## 🤝 コントリビューション

プルリクエスト大歓迎です!

1. Fork this repository
2. Create feature branch (`git checkout -b feature/amazing`)
3. Commit changes (`git commit -m 'Add amazing feature'`)
4. Push to branch (`git push origin feature/amazing`)
5. Open Pull Request

---

## 📄 ライセンス

MIT License - 詳細は [LICENSE](LICENSE) を参照

---

## 🙏 謝辞

- [Spring Framework](https://spring.io/)
- [Next.js](https://nextjs.org/)
- [Tailwind CSS](https://tailwindcss.com/)
- [MySQL](https://www.mysql.com/)

---

<div align="center">

**企業の透明性と心理的安全性を両立する**

[🌟 Star this repo](https://github.com/hera-16/letteral) | [🐛 Report Bug](https://github.com/hera-16/letteral/issues) | [💡 Request Feature](https://github.com/hera-16/letteral/issues)

</div>

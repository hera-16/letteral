# バッジシステム確認チェックリスト

## 📋 バックエンド確認事項

### 1. データベーステーブル作成確認
MySQLにログインして以下のコマンドで確認：
```sql
USE chatapp;
SHOW TABLES;
-- badges と user_badges テーブルが存在することを確認

SELECT * FROM badges;
-- 15個のバッジが登録されていることを確認
```

**期待される結果：**
- `badges` テーブルに15行のデータ
  - FIRST_STEP (🌱)
  - STREAK_3 (🔥)
  - STREAK_7 (⭐)
  - TOTAL_10 (🎯)
  - TOTAL_30 (🏆)
  - TOTAL_50 (👑)
  - GRATITUDE_10 (💝)
  - KINDNESS_10 (🤝)
  - SELF_CARE_10 (🧘)
  - CREATIVITY_10 (🎨)
  - CONNECTION_10 (🌈)
  - LEVEL_3 (🌺)
  - LEVEL_5 (🌻)
  - LEVEL_7 (🌹)
  - LEVEL_10 (🏵️)

### 2. バックエンドログ確認
起動ログに以下が含まれていることを確認：
- ✅ `Hibernate: create table badges` または `Hibernate: select * from badges`
- ✅ `Hibernate: create table user_badges`
- ✅ エラーなしで起動完了

### 3. API動作確認（Postman or curl）

#### 3-1. ログイン
```bash
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "username": "testuser",
  "password": "password"
}
```
→ トークンを取得

#### 3-2. チャレンジ達成（初回）
```bash
POST http://localhost:8080/api/challenges/1/complete
Authorization: Bearer {取得したトークン}
Content-Type: application/json

{
  "note": "初めてのチャレンジ！"
}
```
→ コンソールログに「New badges earned: 1」と表示されることを確認
→ FIRST_STEP バッジが自動付与されるはず

#### 3-3. バッジ一覧取得
```bash
GET http://localhost:8080/api/challenges/badges
Authorization: Bearer {取得したトークン}
```
**期待される結果：**
```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "badge": {
        "name": "初めての一歩",
        "description": "最初のチャレンジを達成しました！すごい！",
        "badgeType": "FIRST_STEP",
        "icon": "🌱"
      },
      "earnedAt": "2025-10-14T...",
      "isNew": true
    }
  ]
}
```

#### 3-4. 新規バッジ取得
```bash
GET http://localhost:8080/api/challenges/badges/new
Authorization: Bearer {取得したトークン}
```
→ isNew=true のバッジのみ取得

#### 3-5. バッジを既読にする
```bash
POST http://localhost:8080/api/challenges/badges/mark-read
Authorization: Bearer {取得したトークン}
```
→ 全バッジの isNew が false になる

---

## 🎨 フロントエンド実装予定

### 次のステップ（未実装）
1. **Badges.tsx コンポーネント作成**
   - バッジ一覧表示（グリッドレイアウト）
   - 獲得済み/未獲得の視覚的区別
   - アイコン、名前、説明、獲得日時表示

2. **新規バッジ通知モーダル**
   - チャレンジ達成時に新規バッジをポップアップ表示
   - 紙吹雪エフェクトと連動
   - 「バッジ一覧を見る」ボタン

3. **ナビゲーション追加**
   - ヘッダーにバッジアイコン追加
   - 新規バッジがあればバッジ数表示

---

## 🐛 トラブルシューティング

### テーブルが作成されない場合
1. `application.properties` を確認：
   ```properties
   spring.jpa.hibernate.ddl-auto=update
   spring.sql.init.mode=always
   ```

2. data.sql の CREATE TABLE 文法確認
3. MySQL接続情報が正しいか確認

### バッジが付与されない場合
1. `ChallengeService.completeChallenge()` にバッジチェック呼び出しがあるか確認
2. コンソールログで「New badges earned」メッセージを確認
3. `BadgeService.checkAndAwardBadges()` のロジックをデバッグ

### API が 500 エラーを返す場合
1. バックエンドのスタックトレースを確認
2. `BadgeService` が正しく依存性注入されているか確認
3. リポジトリメソッドが正常に動作しているか確認

---

## 📈 バッジ獲得条件

| バッジタイプ | 条件 | 説明 |
|------------|------|------|
| FIRST_STEP | 総達成数 >= 1 | 初回チャレンジ達成 |
| STREAK_3 | 連続日数 >= 3 | 3日連続達成 |
| STREAK_7 | 連続日数 >= 7 | 7日連続達成 |
| TOTAL_10 | 総達成数 >= 10 | 合計10回達成 |
| TOTAL_30 | 総達成数 >= 30 | 合計30回達成 |
| TOTAL_50 | 総達成数 >= 50 | 合計50回達成 |
| GRATITUDE_10 | 感謝系 >= 10 | 感謝系チャレンジ10回 |
| KINDNESS_10 | 優しさ系 >= 10 | 優しさ系チャレンジ10回 |
| SELF_CARE_10 | セルフケア系 >= 10 | セルフケア系10回 |
| CREATIVITY_10 | 創造性系 >= 10 | 創造性系10回 |
| CONNECTION_10 | つながり系 >= 10 | つながり系10回 |
| LEVEL_3 | 花レベル >= 3 | 花レベル3到達 |
| LEVEL_5 | 花レベル >= 5 | 花レベル5到達 |
| LEVEL_7 | 花レベル >= 7 | 花レベル7到達 |
| LEVEL_10 | 花レベル >= 10 | 花レベル10到達（最高） |

---

## ✨ 実装完了後の動作イメージ

1. **ユーザーがチャレンジを達成**
   - 紙吹雪エフェクト表示 🎉
   - 励ましメッセージ表示
   - バッジ獲得条件を自動チェック

2. **新規バッジ獲得時**
   - 「おめでとう！新しいバッジを獲得しました！」モーダル表示
   - バッジアイコンと説明をアニメーション付きで表示
   - 「バッジ一覧を見る」ボタンをクリックでバッジページへ

3. **バッジページ**
   - 獲得済みバッジ：カラー表示、獲得日時付き
   - 未獲得バッジ：グレーアウト、条件のみ表示
   - プログレスバーで次のバッジまでの進捗表示

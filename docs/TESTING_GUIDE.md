# テストガイド - Letteral

## 📋 目次

- [概要](#概要)
- [バックエンドテスト](#バックエンドテスト)
- [フロントエンドテスト](#フロントエンドテスト)
- [テスト実行方法](#テスト実行方法)
- [テストカバレッジ](#テストカバレッジ)

---

## 概要

Letteralプロジェクトでは、以下のテスト戦略を採用しています：

### テストレベル

| レベル | 説明 | ツール |
|--------|------|--------|
| **ユニットテスト** | Service層のビジネスロジックをテスト | JUnit 5 + Mockito |
| **統合テスト** | Controller層のAPIエンドポイントをテスト | Spring Boot Test + MockMvc |
| **コンポーネントテスト** | Reactコンポーネントの動作をテスト | Jest + React Testing Library |

---

## バックエンドテスト

### 技術スタック

- **JUnit 5** - テストフレームワーク
- **Mockito** - モックライブラリ
- **Spring Boot Test** - 統合テスト用
- **H2 Database** - テスト用インメモリDB

### テストファイル構造

```
backend/src/test/java/com/chatapp/
├── service/                              # Serviceレイヤーのユニットテスト
│   ├── PostReplyServiceTest.java        # 返信サービスのテスト
│   ├── OrganizationServiceTest.java     # 組織サービスのテスト
│   ├── ProgressDigestServiceTest.java   # ダイジェストサービスのテスト
│   └── ...
└── controller/                           # Controllerレイヤーの統合テスト
    ├── PostReplyControllerIntegrationTest.java
    ├── ProgressPostControllerIntegrationTest.java
    ├── OrganizationControllerIntegrationTest.java
    └── ...
```

### 実装済みテスト

#### Serviceレイヤーのユニットテスト

##### 1. PostReplyServiceTest

**テストケース:**
- ✅ 返信作成（投稿者による）
- ✅ 返信作成（管理者による）
- ✅ 返信作成（権限なしユーザー - 失敗）
- ✅ 投稿IDが存在しない場合
- ✅ ユーザーIDが存在しない場合
- ✅ 返信一覧取得（投稿者による）
- ✅ 返信一覧取得（管理者による）
- ✅ 返信一覧取得（権限なしユーザー）
- ✅ 返信数カウント

**カバー内容:**
- 権限チェックロジック（投稿者 vs 管理者）
- 組織ロール別のアクセス制御（OWNER, ADMIN_SUPER, ADMIN_LEAD, ADMIN_CORE, ADMIN_ROOT）
- エラーハンドリング

##### 2. OrganizationServiceTest

**テストケース:**
- ✅ ルート組織の作成
- ✅ 子組織の作成（階層構造）
- ✅ 組織ID取得
- ✅ 組織ID取得（存在しない場合）
- ✅ テナント配下の組織一覧取得
- ✅ 子組織一覧取得
- ✅ ルート組織一覧取得
- ✅ レベル別組織取得
- ✅ パス検索
- ✅ 組織情報更新
- ✅ 組織のアクティブ状態変更
- ✅ 組織削除（子組織なし）
- ✅ 組織削除（子組織あり - 失敗）
- ✅ 深い階層の組織作成

**カバー内容:**
- 階層構造の作成と管理
- パスの自動生成・更新
- 論理削除
- バリデーション

##### 3. ProgressDigestServiceTest

**テストケース:**
- ✅ 週次ダイジェスト生成
- ✅ ダイジェスト重複作成の防止
- ✅ 月次ダイジェスト生成
- ✅ 現在週のダイジェスト生成
- ✅ 現在月のダイジェスト生成
- ✅ ユーザーダイジェスト一覧取得
- ✅ タイプ別ダイジェスト取得
- ✅ 投稿なしの場合のダイジェスト生成

#### Controllerレイヤーの統合テスト

##### 1. PostReplyControllerIntegrationTest

**テストケース:**
- ✅ 返信作成（投稿者による） - 201 Created
- ✅ 返信作成（権限なしユーザー） - 403 Forbidden
- ✅ 認証なしでの返信作成 - 401 Unauthorized
- ✅ 無効な投稿IDでの返信作成 - 404 Not Found
- ✅ 空の内容での返信作成 - 400 Bad Request
- ✅ 返信一覧取得（投稿者による）
- ✅ 返信一覧取得（権限なしユーザー - 空リスト）
- ✅ 認証なしでの返信一覧取得 - 401 Unauthorized
- ✅ 無効な投稿IDでの返信取得 - 404 Not Found
- ✅ 返信数カウント
- ✅ 返信数カウント（返信なし）
- ✅ 複数返信の順序確認

**カバー内容:**
- HTTPステータスコードの検証
- JWT認証の動作確認
- リクエスト/レスポンスのバリデーション
- エンドツーエンドのフロー

##### 2. ProgressPostControllerIntegrationTest

**テストケース:**
- ✅ 進捗投稿作成
- ✅ ユーザー別投稿一覧取得
- ✅ テナント別投稿一覧取得
- ✅ 認証なしアクセス - 401 Unauthorized
- ✅ 無効なデータでの投稿作成 - 400 Bad Request

##### 3. OrganizationControllerIntegrationTest

**テストケース:**
- ✅ 組織ツリー取得
- ✅ 組織作成
- ✅ 組織メンバー一覧取得
- ✅ メンバー追加

---

## フロントエンドテスト

### 技術スタック

- **Jest** - テストフレームワーク
- **React Testing Library** - Reactコンポーネントテスト
- **@testing-library/user-event** - ユーザーイベントシミュレーション
- **@testing-library/jest-dom** - カスタムマッチャー

### テストファイル構造

```
src/
└── components/
    ├── __tests__/                        # コンポーネントテスト
    │   └── ProgressPostForm.test.tsx    # 投稿フォームのテスト
    ├── ProgressPostForm.tsx
    ├── ProgressPostTimeline.tsx
    └── ...
```

### 実装済みテスト

#### ProgressPostForm コンポーネントテスト

**テストケース:**
- ✅ フォームの正しいレンダリング
- ✅ 内容が空の場合のバリデーション
- ✅ フォーム送信成功
- ✅ タグの追加機能
- ✅ 投稿タイプ変更（進捗 ↔ 質問）
- ✅ APIエラー時のエラーメッセージ表示
- ✅ キャンセルボタンの動作
- ✅ 組織一覧読み込みエラー処理
- ✅ 投稿成功後のフォームリセット

**カバー内容:**
- ユーザーインタラクション
- APIモック
- エラーハンドリング
- フォーム状態管理

---

## テスト実行方法

### バックエンドテスト

#### すべてのテストを実行

**Windows:**
```bash
cd backend
set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.16.8-hotspot
..\mvnw.cmd test
```

**Mac/Linux:**
```bash
cd backend
./mvnw test
```

#### 特定のテストクラスを実行

```bash
# PostReplyServiceのテストのみ
..\mvnw.cmd test -Dtest=PostReplyServiceTest

# OrganizationServiceのテストのみ
..\mvnw.cmd test -Dtest=OrganizationServiceTest

# 統合テストのみ
..\mvnw.cmd test -Dtest=*IntegrationTest
```

#### テストカバレッジレポート生成

```bash
..\mvnw.cmd test jacoco:report
```

レポートは `backend/target/site/jacoco/index.html` に生成されます。

### フロントエンドテスト

#### すべてのテストを実行

```bash
npm test
```

#### ウォッチモードで実行

```bash
npm run test:watch
```

#### カバレッジレポート生成

```bash
npm run test:coverage
```

レポートは `coverage/lcov-report/index.html` に生成されます。

---

## テストカバレッジ

### 現在のカバレッジ目標

| レイヤー | 目標カバレッジ | 現在の状況 |
|---------|--------------|-----------|
| Service層 | 80%以上 | 🟢 主要サービス実装済み |
| Controller層 | 70%以上 | 🟢 主要API実装済み |
| コンポーネント | 60%以上 | 🟡 一部実装済み |

### 優先度の高いテスト領域

#### 高優先度 ✅ 完了
- [x] PostReplyService（返信機能）
- [x] OrganizationService（組織管理）
- [x] ProgressDigestService（ダイジェスト生成）
- [x] PostReplyController（返信API）
- [x] ProgressPostController（投稿API）
- [x] ProgressPostForm（投稿フォーム）

#### 中優先度（今後の実装推奨）
- [ ] ProgressPostService（進捗投稿サービス）
- [ ] OrganizationPermissionService（組織権限サービス）
- [ ] TenantService（テナント管理）
- [ ] AuthController（認証API）
- [ ] ProgressPostTimeline（タイムラインコンポーネント）
- [ ] OrganizationTree（組織ツリーコンポーネント）

#### 低優先度
- [ ] ExportService（エクスポート機能）
- [ ] ReportService（レポート機能）
- [ ] SlackIntegrationService（Slack連携）

---

## テスト作成のベストプラクティス

### ユニットテスト（Service層）

```java
@ExtendWith(MockitoExtension.class)
class ExampleServiceTest {

    @Mock
    private ExampleRepository repository;

    @InjectMocks
    private ExampleService service;

    @Test
    void testMethodName_Condition_ExpectedResult() {
        // Arrange - テストデータとモックの設定
        when(repository.findById(1L)).thenReturn(Optional.of(entity));

        // Act - テスト対象のメソッド実行
        Result result = service.someMethod(1L);

        // Assert - 期待値の検証
        assertNotNull(result);
        assertEquals(expected, result.getValue());
        verify(repository, times(1)).findById(1L);
    }
}
```

### 統合テスト（Controller層）

```java
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class ExampleControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testEndpoint_Success() throws Exception {
        mockMvc.perform(get("/api/endpoint")
                .header("Authorization", authToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.field").value("expectedValue"));
    }
}
```

### コンポーネントテスト（React）

```typescript
describe('ComponentName', () => {
  it('should render correctly', () => {
    render(<ComponentName {...props} />);
    expect(screen.getByText('Expected Text')).toBeInTheDocument();
  });

  it('should handle user interaction', async () => {
    render(<ComponentName {...props} />);
    const button = screen.getByRole('button');
    fireEvent.click(button);

    await waitFor(() => {
      expect(mockFunction).toHaveBeenCalled();
    });
  });
});
```

---

## トラブルシューティング

### バックエンドテスト

#### H2データベースエラー
```bash
# application-test.properties を確認
spring.datasource.url=jdbc:h2:mem:testdb
spring.jpa.hibernate.ddl-auto=create-drop
```

#### テストが無限ループする場合
Lazy loadingによるN+1問題の可能性があります。テスト内で明示的に関連エンティティを初期化してください。

### フロントエンドテスト

#### モジュール解決エラー
```bash
# jest.config.js の moduleNameMapper を確認
moduleNameMapper: {
  '^@/(.*)$': '<rootDir>/src/$1',
}
```

#### 非同期処理のタイムアウト
```typescript
// waitFor のタイムアウトを調整
await waitFor(() => {
  expect(mockFunction).toHaveBeenCalled();
}, { timeout: 3000 });
```

---

## 継続的インテグレーション（CI）

### GitHub Actions設定例

```yaml
name: Tests

on: [push, pull_request]

jobs:
  backend-tests:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - name: Set up JDK 17
        uses: actions/setup-java@v2
        with:
          java-version: '17'
      - name: Run tests
        run: cd backend && ./mvnw test

  frontend-tests:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v2
      - name: Set up Node.js
        uses: actions/setup-node@v2
        with:
          node-version: '18'
      - name: Install dependencies
        run: npm install
      - name: Run tests
        run: npm test -- --coverage
```

---

## まとめ

Letteralプロジェクトでは、堅牢なテスト戦略により品質を担保しています：

- ✅ **ユニットテスト** - ビジネスロジックの正確性を保証
- ✅ **統合テスト** - APIエンドポイントの動作を検証
- ✅ **コンポーネントテスト** - ユーザーインターフェースの動作を確認

新しい機能を追加する際は、必ず対応するテストも作成してください。

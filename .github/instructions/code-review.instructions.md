---
description: 'コードレビューガイド — Java 21 + Spring Boot 3.5 プロジェクト向け'
applyTo: '**'
excludeAgent: ["coding-agent"]
---

# コードレビューガイド

> 本プロジェクト向けのコードレビュー指針。
> レビューは **日本語** で行う。
> 自動検証ツール (Spotless / Checkstyle / Error Prone / PMD) で検出可能な違反はレビューで指摘せず、ツールでカバーできない領域に集中する。

## 1. プロジェクトコンテキスト

| 項目 | 値 |
|---|---|
| 言語 | Java 21 LTS |
| フレームワーク | Spring Boot 3.5 |
| ビルドツール | Gradle (Kotlin DSL) |
| DB | PostgreSQL (本番) / H2 インメモリ (ローカル開発) |
| テスト | JUnit 5 + Spring Boot Test |
| E2E テスト | Playwright (smoke テスト + スクリーンショット) |
| コードスタイル | Google Java Style Guide (AOSP 改変: 4-space) |
| フォーマッタ | Spotless (google-java-format AOSP) |
| 静的解析 | Checkstyle (`google_checks.xml`) + Error Prone + PMD |

---

## 2. レビュー優先度

### 🔴 CRITICAL (マージブロック)

- **セキュリティ**: 脆弱性、シークレットの露出、認証・認可の欠陥
- **正確性**: ロジックエラー、データ破損リスク、競合状態
- **破壊的変更**: バージョニングなしの API 契約変更
- **データ損失**: データの損失・破損リスク

### 🟡 IMPORTANT (要議論)

- **コード品質**: SOLID 原則の重大な違反、過度な重複
- **テストカバレッジ**: クリティカルパスまたは新機能のテスト不足
- **パフォーマンス**: 明白なボトルネック (N+1 クエリ、メモリリーク)
- **アーキテクチャ**: 確立されたパターンからの逸脱

### 🟢 SUGGESTION (非ブロック改善)

- **可読性**: 命名の改善、複雑なロジックの簡素化
- **最適化**: 機能に影響しないパフォーマンス改善
- **ベストプラクティス**: 規約からの軽微な逸脱
- **ドキュメント**: コメント・ドキュメントの不足や不備

---

## 3. レビュー原則

1. **具体的に**: 正確なファイル、行番号を参照し、具体的な修正例を示す
2. **理由を説明する**: なぜ問題か、どのような影響があるかを述べる
3. **解決案を示す**: 問題点だけでなく、修正後のコード例を提示する
4. **建設的に**: コードの改善に焦点を当て、著者を批判しない
5. **良い実践を認める**: 優れたコードや巧みな解決策を積極的に評価する
6. **実用的に**: すべての提案が即座の対応を必要とするわけではない
7. **関連コメントをまとめる**: 同じトピックについて複数のコメントを避ける

---

## 4. ツールとの協調

### 原則: ツールで検出可能なものはレビューで指摘しない

`./gradlew check` で検出される違反はツールに任せ、レビューでは**ツールがカバーできない ~15% の領域**に集中する。

| カテゴリ | ツール検出 (レビュー不要) | レビュー対象 |
|---|---|---|
| フォーマット | Spotless が全自動修正 | — |
| 命名の形式 | Checkstyle (CamelCase 等) | 名前の**意味的妥当性** |
| Javadoc の形式 | Checkstyle (タグ存在・順序) | Javadoc の**内容品質** |
| バグパターン | Error Prone | 設計上の論理エラー |
| 未使用コード・複雑度 | PMD | — |

---

## 5. レビューチェックリスト

### ツールでカバーできない領域

以下はツールの原理的限界により自動検出できない。レビューで必ず確認する:

- [ ] 変数名・メソッド名は意図を正しく表現しているか
- [ ] 略語は適切で、チーム内で統一されているか
- [ ] クラスメンバは論理的に配置されているか (追加日時順になっていないか)
- [ ] Javadoc は正確かつ十分な説明になっているか
- [ ] 自明なメソッドの Javadoc が冗長になっていないか
- [ ] 非 ASCII 文字の記述方法は可読性を考慮しているか
- [ ] 複雑な演算子式には適切に括弧が付与されているか

### コード品質

- [ ] 一貫したスタイルと規約に従っているか
- [ ] 関数/メソッドは小さく、単一責務か
- [ ] コード重複がないか
- [ ] 複雑なロジックが小さく分割されているか
- [ ] エラーハンドリングが適切か
- [ ] コメントアウトされたコードや TODO (チケットなし) が残っていないか

### セキュリティ

- [ ] シークレット (パスワード、API キー、トークン) がコードやログに含まれていないか
- [ ] ユーザ入力のバリデーションとサニタイズが行われているか
- [ ] SQL インジェクション脆弱性がないか (文字列連結クエリの禁止)
- [ ] 認証・認可が適切に実装されているか
- [ ] 依存ライブラリに既知の脆弱性がないか

### テスト

- [ ] 新規コードに適切なテストカバレッジがあるか
- [ ] テスト名が目的を明確に説明しているか (`@DisplayName`)
- [ ] テストがエッジケースとエラーシナリオをカバーしているか
- [ ] テストが独立かつ決定的か (外部状態に依存しない)
- [ ] 常にパスするテストやコメントアウトされたテストがないか

### パフォーマンス

- [ ] 明白なパフォーマンス問題がないか (N+1 クエリ、メモリリーク)
- [ ] キャッシュが適切に活用されているか
- [ ] 効率的なアルゴリズムとデータ構造が選択されているか
- [ ] リソースのクリーンアップが適切か (コネクション、ストリーム)

### アーキテクチャ

- [ ] 確立されたパターンと規約に従っているか
- [ ] 関心の分離が適切か (Controller は薄く、Service にロジック)
- [ ] 依存の方向が正しいか (高レベルモジュールが低レベルに依存しない)
- [ ] パッケージ構成が機能/ドメイン単位になっているか

### ドキュメント

- [ ] パブリック API がドキュメント化されているか
- [ ] 複雑なロジックに説明コメントがあるか
- [ ] README が必要に応じて更新されているか
- [ ] 破壊的変更がドキュメント化されているか

---

## 6. AI レビューと人間レビューの使い分け

| 領域 | 人間レビュー | AI レビュー | 備考 |
|---|---|---|---|
| 命名の意味的妥当性 | ✅ | ✅ | AI は代替案の提案に強い |
| メンバの論理的順序 | ✅ | ⚠️ | 設計意図の理解は人間が優位 |
| Javadoc の内容品質 | ✅ | ✅ | AI は不足・冗長の検出に強い |
| 非 ASCII 文字の扱い | ✅ | ⚠️ | コンテキスト依存 |
| グループ化括弧 | ✅ | ⚠️ | 可読性の主観判断 |
| セキュリティ脆弱性 | ✅ | ✅ | AI はパターンマッチに強い |
| テストカバレッジ不足 | ✅ | ✅ | AI は不足箇所の列挙に強い |
| アーキテクチャ適合 | ✅ | ⚠️ | ドメイン知識と設計意図の理解が必要 |

---

## 7. コード品質基準 (Java 例)

### クリーンコード

```java
// ❌ Bad — マジックナンバーと不明確な命名
public double calc(int x, List<Item> items) {
    if (x > 100) return items.size() * 0.15;
    return items.size() * 0.10;
}

// ✅ Good — 意図を表現する命名と定数
private static final int PREMIUM_THRESHOLD = 100;
private static final double PREMIUM_DISCOUNT_RATE = 0.15;
private static final double STANDARD_DISCOUNT_RATE = 0.10;

public double calculateDiscount(int orderTotal, List<Item> items) {
    double rate = orderTotal > PREMIUM_THRESHOLD
            ? PREMIUM_DISCOUNT_RATE
            : STANDARD_DISCOUNT_RATE;
    return items.size() * rate;
}
```

### エラーハンドリング

```java
// ❌ Bad — 例外の黙殺と汎用的な catch
public User processUser(Long userId) {
    try {
        User user = userRepository.findById(userId).orElse(null);
        user.activate();
        return user;
    } catch (Exception e) {
        return null;
    }
}

// ✅ Good — 明示的なエラーハンドリングとフェイルファスト
public User processUser(Long userId) {
    Objects.requireNonNull(userId, "userId must not be null");

    User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException(
                    "ユーザが見つかりません: id=" + userId));

    user.activate();
    return user;
}
```

---

## 8. セキュリティレビュー (Spring Boot 例)

```java
// ❌ Bad — SQL インジェクション脆弱性
String query = "SELECT * FROM users WHERE email = '" + email + "'";
jdbcTemplate.query(query, rowMapper);

// ✅ Good — Spring Data JPA
Optional<User> user = userRepository.findByEmail(email);

// ✅ Good — NamedParameterJdbcTemplate
var params = new MapSqlParameterSource("email", email);
namedParameterJdbcTemplate.query(
        "SELECT * FROM users WHERE email = :email",
        params,
        rowMapper);
```

```java
// ❌ Bad — シークレットのハードコード
private static final String DB_PASSWORD = "my-secret-password";

// ✅ Good — 環境変数の利用
// application-prod.yml で ${DB_PASSWORD} を参照
```

---

## 9. テスト品質基準 (JUnit 5 例)

```java
// ❌ Bad — 曖昧なテスト名と弱いアサーション
@Test
void test1() {
    var result = calculator.calc(5, 10);
    assertTrue(result > 0);
}

// ✅ Good — 目的が明確なテスト名と具体的なアサーション
@Test
@DisplayName("注文額100円未満の場合、標準割引率10%が適用される")
void calculateDiscount_underThreshold_appliesStandardRate() {
    // Arrange
    int orderTotal = 50;
    var items = List.of(new Item("商品A", 200));

    // Act
    double discount = discountService.calculateDiscount(orderTotal, items);

    // Assert
    assertThat(discount).isEqualTo(0.10);
}

// ✅ Good — 例外テスト
@Test
@DisplayName("nullのユーザIDで検索するとNullPointerExceptionが発生する")
void findById_nullId_throwsException() {
    assertThatThrownBy(() -> userService.findById(null))
            .isInstanceOf(NullPointerException.class)
            .hasMessageContaining("userId must not be null");
}

// ✅ Good — @Nested でテストをグルーピング
@Nested
@DisplayName("ユーザ検索")
class FindById {

    @Test
    @DisplayName("存在するIDで検索すると、ユーザが返される")
    void existingId_returnsUser() { /* ... */ }

    @Test
    @DisplayName("存在しないIDで検索すると、例外が発生する")
    void nonExistingId_throwsException() { /* ... */ }
}
```

---

## 10. パフォーマンスレビュー (PostgreSQL 例)

```java
// ❌ Bad — N+1 問題
List<User> users = userRepository.findAll();
for (User user : users) {
    List<Order> orders = orderRepository.findByUserId(user.getId()); // N+1!
}

// ✅ Good — JOIN FETCH で一括取得
@Query("SELECT u FROM User u JOIN FETCH u.orders")
List<User> findAllWithOrders();

// ✅ Good — @EntityGraph で一括取得
@EntityGraph(attributePaths = {"orders"})
List<User> findAll();
```

---

## 11. コメントフォーマットテンプレート

レビューコメントは以下のフォーマットで記述する:

```markdown
**[優先度] カテゴリ: 概要**

問題の詳細説明。

**影響:**
なぜこれが問題か、どのような影響があるかの説明。

**修正案:**
[コード例 (該当する場合)]

**参考:** [関連ドキュメントや規約へのリンク]
```

### コメント例

#### Critical

```markdown
**🔴 CRITICAL — セキュリティ: SQL インジェクション脆弱性**

42行目で `email` パラメータが SQL 文字列に直接連結されており、
SQL インジェクション攻撃が可能です。

**影響:**
攻撃者が email パラメータを操作して任意の SQL を実行する可能性があります。
全データの漏洩やデータベースの破壊につながります。

**修正案:**
Spring Data JPA の derived query を使用してください:
`Optional<User> findByEmail(String email);`

**参考:** OWASP SQL Injection Prevention Cheat Sheet
```

#### Important

```markdown
**🟡 IMPORTANT — テスト: クリティカルパスのテスト不足**

`processPayment()` メソッドは金融取引を処理しますが、
返金シナリオのテストがありません。

**影響:**
返金処理は金銭の移動を伴うため、十分なテストがないと
金融エラーやデータ不整合が発生するリスクがあります。

**修正案:**
以下のテストケースを追加してください:

`@Test`
`@DisplayName("注文キャンセル時に全額返金が処理される")`
`void processPayment_cancelledOrder_processesFullRefund() { ... }`
```

#### Suggestion

```markdown
**🟢 SUGGESTION — 可読性: ネストした条件分岐の簡素化**

30-40行目のネストされた if 文はロジックの追跡が困難です。

**影響:**
シンプルなコードはメンテナンス、デバッグ、テストが容易になります。

**修正案:**
ガード句 (早期 return) を使用してネストを平坦化してください:

// ネストされた if の代わりに:
if (user == null || !user.isActive() || !user.hasPermission("write")) {
    return;
}
// メインロジック
```

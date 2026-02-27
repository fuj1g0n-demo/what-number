---
description: 'Java 21 + Spring Boot 3.5 アプリケーション開発のコーディング規約'
applyTo: '**/*.java'
---

# Java / Spring Boot 開発ガイド

> 本プロジェクト (Java 21 LTS, Spring Boot 3.5, Gradle) 向けのコーディング規約。
> Google Java Style Guide (AOSP 改変) をベースとし、自動検証ツール (Spotless / Checkstyle / Error Prone / PMD) との責務分担を前提とする。

## 1. スタイルとフォーマット

### フォーマット方針

- **google-java-format (AOSP スタイル)** を唯一のフォーマッタとして使用する。IDE 固有のフォーマッタは使用しない。
- フォーマットの自動修正と検証は **Spotless** が担う。手動でのフォーマット調整は行わない。

### インデント

| 項目 | 値 |
|---|---|
| ブロックインデント | **4-space** |
| 継続行インデント | **+8** |
| switch case | **+4** |
| 列制限 | **100 文字** |

### ファイルヘッダ

全 Java ファイルの先頭に以下のヘッダを必須とする:

```java
// fuj1g0n demo, love coding
```

Spotless の `licenseHeader()` が自動挿入するため、手動追加は不要。

### ファイル末尾・行末

- ファイル末尾に改行を必須とする。
- 行末の空白は不許可。
- いずれも Spotless が自動修正する。

### ツールとの責務分担

| 責務 | 担当ツール |
|---|---|
| インデント・空白・改行・ブレース配置・import 順序・修飾子順序 | **Spotless** (自動修正) |
| ファイルヘッダ・末尾改行・行末空白 | **Spotless** (自動修正) |
| 命名規則 (PackageName, TypeName, MethodName 等) | **Checkstyle** (違反検出) |
| Javadoc (存在・形式・タグ順序) | **Checkstyle** (違反検出) |
| プログラミング慣習 (@Override, EmptyCatch, static 参照等) | **Error Prone + Checkstyle** (違反検出) |
| 未使用変数、複雑度 | **PMD** (違反検出) |

> フォーマットに関する指摘はレビューで行わない。`./gradlew spotlessCheck` が検出する。

---

## 2. 命名規約

Google Java Style Guide Section 4 に準拠する。Checkstyle (`google_checks.xml`) が形式を自動検証する。

| 対象 | 規約 | 例 |
|---|---|---|
| クラス・インタフェース | `UpperCamelCase` (名詞) | `UserService`, `OrderRepository` |
| メソッド | `lowerCamelCase` (動詞) | `getUserById`, `calculateTotal` |
| 変数・フィールド | `lowerCamelCase` | `userName`, `orderCount` |
| 定数 (`static final`) | `UPPER_SNAKE_CASE` | `MAX_RETRY_COUNT`, `DEFAULT_PAGE_SIZE` |
| パッケージ | `lowercase` (連結語) | `com.example.userservice` |
| 型パラメータ | 単一大文字 or `T` + UpperCamelCase | `T`, `E`, `RequestT` |

### 命名の原則

- **意図を表現する名前** を付ける。形式的な正しさよりも意味的な正しさを優先する。
- 略語は避ける。チーム内で合意された略語 (例: `id`, `url`, `dto`) のみ使用する。
- Hungarian notation (型名プレフィックス) は使用しない。
- boolean 変数は `is`, `has`, `can`, `should` で始める。

> 命名の意味的妥当性はツールでは検出できない。レビューで確認する。

---

## 3. ベストプラクティス

### Records

データ保持が主目的のクラス (DTO、イミュータブルな値オブジェクト等) には **Java Record** を使用する:

```java
public record UserResponse(Long id, String name, String email) {}
```

### Pattern Matching

`instanceof` チェックには Pattern Matching を使用する:

```java
// ✅ Good
if (obj instanceof String s) {
    return s.toUpperCase();
}

// ❌ Bad
if (obj instanceof String) {
    String s = (String) obj;
    return s.toUpperCase();
}
```

### 型推論 (`var`)

ローカル変数で右辺から型が明確な場合のみ `var` を使用する:

```java
// ✅ Good — 右辺から型が明確
var users = List.of("Alice", "Bob");
var response = restTemplate.getForObject(url, UserResponse.class);

// ❌ Bad — 型が不明確
var result = process(input);
```

### 不変性

- クラスとフィールドは可能な限り `final` にする。
- 固定データには `List.of()`, `Map.of()`, `Set.of()` を使用する。
- Stream の結果は `Stream.toList()` でイミュータブルリストにする。

### Streams とラムダ

- コレクション処理には Streams API を使用する。
- メソッド参照を積極的に活用する: `stream.map(User::getName)`。
- 副作用のある `forEach` よりも `collect`, `reduce`, `toList` を優先する。

### Null ハンドリング

- `null` を返さない。戻り値が不在の可能性がある場合は `Optional<T>` を使用する。
- `null` を引数として受け取らない。必須パラメータには `Objects.requireNonNull()` を使用する。
- `@Nullable` アノテーション (Spring Framework) でフレームワーク境界の null 許容性を明示する。

---

## 4. バグパターン・コードスメルの防止

以下は Error Prone / Checkstyle / PMD が検出するパターンだが、開発時から意識することでツール指摘を未然に防ぐ。

### リソース管理

- ファイル、ソケット、ストリーム等のリソースは必ずクローズする。
- `try-with-resources` を使用する。`finalize()` は使用しない (Deprecated)。

### 等値比較

- 非プリミティブ型の等値比較には `.equals()` または `Objects.equals()` を使用する。`==` は参照比較にのみ使用する。

### パラメータ数

- メソッドのパラメータは少なく保つ。多い場合は値オブジェクトや Builder パターンを検討する。

### メソッドサイズと複雑度

- メソッドは小さく保つ。1 メソッドは 1 つの責務のみを担う。
- 深いネストは避ける (最大 3-4 レベル)。ガード句 (早期 return) で平坦化する。
- PMD の CyclomaticComplexity / CognitiveComplexity で複雑度を計測する。

### マジックナンバー

- 数値リテラルは名前付き定数に置き換える。意図を名前で表現する。

### 未使用コード

- 未使用の変数、import、メソッドは即座に削除する (PMD が検出)。
- コメントアウトされたコードは残さない。

---

## 5. Spring Boot 固有の規約

### Dependency Injection

- **コンストラクタインジェクション** を使用する。`@Autowired` フィールドインジェクションは使用しない。
- 依存フィールドは `private final` で宣言する。

```java
@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
}
```

### 設定

- 設定ファイルは `application.yml` (YAML 形式) を使用する。
- 環境ごとの差異は Spring Profile (`application-{profile}.yml`) で管理する。
- 型安全な設定バインディングには `@ConfigurationProperties` を使用する。
- シークレット (DB パスワード、API キー等) はソースコードに含めない。環境変数またはシークレット管理システムを使用する。

### パッケージ構成

- **機能/ドメイン単位** でパッケージを構成する (レイヤ単位ではない)。

```
com.example.app
├── user/
│   ├── UserController.java
│   ├── UserService.java
│   ├── UserRepository.java
│   └── UserResponse.java
├── order/
│   ├── OrderController.java
│   ├── OrderService.java
│   └── ...
└── config/
    └── SecurityConfig.java
```

### Service Layer

- ビジネスロジックは `@Service` クラスに配置する。Controller は薄く保つ。
- Service はステートレスかつテスト可能に設計する。
- Service のメソッドシグネチャにはドメイン ID や DTO を使用し、Repository エンティティを直接公開しない。

### ログ

- SLF4J を使用する。具体的な実装 (Logback, Log4j2) や `System.out.println()` を直接使用しない。
- パラメータ化ログを使用する:

```java
private static final Logger logger = LoggerFactory.getLogger(UserService.class);

logger.info("ユーザ {} のログイン処理を開始", userId);
```

### セキュリティ・入力バリデーション

- SQL インジェクション防止: Spring Data JPA または `NamedParameterJdbcTemplate` を使用する。文字列連結によるクエリ構築は禁止。
- リクエストバリデーション: JSR-380 アノテーション (`@NotNull`, `@Size`, `@Valid` 等) を使用する。

### ユーティリティクラス

- ユーティリティクラスは `final` クラスとし、`private` コンストラクタを持つ。

---

## 6. データベースアクセス

### プロファイル構成

| プロファイル | DB | 用途 |
|---|---|---|
| `default` / `local` | **H2** (インメモリ) | ローカル開発・単体テスト |
| `prod` | **PostgreSQL** | 本番環境 |

```yaml
# application.yml (共通)
spring:
  jpa:
    open-in-view: false

# application-local.yml
spring:
  datasource:
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver
  h2:
    console:
      enabled: true

# application-prod.yml
spring:
  datasource:
    url: jdbc:postgresql://${DB_HOST}:${DB_PORT}/${DB_NAME}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
```

### JPA の原則

- `open-in-view: false` を設定し、OSIV を無効化する。
- N+1 問題を防止する。`@EntityGraph` または JPQL `JOIN FETCH` を使用する。
- エンティティの直接露出を避ける。Controller 層への返却は DTO / Record を使用する。

---

## 7. テスト

### テストフレームワーク

- **JUnit 5** を使用する。JUnit 4 のアノテーションやランナーは使用しない。

### テストスライス

| アノテーション | 用途 | ロードするコンテキスト |
|---|---|---|
| `@SpringBootTest` | Integration テスト (全コンテキスト) | Full |
| `@WebMvcTest` | Controller のテスト | Web Layer のみ |
| `@DataJpaTest` | Repository のテスト (H2) | JPA Layer のみ |

### テストの原則

- テスト名は `@DisplayName` で目的を記述する。
- Arrange-Act-Assert (AAA) パターンを使用する。
- テストは独立かつ決定的にする。テスト間の依存や外部状態への依存を排除する。
- 外部依存は Mockito でモック化する。ドメインロジックはモック化しない。
- 境界値、null、空コレクション等のエッジケースをカバーする。

```java
@Test
@DisplayName("存在するユーザIDで検索すると、ユーザ情報が返される")
void findById_existingUser_returnsUser() {
    // Arrange
    var expected = new User(1L, "Alice");
    when(userRepository.findById(1L)).thenReturn(Optional.of(expected));

    // Act
    var result = userService.findById(1L);

    // Assert
    assertThat(result).isPresent();
    assertThat(result.get().getName()).isEqualTo("Alice");
}
```

---

## 8. ビルドと検証

### 日常の検証コマンド

```bash
# 全チェック一括実行 (Spotless → Error Prone → Checkstyle → PMD → テスト)
./gradlew check

# フォーマット自動修正
./gradlew spotlessApply

# フォーマット違反のみ検出
./gradlew spotlessCheck

# テストのみ実行
./gradlew test

# アプリケーション起動
./gradlew bootRun
```

### 原則

- コードの追加・変更後は `./gradlew check` で全チェックが通ることを確認してからコミットする。
- フォーマット違反は `./gradlew spotlessApply` で自動修正する。手動修正は行わない。
- CI パイプラインでも同一コマンドが実行される。ローカルと CI の結果は一致する。

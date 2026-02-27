---
description: 'Java 21 の新機能活用ガイド — 標準機能の積極採用と Preview 機能の慎重利用'
applyTo: '**/*.java'
---

# Java 21 機能活用ガイド

> Java 17 以降に導入された Java 21 の新機能について、本プロジェクトでの採用方針を定義する。
> 機能ごとに「積極採用」「慎重利用 (Preview)」「使用しない」に分類する。

---

## 1. 積極採用 (標準機能)

以下の機能は Java 21 で正式に標準化されている。新規コードでは積極的に使用する。

### 1.1 Pattern Matching for switch (JEP 441)

`instanceof` チェーンを switch 式に置き換え、コードを簡潔にする:

```java
// ❌ 旧スタイル — instanceof チェーン
public String processObject(Object obj) {
    if (obj instanceof String s) {
        return s.toUpperCase();
    } else if (obj instanceof Integer i) {
        return i.toString();
    }
    return "unknown";
}

// ✅ Java 21 — switch 式 + Pattern Matching
public String processObject(Object obj) {
    return switch (obj) {
        case String s -> s.toUpperCase();
        case Integer i -> i.toString();
        case null -> "null";
        default -> "unknown";
    };
}
```

ガード付きパターンも活用する:

```java
switch (obj) {
    case String s when s.length() > 10 -> "長い文字列: " + s;
    case String s -> "短い文字列: " + s;
    case Integer i when i > 100 -> "大きな数値: " + i;
    case Integer i -> "小さな数値: " + i;
    default -> "その他";
}
```

### 1.2 Record Patterns (JEP 440)

Record のデストラクチャリングで値を直接取得する:

```java
public record Point(int x, int y) {}
public record ColoredPoint(Point point, Color color) {}

// ✅ Record Pattern によるデストラクチャリング
public String describe(Object obj) {
    return switch (obj) {
        case Point(var x, var y) -> "座標 (" + x + ", " + y + ")";
        case ColoredPoint(Point(var x, var y), var color) ->
                "色付き座標 (" + x + ", " + y + ") " + color;
        default -> "不明な図形";
    };
}
```

### 1.3 Sequenced Collections (JEP 431)

`SequencedCollection`, `SequencedSet`, `SequencedMap` で先頭/末尾アクセスを統一的に行う:

```java
// ✅ Java 21 — 統一的な先頭/末尾アクセス
List<String> list = List.of("first", "middle", "last");
String first = list.getFirst();   // "first"
String last = list.getLast();     // "last"
List<String> reversed = list.reversed(); // ["last", "middle", "first"]

// SequencedSet も同様
SequencedSet<String> set = new LinkedHashSet<>();
set.addFirst("start");
set.addLast("end");
```

> 従来の `list.get(0)` や `list.get(list.size() - 1)` よりも `getFirst()` / `getLast()` を優先する。

### 1.4 Virtual Threads (JEP 444)

I/O バウンドな処理で Virtual Threads を活用し、スループットを向上させる:

```java
// ❌ 旧スタイル — プラットフォームスレッドのプール
ExecutorService executor = Executors.newFixedThreadPool(100);
executor.submit(() -> {
    httpClient.send(request);
});

// ✅ Java 21 — Virtual Thread (タスクごとに軽量スレッド)
try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
    executor.submit(() -> {
        httpClient.send(request);
    });
}
```

#### Spring Boot 3.5 での有効化

Spring Boot 3.5 では設定一つで Tomcat、Spring MVC、`@Async` に Virtual Threads が適用される:

```yaml
# application.yml
spring:
  threads:
    virtual:
      enabled: true
```

> この設定により、リクエスト処理スレッドが Virtual Threads に切り替わる。既存コードの変更は不要。

#### 注意点

- Virtual Threads は **I/O バウンドな処理** に最も効果的。CPU バウンドなタスクでは恩恵が少ない。
- `synchronized` ブロック内でのブロッキング I/O は Virtual Thread をピン留め (pin) する。`ReentrantLock` への置き換えを検討する。
- スレッドローカル (`ThreadLocal`) は Virtual Threads でも動作するが、Virtual Thread ごとにコピーが作られるためメモリ効率に注意する。

---

## 2. 活用を推奨する API 改善

### 2.1 UTF-8 デフォルト化 (JEP 400 — Java 18 で標準化)

UTF-8 が全プラットフォームでデフォルト文字セットになった。明示的な `StandardCharsets.UTF_8` 指定は冗長なため省略可能:

```java
// ❌ 冗長 — Java 18 以降は不要
Files.readString(path, StandardCharsets.UTF_8);
Files.writeString(path, content, StandardCharsets.UTF_8);

// ✅ Java 18+ — デフォルトで UTF-8
Files.readString(path);
Files.writeString(path, content);
```

### 2.2 Finalization の廃止 (JEP 421)

`finalize()` メソッドは Deprecated。代替として `Cleaner` API または `try-with-resources` を使用する:

```java
// ❌ Deprecated — 使用しない
@Override
protected void finalize() throws Throwable {
    cleanup();
}

// ✅ 代替 — try-with-resources
try (var resource = new MyResource()) {
    resource.process();
}
// AutoCloseable.close() で自動クリーンアップ
```

> Error Prone が `finalize()` の使用を検出する。

---

## 3. 慎重利用 (Preview 機能)

以下の機能は Java 21 時点で **Preview** であり、`--enable-preview` フラグが必要。チーム合意なしに使用しない。

### 3.1 Unnamed Patterns and Variables (JEP 443)

> Java 22 で標準化 (JEP 456)。Java 21 では Preview。

使用しない値を `_` で明示できる:

```java
// Java 22+ で標準化される記法
switch (ball) {
    case RedBall(_) -> "赤いボール";
    case BlueBall(var size) -> "青いボール サイズ " + size;
}

try {
    riskyOperation();
} catch (IOException | SQLException _) {
    handleError();
}
```

> 本プロジェクトでは Java 21 を使用するため、この機能は **Preview** 扱い。使用する場合は `build.gradle.kts` に `--enable-preview` フラグを追加し、チームで合意すること。

### 3.2 Scoped Values (JEP 446)

> Java 21 では Preview。Java 25 で標準化予定。

`ThreadLocal` の代替として、Virtual Threads との親和性が高い:

```java
private static final ScopedValue<String> USER_ID = ScopedValue.newInstance();

ScopedValue.where(USER_ID, "user123")
    .run(() -> {
        processRequest(); // USER_ID.get() でアクセス可能
    });
```

> Spring Boot 3.5 は `ThreadLocal` ベースの設計が健在であり、`RequestContextHolder` 等は既に機能している。積極的な移行は不要。Virtual Threads 環境でパフォーマンスが問題になった場合に検討する。

### 3.3 Structured Concurrency (JEP 453)

> Java 21 では Preview。

```java
try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
    Subtask<String> user = scope.fork(() -> fetchUser(userId));
    Subtask<String> order = scope.fork(() -> fetchOrder(orderId));

    scope.join();
    scope.throwIfFailed();

    return processResults(user.get(), order.get());
}
```

> Spring Boot 3.5 では `@Async` + Virtual Threads の組み合わせが推奨パス。Structured Concurrency は Preview であり、Spring Framework との統合も未成熟なため、当面は使用しない。

---

## 4. 使用しない機能

### 4.1 String Templates (JEP 430)

**JEP 465 により Java 23 で撤回 (withdrawn) された。使用しない。**

```java
// ❌ 使用しない — JEP 465 により撤回済み
String message = STR."Hello, \{name}! You have \{count} messages.";
```

代替として従来の文字列フォーマットを使用する:

```java
// ✅ 従来のフォーマット
String message = "Hello, %s! You have %d messages.".formatted(name, count);
// または
String message = String.format("Hello, %s! You have %d messages.", name, count);
```

---

## 5. ビルド設定

### Preview 機能を使用する場合 (チーム合意を前提)

```kotlin
// build.gradle.kts
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

tasks.withType<JavaCompile> {
    options.compilerArgs.add("--enable-preview")
}

tasks.withType<Test> {
    jvmArgs("--enable-preview")
}

tasks.withType<JavaExec> {
    jvmArgs("--enable-preview")
}
```

> Preview フラグは **原則追加しない**。追加する場合は、使用する Preview 機能を明記したコメントを `build.gradle.kts` に記載する。

### Error Prone との互換性

- Error Prone は Preview 機能の構文で誤検出 (false positive) を出す場合がある。その場合は `@SuppressWarnings("ErrorProneRuleKey")` で抑制し、抑制理由をコメントに記載する。

### GC チューニング (本番環境向け)

ローカル開発では不要。本番環境で検討する:

```kotlin
// build.gradle.kts (bootRun 用の例)
tasks.named<org.springframework.boot.gradle.tasks.run.BootRun>("bootRun") {
    jvmArgs(
        "-XX:+UseZGC",
        "-XX:+ZGenerational"  // Generational ZGC (JEP 439)
    )
}
```

---

## 6. コードレビューチェックリスト (Java 21 観点)

Java 21 機能の活用についてレビュー時に確認する:

- [ ] `instanceof` チェーンが switch 式に置き換え可能ではないか
- [ ] データ保持クラスに Record Pattern が活用できないか
- [ ] `list.get(0)` / `list.get(list.size() - 1)` が `getFirst()` / `getLast()` に置き換え可能ではないか
- [ ] I/O バウンドな処理で Virtual Threads が活用できないか
- [ ] 明示的な `StandardCharsets.UTF_8` 指定が不要ではないか
- [ ] `finalize()` メソッドが残っていないか
- [ ] 撤回済みの String Templates (`STR."..."`) を使用していないか
- [ ] Preview 機能を使用する場合、チーム合意と `--enable-preview` フラグの追加がされているか

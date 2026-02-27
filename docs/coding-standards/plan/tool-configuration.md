# ツール設定計画

> 各ツールの設定方針を整理する。具体的な設定値は各ツールのドキュメントを参照。

## 1. Spotless (google-java-format)

### Java ファイル

| 設定項目 | 値 | 備考 |
|---|---|---|
| google-java-format バージョン | 最新安定版 | リリースノートを確認して更新 |
| スタイル | **AOSP** | 4-space インデント対応 |
| ライセンスヘッダ | `// fuj1g0n demo, love coding` | `licenseHeader()` で設定 |
| 実行フェーズ | validate | コンパイル前に実行 |

### 非 Java ファイル (format ブロック)

| 設定項目 | 対象 |
|---|---|
| `trimTrailingWhitespace()` | `*.md`, `*.xml`, `*.yml`, `*.properties` |
| `endWithNewline()` | 同上 |

> Java ファイルの末尾改行・行末空白は google-java-format が暗黙的に処理するため、追加設定不要。

## 2. Checkstyle

| 設定項目 | 値 |
|---|---|
| 設定ファイル | `google_checks.xml` をベースにカスタマイズ |
| 実行フェーズ | verify |
| 失敗条件 | 違反があればビルド失敗 |

### `google_checks.xml` のカスタマイズ箇所

| モジュール | パラメータ | 変更内容 |
|---|---|---|
| `Indentation` | `basicOffset` | 2 → **4** |
| `Indentation` | `caseIndent` | 2 → **4** |
| `Indentation` | `throwsIndent` | 4 → **8** |
| `Indentation` | `lineWrappingIndentation` | 4 → **8** |
| `Indentation` | `arrayInitIndent` | 4 → **8** |
| `LineLength` | `max` | 100 据え置き (実測後に 120 を検討) |

フォーマット系モジュール (WhitespaceAround 等) は Spotless に委譲し、無効化を検討する。

## 3. Error Prone

| 設定項目 | 値 |
|---|---|
| 統合方式 | maven-compiler-plugin / net.ltgt.errorprone |
| 実行フェーズ | compile (javac プラグインとして動作) |
| フォーマットへの影響 | **なし** |

## 4. PMD (任意)

| 設定項目 | 値 |
|---|---|
| ルールセット | `quickstart.xml` またはカスタムルールセット |
| 実行フェーズ | verify |
| フォーマットへの影響 | **なし** |

## 5. `.editorconfig`

| セクション | プロパティ | 値 | 対応改変 |
|---|---|---|---|
| `[*]` | `charset` | `utf-8` | — |
| `[*]` | `end_of_line` | `lf` | — |
| `[*]` | `insert_final_newline` | `true` | 改変 #2 |
| `[*]` | `trim_trailing_whitespace` | `true` | 改変 #3 |
| `[*.java]` | `indent_style` | `space` | — |
| `[*.java]` | `indent_size` | `4` | 改変 #1 |
| `[*.{xml,yaml,yml}]` | `indent_style` | `space` | — |
| `[*.{xml,yaml,yml}]` | `indent_size` | `2` | — |

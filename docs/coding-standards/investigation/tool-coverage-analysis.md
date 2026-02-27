# ツールカバレッジ分析

> Google Java Style Guide の各領域に対するチェックツールの対応状況を整理する。

## 1. 推奨ツール構成

| ツール | 種別 | 役割 | 推奨度 |
|---|---|---|---|
| **google-java-format** (Spotless 経由) | Formatter | フォーマット自動修正 + CI 検証 | **必須** |
| **Checkstyle** (`google_checks.xml`) | Linter | 命名・Javadoc・構造の違反検出 | **必須** |
| **Error Prone** | コンパイラ拡張 | コンパイル時バグ・慣習チェック | **必須** |
| **PMD** | 静的解析 | Checkstyle 補完 | 推奨 |

## 2. セクション別自動化率

| セクション | 自動化率 | 主担当ツール |
|---|---|---|
| 1. Source file basics | ~60% | Checkstyle |
| 2. Source file structure | ~85% | Checkstyle + google-java-format |
| 3. Formatting | ~95% | google-java-format (主) + Checkstyle (補) |
| 4. Naming | ~80% | Checkstyle |
| 5. Programming Practices | ~90% | Error Prone (主) + Checkstyle (補) |
| 6. Javadoc | ~70% | Checkstyle |
| **総合** | **~85%** | — |

## 3. 自動化困難な領域 (~15%)

以下の領域はツールの原理的限界により自動化できず、人間/AI レビューで補完する。

| 領域 | Style Guide 参照 | 困難な理由 |
|---|---|---|
| 命名の意味的妥当性 | Section 4 全般 | 形式 (CamelCase 等) は検出可能だが、名前が意図を正しく表現しているかはドメイン知識が必要 |
| メンバの論理的順序 | Section 2.4.1 | 「論理的順序」は設計意図に依存。オーバーロードの連続配置のみ Checkstyle で検出可能 |
| Javadoc の内容品質 | Section 6.2, 6.4 | タグの存在・形式は検出可能だが、記述の正確性・十分性は自然言語の品質評価を伴う |
| 非 ASCII 文字の扱い | Section 1.3.3 | `μ` vs `\u03bc` の選択はコンテキスト依存 |
| グループ化括弧 | Section 3.7 | 優先順位の明確さは可読性の主観判断に依存 |

## 4. Spotless の位置づけ

Spotless は **フォーマット統一のレイヤーとして最適** だが、全ツールの統一レイヤーにはならない。

| 観点 | 説明 |
|---|---|
| Spotless がカバーする範囲 | google-java-format 適用、ライセンスヘッダ挿入、import 整理、末尾空白・改行の正規化 |
| Spotless がカバーしない範囲 | Checkstyle (命名・Javadoc)、Error Prone (バグ検出)、PMD (静的解析) |
| 統一レイヤーは何か | **ビルドツール (Maven / Gradle) のライフサイクル**。`mvn verify` / `gradle check` で全チェック一括実行 |

各ツールはビルドライフサイクル上の異なるフェーズで動作する:

| フェーズ | ツール | 性質 |
|---|---|---|
| validate | Spotless | テキスト変換 (AST 不要) |
| compile | Error Prone | javac プラグイン (AST + 型情報が必要) |
| verify | Checkstyle / PMD | AST ベースの静的解析 |

## 5. ツール間の責務分離

Spotless と Checkstyle を併用する場合、フォーマット系ルールの重複を排除する:

| 責務 | 担当ツール |
|---|---|
| インデント・空白・改行・ブレース配置・import 順序・修飾子順序 | **Spotless** (自動修正) |
| ファイルヘッダ・末尾改行・行末空白 | **Spotless** |
| 命名規則 (PackageName, TypeName, MethodName 等) | **Checkstyle** (検出) |
| Javadoc (存在・形式・タグ順序) | **Checkstyle** (検出) |
| プログラミング慣習 (@Override, EmptyCatch, static 参照) | **Error Prone + Checkstyle** (検出) |
| ファイル構造 (OneTopLevelClass, OuterTypeFilename) | **Checkstyle** (検出) |

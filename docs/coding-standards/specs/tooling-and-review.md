# ツール・レビュー要件

> コーディング規約を自動検証するためのツール要件と、ツールでカバーできない領域のレビュー要件を定義する。

## 1. 必須ツール

| ツール | 役割 | 必須/推奨 |
|---|---|---|
| **Spotless** (google-java-format) | フォーマット自動修正 + ヘッダ挿入 + CI 検証 | **必須** |
| **Checkstyle** (`google_checks.xml`) | 命名・Javadoc・構造の違反検出 | **必須** |
| **Error Prone** | コンパイル時バグ・慣習チェック | **必須** |
| **PMD** | Checkstyle 補完 (未使用変数、複雑度等) | 推奨 |

## 2. Spotless の責務

| 機能 | 対応する改変 |
|---|---|
| google-java-format AOSP 適用 | 改変 #1 (4-space) + フォーマット全般 |
| `endWithNewline()` | 改変 #2 (末尾改行) |
| `trimTrailingWhitespace()` | 改変 #3 (行末空白) |
| `licenseHeader()` | 改変 #4 (ファイルヘッダ) |

Checkstyle のフォーマット系モジュール (Indentation, WhitespaceAround 等) は Spotless に委譲し、重複を排除する。

## 3. ビルドライフサイクル要件

| フェーズ | ツール | 検証内容 |
|---|---|---|
| validate | Spotless `check` | フォーマット + ヘッダ + 末尾改行 + 行末空白 |
| compile | Error Prone | @Override 漏れ、Finalizer 使用、静的参照 |
| verify | Checkstyle `check` | 命名規則、Javadoc 形式、ファイル構造 |
| verify | PMD `check` (任意) | 未使用変数、複雑度 |

`mvn verify` / `gradle check` の一コマンドで全チェックが実行されること。

## 4. IDE 整合性要件

IDE 間のフォーマット差異を吸収する多層防御:

| レイヤー | 役割 | 必須/推奨 |
|---|---|---|
| **Layer 1**: `.editorconfig` | 基本設定の統一 (文字コード、インデント、改行) | **必須** |
| **Layer 2**: IDE フォーマットアクション | リアルタイム整形 (google-java-format プラグイン or Spotless 呼び出し) | 推奨 |
| **Layer 3**: Pre-commit hook | コミット前に `spotless:apply` 自動実行 | 推奨 |
| **Layer 4**: CI/CD gate | `spotless:check` でビルド失敗 | **必須** |

## 5. レビューチェックリスト

ツールで検出できない ~15% の領域に対するレビュー観点:

- [ ] 変数名・メソッド名は意図を正しく表現しているか
- [ ] 略語は適切で、チーム内で統一されているか
- [ ] クラスメンバは論理的に配置されているか (追加日時順になっていないか)
- [ ] Javadoc は正確かつ十分な説明になっているか
- [ ] 自明なメソッドの Javadoc が冗長になっていないか
- [ ] 非 ASCII 文字の記述方法は可読性を考慮しているか
- [ ] 複雑な演算子式には適切に括弧が付与されているか

### レビュー担当の使い分け

| 領域 | 人間レビュー | AI レビュー | 備考 |
|---|---|---|---|
| 命名の意味的妥当性 | ✅ | ✅ | AI は代替案の提案に強い |
| メンバの論理的順序 | ✅ | ⚠️ | 設計意図の理解は人間が優位 |
| Javadoc の内容品質 | ✅ | ✅ | AI は不足・冗長の検出に強い |
| 非 ASCII 文字の扱い | ✅ | ⚠️ | コンテキスト依存 |
| グループ化括弧 | ✅ | ⚠️ | 可読性の主観判断 |

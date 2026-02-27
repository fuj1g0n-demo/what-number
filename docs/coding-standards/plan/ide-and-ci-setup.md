# IDE 整合性・CI/CD 計画

> IDE 間のフォーマット差異を吸収する多層防御の設定計画と、CI/CD パイプラインの構成を整理する。

## 1. 多層防御アーキテクチャ

| レイヤー | 目的 | 実行タイミング |
|---|---|---|
| **Layer 1**: `.editorconfig` | 基本設定の統一 (文字コード、インデント幅、改行コード) | IDE ファイル保存時 |
| **Layer 2**: IDE フォーマットアクション | google-java-format によるリアルタイム整形 | 開発者の任意操作 / 保存時 |
| **Layer 3**: Pre-commit hook | `spotless:apply` の自動実行 | `git commit` 時 |
| **Layer 4**: CI/CD gate | `spotless:check` による最終検証 | push / PR 時 |

## 2. Layer 2: IDE フォーマットアクション

### 方式選定

| 条件 | 推奨方式 |
|---|---|
| Spotless に google-java-format 以外の設定がある (ヘッダ等) | **方式 B (Spotless 呼び出し)** |
| google-java-format のみで開発体験を優先 | 方式 A (プラグイン) + Layer 3/4 で補完 |

> 本プロジェクトでは `licenseHeader()` を使用するため、方式 B が有効。

### 方式 A: google-java-format プラグイン

| IDE | プラグイン | 備考 |
|---|---|---|
| IntelliJ IDEA | google-java-format (JetBrains Marketplace) | Settings で AOSP スタイル選択。Actions on Save で Reformat 有効化推奨 |
| Eclipse | google-java-format Eclipse plugin (dropins/ 配置) | Formatter を google-java-format に切替。Save Actions で Format 有効化推奨 |
| VS Code | Eclipse JDT LS 経由 (近似) | google-java-format と 100% 同一ではない。Layer 3/4 で差異を吸収 |

### 方式 B: IDE から Spotless を呼び出し

各 IDE から Spotless の単一ファイルフォーマット機能を利用する:

- **Gradle**: `./gradlew spotlessApply -PspotlessIdeHook=<ファイルパス>`
- **Maven**: `mvn spotless:apply -DspotlessFiles='.*<ファイル名>\.java'`

| IDE | 推奨方法 |
|---|---|
| IntelliJ IDEA | External Tool + キーバインド、または File Watcher (保存時自動) |
| Eclipse | External Tool Builder + キーバインド、または Builder 登録 (保存時自動) |
| VS Code | Run on Save 拡張 (emeraldwalk.RunOnSave 等)、または Task + キーバインド |

**パフォーマンス対策**: Gradle Daemon (デフォルト有効) で 1-3 秒。Maven は mvnd の使用を推奨。`-q` オプションでログ出力を抑制。

## 3. Layer 3: Pre-commit hook

`spotless:apply` をコミット前に自動実行し、IDE 設定漏れや近似差異を吸収する。

| 方式 | 概要 |
|---|---|
| `.git/hooks/pre-commit` | シェルスクリプトで `spotless:apply` → `git add -u` |
| pre-commit framework | `.pre-commit-config.yaml` でローカルフックとして登録 |

## 4. Layer 4: CI/CD パイプライン

全レイヤーの最終チェックとして CI で全ツールを実行する。

| 順序 | ステップ | 目的 |
|---|---|---|
| 1 | `spotless:check` | フォーマット + ヘッダ + 改行 + 空白の検証 |
| 2 | `compile` (Error Prone) | コンパイル時慣習チェック |
| 3 | `checkstyle:check` | 命名・Javadoc・構造の検証 |
| 4 | `pmd:check` (任意) | 補助チェック |

> `mvn verify` / `gradle check` で上記すべてが一括実行される。

### パイプライン上の位置づけ

| チェック主体 | カバー範囲 | 自動化 |
|---|---|---|
| ツール (Layer 1-4) | ~85% | 完全自動 |
| AI Code Review | ~15% の意味的妥当性 | 半自動 |
| 人間 Code Review | 最終的な設計・品質判断 | 手動 |

## 5. リポジトリ管理対象

| ファイル | 管理対象 |
|---|---|
| `.editorconfig` | ✅ |
| `pom.xml` / `build.gradle` (Spotless 設定) | ✅ |
| カスタム `google_checks.xml` | ✅ |
| `.vscode/settings.json` | ✅ |
| `.idea/google-java-format.xml` | ✅ 推奨 |
| `.pre-commit-config.yaml` or hook script | ✅ |

## 6. 初回導入時の対応

| 課題 | 対策 |
|---|---|
| 既存コードの大量リフォーマット | PR が少ないタイミングで一括実施 |
| git blame の履歴途切れ | `git blame --ignore-rev` で対処 |
| 既存 PR のコンフリクト | リフォーマットコミット前に通知・調整 |

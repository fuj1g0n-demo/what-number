# Agent 設計（Skill 中心）

## 概要

本ドキュメントは、GitHub Copilot 向けのリポジトリ設定の設計を定義する。
手順的知識を **Agent Skill** として部品化し、ローカル Skill は **Agent Plugin** 形式、
外部 Skill は **APM（Agent Package Manager）** で管理する。

## 設計原則

1. **常時ロードと on-demand の分離**: エージェントが常に必要とする事実（プロジェクト概要、統制ルール）のみを常時ロードされる instructions に置き、手順的知識はトリガー時にのみロードされる Skill に置く
2. **ドキュメント正本主義**: `docs/specs/`・`docs/design/`・README が知識の正本であり、Skill は「いつ・どのドキュメントを読むか」の導線と実行手順のみを持つ。内容の複製によるドリフトを禁止する
3. **標準形式**: ローカル Skill は Agent Plugins 1.0 と Agent Skills 標準（SKILL.md 形式）に準拠する
4. **限定した配備先**: APM の target は GitHub Copilot のみとし、外部依存の解決結果はロックファイルで固定する

## 構成要素と配置

| 構成要素 | 配置場所 | 役割 | 管理方法 |
|---|---|---|---|
| ローカル Skill（ソース） | `plugins/what-number/skills/` 配下 | 本リポジトリ固有の手順的知識 | Agent Plugin の一部として手動編集 |
| ローカル Agent Plugin | `plugins/what-number/` | ローカル Skill を束ねる Agent Plugins 1.0 パッケージ | repository marketplace で配布 |
| Plugin marketplace | `.github/plugin/marketplace.json` | ローカル Plugin の catalog | GitHub Copilot CLI / VS Code / cloud agent が利用 |
| Copilot Plugin defaults | `.github/copilot/settings.json` | marketplace と既定 Plugin を共有 | 手動編集 |
| 外部 Skill | Azure skills の各 `skills/<name>/` | Azure 操作等の汎用 Skill | APM の個別 subdirectory 依存。`main` を参照し lockfile で commit 固定 |
| 配備済み外部 Skill | `.agents/skills/` 配下 | GitHub Copilot が発見・ロードする Skill | APM が生成（手動編集禁止） |
| 常時ロード instructions | `.github/copilot-instructions.md` | プロジェクト概要・統制ルール・Skill 一覧 | 手動編集 |
| APM マニフェスト | リポジトリ直下の `apm.yml` | 依存宣言・GitHub Copilot target | 手動編集 |
| APM ロックファイル | リポジトリ直下の `apm.lock.yaml` | 解決済みコミットとファイルハッシュの記録 | APM が生成 |

## Skill の分類

Skill は以下の 2 側面で分類する。

### 開発プロセス系

開発の進め方に関する手順的知識。

| Skill | 責務 |
|---|---|
| issue-workflow | Issue 4 カテゴリ管理、sub-issue 作成手順、ラベル統制、クローズ・reopen 規則 |
| user-story-quality-check | ユーザーストーリーの品質チェックフロー（EARS 検査から最終判定まで） |
| incremental-planning | インクリメンタルな実装と統合の分割基準 |
| pr-review | レビュー要件の適用手順、AI と人間の役割分担、Approve・マージ条件 |
| branching-release | GitHub Flow のブランチ運用、PR 規則、リリース実行手順 |
| manual-test-gherkin | 手動テストの要否判断と Gherkin 形式のテストケース策定 |
| task-file-workflow | タスクファイル（docs/tasks/）によるローカルタスク管理方式 |

### 言語・アーキテクチャ系

技術スタック・実装規約に関する知識。

| Skill | 責務 |
|---|---|
| java-spring-conventions | Java 21 言語機能の活用、Spring Boot 規約、レイヤー分離、セキュリティ |
| testing-strategy | 3 レベルテスト戦略、ソースセット配置、実行コマンド |
| thymeleaf-frontend | Natural Templating、XSS 対策、Bootstrap 5、JS 最小化 |
| gradle-build | Kotlin DSL、BOM 委任、マルチプロジェクト構成、バージョン規律 |
| azure-cicd-ops | CI/CD ワークフローの全体像、プレビュー環境、運用上の注意 |
| screenshot | Playwright CLI によるスクリーンショット撮影手順 |

## カスタムエージェントを設けない判断

エージェント製品固有のカスタムエージェント機構（例: GitHub Copilot の `.github/agents/`）は使用しない。

- 手順的知識はすべて Skill に部品化されており、エージェント定義に残る内容（ペルソナ・使用 Skill の宣言）が薄く、独立した構成要素として維持する価値が低い
- カスタムエージェント機構は特定製品専用であり、クロスエージェント互換の設計原則に反する
- タスク管理媒体の差分（GitHub issue またはタスクファイル）は `issue-workflow` と `task-file-workflow` の Skill の使い分けで表現する

### 「確認済み」の統制方式

ユーザーストーリーの「確認済み」判定は、特定エージェントの身元ではなく **プロセスの完遂と証跡** を根拠とする:

- 「確認済み」ラベル（タスクファイル方式では `verified` ステータス）は、`user-story-quality-check` Skill のチェックフローをすべて完遂し、構造化コメントを証跡として記録した場合にのみ付与できる
- 証跡（構造化コメント）が存在しない「確認済み」は無効とみなし、ユーザーはラベルを引きはがすことができる

## Agent Plugin / APM による管理ルール

- ローカル Skill の追加・変更は `plugins/what-number/skills/` 配下で行い、Agent Plugin の形式を維持する
- ローカル Plugin を変更した場合は `plugin.json` と marketplace entry の version を一致させる
- `.github/copilot/settings.json` はローカル marketplace と Plugin を GitHub Copilot の既定として有効にする
- 外部 Azure Skill は APM で個別に管理し、`main` の最新 commit を `apm.lock.yaml` に固定する。更新時は `apm update` を実行する
- Azure repository root の Agent Plugin は MCP 等も含むため依存にせず、必要な Skill subdirectory のみを列挙する。新しい upstream Skill を追加する場合は `apm.yml` に依存を追加する
- APM の target は `apm.yml` で `copilot` のみに限定する
- `.agents/skills/` 配下は APM が配備する外部 Skill の出力先であり、直接編集しない

## Skill 記述の規約

- SKILL.md のフロントマターの description には、機能の要約に加えて **WHEN（起動すべき状況・トリガー語句）** を日本語・英語の双方で明記する
- 本文は簡潔に保ち、詳細は正本ドキュメントへの参照で示す
- 他の Skill と重複する知識は記述せず、Skill 名で相互参照する

## 発見・配備先

| エージェント | 発見パス | 備考 |
|---|---|---|
| GitHub Copilot CLI / cloud agent | `what-number` Plugin と `.agents/skills/` | Repository defaults が Plugin を有効化 |
| VS Code | `what-number` Plugin と `.agents/skills/` | repository marketplace から Plugin をインストール |

# Agent 設計（Skill 中心）

## 概要

本ドキュメントは、AI コーディングエージェント（GitHub Copilot、Claude Code、OpenAI Codex 等）向けの
リポジトリ設定の設計を定義する。手順的知識を **Agent Skill** として部品化し、
**APM（Agent Package Manager）** で一元管理する Skill 中心の設計を採用する。

## 設計原則

1. **常時ロードと on-demand の分離**: エージェントが常に必要とする事実（プロジェクト概要、統制ルール）のみを常時ロードされる instructions に置き、手順的知識はトリガー時にのみロードされる Skill に置く
2. **ドキュメント正本主義**: `docs/specs/`・`docs/design/`・README が知識の正本であり、Skill は「いつ・どのドキュメントを読むか」の導線と実行手順のみを持つ。内容の複製によるドリフトを禁止する
3. **クロスエージェント互換**: Skill は Agent Skills 標準（SKILL.md 形式）に準拠し、複数のエージェント製品から利用可能なパスに配備する
4. **APM による一元管理**: ローカル Skill と外部 Skill パッケージの双方を APM のマニフェストとロックファイルで管理し、配備物の完全性を検証可能にする

## 構成要素と配置

| 構成要素 | 配置場所 | 役割 | 管理方法 |
|---|---|---|---|
| ローカル Skill（ソース） | `.apm/skills/` 配下 | 本リポジトリ固有の手順的知識の正本 | 手動編集し、APM で配備 |
| 配備済み Skill | `.agents/skills/` 配下 | 各エージェントが発見・ロードする実体 | APM が生成（手動編集禁止） |
| 外部 Skill パッケージ | `.agents/skills/` 配下 | Azure 操作等の汎用 Skill（microsoft/azure-skills） | APM 依存としてコミット固定 |
| 常時ロード instructions | `.github/copilot-instructions.md` | プロジェクト概要・統制ルール・Skill 一覧 | 手動編集 |
| カスタムエージェント | `.github/agents/` 配下 | ペルソナ・権限・使用 Skill の宣言のみを持つ薄いプロファイル | 手動編集 |
| APM マニフェスト | リポジトリ直下の apm.yml | 依存宣言・配備ターゲット定義 | 手動編集 |
| APM ロックファイル | リポジトリ直下の apm.lock.yaml | 解決済みコミットとファイルハッシュの記録 | APM が生成 |

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

## カスタムエージェントの設計

カスタムエージェント（`.github/agents/` 配下）は以下のみを定義する薄いプロファイルとする:

- ペルソナ（役割・専門性）
- 権限（例: 「確認済み」ラベル付与の唯一の権限者）
- ミッションと、参照すべき Skill の一覧
- タスク管理媒体（GitHub issue またはタスクファイル）等のエージェント固有差分

チェックフロー・操作手順等の手順的知識はエージェント定義に直接記述せず、Skill を参照する。
これにより、同一の品質チェック手順を issue 駆動・タスクファイル駆動の両エージェントで共有する。

## APM による管理ルール

- ローカル Skill の追加・変更は `.apm/skills/` 配下でのみ行い、変更後に APM の install を実行して `.agents/skills/` へ再配備する
- `.agents/skills/` 配下の直接編集を禁止する（APM の audit で検出される）
- 外部依存（microsoft/azure-skills 等）はコミット SHA で固定し、更新は明示的な操作でのみ行う
- 配備ターゲットは apm.yml の targets で宣言する（copilot および agent-skills）

## Skill 記述の規約

- SKILL.md のフロントマターの description には、機能の要約に加えて **WHEN（起動すべき状況・トリガー語句）** を日本語・英語の双方で明記する
- 本文は簡潔に保ち、詳細は正本ドキュメントへの参照で示す
- 他の Skill と重複する知識は記述せず、Skill 名で相互参照する

## エージェント別の発見パス

| エージェント | 発見パス | 備考 |
|---|---|---|
| GitHub Copilot（CLI / Coding Agent / IDE） | `.agents/skills/` および `.github/skills/` | instructions・カスタムエージェントも読む |
| OpenAI Codex | `.agents/skills/` | instructions 機構は持たない |
| Claude Code | `.claude/skills/` および `.agents/skills/` | 必要に応じて claude ターゲットを追加可能 |
| Cursor / Gemini CLI / その他 | `.agents/skills/` | Agent Skills 標準準拠 |

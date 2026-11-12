# Copilot Instructions

## プロジェクト概要

本リポジトリは、金融・公共のお客様向けデモ用「この数字、なんの数字？」クイズアプリケーションです。

- 技術スタック: Java 21 / Spring Boot 3.4 / Thymeleaf（SSR） / Bootstrap 5（CDN） / Gradle（Kotlin DSL）
- 詳細な技術選定と根拠: `docs/design/tech-stack.md`
- 開発方針（ウォーターフォール制約下でのアジャイル実践導入）: `docs/design/hybrid-development-policy.md`

## Skill 中心の知識体系

開発プロセス・実装規約の手順的知識は、`plugins/what-number/` をソースとする
**Agent Plugin** の Skill として提供される。ローカル Plugin と外部 Azure Plugin は repository marketplace から GitHub Copilot 向けに提供される。
該当する作業を行う際は、対応する skill をロードしてその手順に従うこと。

| 側面 | skill |
|---|---|
| 開発プロセス | `issue-workflow`, `user-story-quality-check`, `incremental-planning`, `pr-review`, `branching-release`, `manual-test-gherkin`, `task-file-workflow` |
| 言語・アーキテクチャ | `java-spring-conventions`, `testing-strategy`, `thymeleaf-frontend`, `gradle-build`, `azure-cicd-ops`, `screenshot` |

Skill の管理・配備ルールは `docs/design/agent-design.md` を参照。

## 常時遵守すべきルール

### ドキュメント配置

| ドキュメント種別 | 配置場所 | 備考 |
|---|---|---|
| 要件定義 | `docs/specs/` | コード例を含めてはならない |
| 基本設計・アーキテクチャ | `docs/design/` | コード例を含めてはならない |
| 詳細設計 | 対応する issue 内 | リポジトリ内にファイルとして保持しない |
| テスト設計 | 対応する issue 内 | リポジトリ内にファイルとして保持しない |

### 記述フォーマット

- ユーザーストーリーの Acceptance Criteria: **EARS 形式**
- System Test および手動テストのテストケース: **Gherkin 形式**

### 「確認済み」ラベルの統制

- 「確認済み」ラベルは、`user-story-quality-check` skill のチェックフロー（ステップ 1〜6）を **すべて完遂し、構造化コメントを証跡として issue に記録した場合にのみ** 付与できる
- チェックフローを完遂せずに「確認済み」ラベルを付与してはならない
- ユーザーストーリー issue およびその sub-issue の作業開始には、親ユーザーストーリーへの「確認済み」ラベル付与が必要（詳細は `issue-workflow` skill）

### レビュー要件

- 実装・レビュー時は `docs/design/review-requirements.md` のチェック項目に準拠すること（運用手順は `pr-review` skill)
- 👤 人間 判定項目の合否を AI が最終判定してはならない

### ブランチ・PR

- GitHub Flow。ブランチ命名は `feature/#<issue番号>-<短い説明>`、PR は `Closes #<issue番号>` で issue を参照（詳細は `branching-release` skill）

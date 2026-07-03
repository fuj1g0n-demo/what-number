---
name: branching-release
description: 'GitHub Flow に基づくブランチ運用と PR 作成規則、およびリリース手順。ブランチ命名規則、PR 記載事項、cd-release ワークフローによるリリース実行手順を定義する。WHEN: "ブランチを作成", "PR を作成", "リリースして", "release", "バージョンを上げて", ブランチ・PR・リリース操作を行うとき。'
---

# ブランチ戦略とリリース

## ブランチ運用ルール（GitHub Flow）

1. **`main` ブランチ** は常にデプロイ可能な状態を維持する
2. 作業は issue ごとに **feature branch** を作成する
   - 命名規則: `feature/#<issue番号>-<短い説明>`
   - 例: `feature/#42-add-quiz-api`
3. feature branch から `main` への統合は **Pull Request（PR）** を通じて行う

## Pull Request ルール

1. PR は対応する issue を `Closes #<issue番号>` で参照すること
2. PR マージには最低 1 名のレビュー承認を必須とする（レビュー運用は `pr-review` skill を参照）
3. PR タイトルは変更内容を簡潔に表すこと
4. PR の説明には以下を含めること:
   - 変更の概要
   - 対応する issue 番号
   - テスト結果の概要

## バージョン管理の規律

- バージョンは **`gradle.properties` の `version` を単一正本** とする
- 開発中（`main`）は常に `-SNAPSHOT` 付き（例: `0.0.1-SNAPSHOT`）
- リリース版は `-SNAPSHOT` を外した不変版（例: `1.0.0`）
- **バージョン書き換えを手作業で行わないこと**（リリースワークフローが自動で行う）

## リリース手順

リリースは GitHub Actions の **`cd-release.yml` を手動実行** して行う。

1. リポジトリ → Actions → Release → Run workflow を開く
2. 入力欄に以下を指定して実行する:
   - `release_version`: リリースする版（`-SNAPSHOT` 無し）。例 `1.0.0`
   - `next_dev_version`: 次期開発版（`-SNAPSHOT` 付き）。例 `1.0.1-SNAPSHOT`

ワークフローが入力検証、ビルド・テスト、タグ付け（`v<release_version>`）、Maven 発行
（Azure Artifacts / GitHub Packages）、ACR へのコンテナイメージ発行、GitHub Release 作成、
次期開発版への戻しまでを自動実行する。詳細は README の「リリース手順」を参照。

- ACA / App Service への本番デプロイは cd-release.yml には含まれない（承認者が手動実施）
- リリースボットのコミットには `[skip ci]` が付与される
- CI/CD ワークフロー全体の構成は `azure-cicd-ops` skill を参照

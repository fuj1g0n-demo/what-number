---
name: azure-cicd-ops
description: '本リポジトリの CI/CD（GitHub Actions + Azure）運用知識。9 本のワークフローの役割、PR ごとのプレビュー環境（Container Apps / App Service）、ステージングデプロイ、TTL クリーンアップ、必要な Actions 変数を定義する。WHEN: "CI が失敗", "プレビュー環境", "デプロイ", "ワークフローを変更", "GitHub Actions", CI/CD の調査・変更・運用を行うとき。'
---

# CI/CD 運用（GitHub Actions + Azure）

詳細な手順・変数一覧は README の「CI/CD」セクションが正本。本 skill は全体像と調査の起点を提供する。

## ワークフロー一覧

| ワークフロー | 種別 | トリガー | 内容 |
|---|---|---|---|
| `ci-pr-local.yml` | CI | PR (main) | build 後に Unit / Integration / System Test を並行実行（System Test はランナー内でアプリを起動） |
| `ci-pr-remote.yml` | CI | PR (main) open/sync/close | PR ごとに Container App と App Service Web App のプレビュー環境を並行作成 → 固有 URL へデプロイ → E2E 実行 → URL を PR にコメント。クローズ時に削除 |
| `cd-merge.yml` | CD | push (main) / 手動 | bootJar を Azure Artifacts + GitHub Packages へ発行し、ACA / App Service のステージング環境へ並行デプロイ。各環境で HTTP 200 を確認 |
| `cd-release.yml` | CD | 手動 | 2 コミットでリリースタグを確定し、Maven / ACR へ発行して GitHub Release を作成。デプロイは行わない |
| `infra-bootstrap.yml` | セットアップ | 手動 | 初回セットアップ用に RG・ACR・マネージド ID・ACA 環境・App Service プランを作成 |
| `ttl-cleanup.yml` | 運用 | 15 分毎 / 手動 | TTL 期限切れ（最終 push から 1 時間）のプレビュー環境を削除 |
| `dependency-submission.yml` | 依存関係 | push (main) / 手動 | Gradle の依存関係グラフを GitHub Dependency Graph へ送信 |
| `dependency-review.yml` | 依存関係 | PR (main) | head の依存関係グラフを送信後、base との差分を確認 |
| `codeql.yml` | セキュリティ | push (main) / PR (main) / 毎週 | GitHub Actions と Java を CodeQL で解析。現在の設定では Kotlin を解析しない |

## プレビュー環境の命名

- Container Apps: `<AZURE_CONTAINERAPP_NAME>-pr-<PR番号>`（共有 ACA 環境内、固有 Ingress FQDN）
- App Service: `<APP_SERVICE_WEBAPP_PREFIX>-pr-<PR番号>`（共有プラン上、`*.azurewebsites.net`）
- ステージング: それぞれ `-staging` サフィックス

## 運用上の注意（Gotchas）

- App Service プランは空になると自動削除されることがあるため、`cd-merge.yml` の `deploy-appservice` ジョブはデプロイ前にプランの存在を確認し、無ければ冪等に再作成する
- リリースボットのコミットには `[skip ci]` が付与され、`cd-merge.yml` は起動しない
- `main` がブランチ保護で直接 push を禁止している場合、リリース実行者に保護のバイパス許可が必要
- 認証は Microsoft Entra ID の OIDC フェデレーション資格情報（シークレットレス）。必要な Actions Variables / Secrets の一覧は README を参照

## CI 失敗時の調査手順

1. `gh run list --branch <ブランチ> --limit 5` で対象 run を特定する
2. `gh run view <run-id> --log-failed` で失敗ジョブのログを確認する
3. remote E2E 失敗時は `ci-pr-remote.yml` の artifact（スクリーンショット・テストレポート）をダウンロードして確認する

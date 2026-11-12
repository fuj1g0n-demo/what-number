---
name: azure-cicd-ops
description: '本リポジトリの CI/CD（GitHub Actions + Azure）運用知識。6 本のワークフローの役割、PR ごとのプレビュー環境（Container Apps / App Service）、ステージングデプロイ、TTL クリーンアップ、必要な Actions 変数を定義する。WHEN: "CI が失敗", "プレビュー環境", "デプロイ", "ワークフローを変更", "GitHub Actions", CI/CD の調査・変更・運用を行うとき。'
---

# CI/CD 運用（GitHub Actions + Azure）

詳細な手順・変数一覧は README の「CI/CD」セクションが正本。本 skill は全体像と調査の起点を提供する。

## ワークフロー一覧

| ワークフロー | 種別 | トリガー | 内容 |
|---|---|---|---|
| `ci-pr-local.yml` | CI | PR (main) | Build → Unit → Integration → System Test（ランナー内でローカル起動して検証）。スクリーンショット・テストレポートを artifact 化 |
| `ci-pr-remote.yml` | CI | PR (main) open/sync/close | PR ごとに Container App と App Service Web App のプレビュー環境を並行作成 → 固有 URL へデプロイ → E2E 実行 → URL を PR にコメント。クローズ時に削除 |
| `cd-merge.yml` | CD | push (main) / 手動 | bootJar を Azure Artifacts + GitHub Packages へ発行し、ACA / App Service のステージング環境へ並行デプロイ。スモークチェック付き |
| `cd-release.yml` | CD | 手動 | バージョン確定 → タグ付け → Maven / ACR 発行 → GitHub Release（`branching-release` skill 参照） |
| `infra-bootstrap.yml` | セットアップ | 手動 | RG・ACR・マネージド ID・ACA 環境・App Service プランを作成（初回のみ） |
| `ttl-cleanup.yml` | 運用 | 15 分毎 / 手動 | TTL 期限切れ（最終 push から 1 時間）のプレビュー環境を削除 |

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
3. System Test 失敗時は artifact（スクリーンショット・テストレポート）をダウンロードして確認する

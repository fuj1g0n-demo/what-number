# この数字、なんの数字？

金融・公共のお客様向けデモ用クイズアプリケーションです。

## 技術スタック

- **言語**: Java 21（LTS）
- **フレームワーク**: Spring Boot 3.4 + Thymeleaf（SSR）
- **フロントエンド**: Bootstrap 5（CDN）
- **データ管理**: JSON ファイル（起動時ロード）
- **ビルドツール**: Gradle（Kotlin DSL）

## 前提条件

- Java 21 以上

## ビルド・実行

```bash
# ビルド（Unit Test + Integration Test を含む）
./gradlew :app:build

# アプリケーション起動
./gradlew :app:bootRun
```

ブラウザで http://localhost:8080 にアクセスしてください。

## テスト

```bash
# Unit Test
./gradlew :app:test

# Integration Test
./gradlew :app:integrationTest

# System Test（事前にアプリケーション起動が必要）
./gradlew :system-test:test

# カバレッジレポート（Unit + Integration）
./gradlew :app:jacocoFullReport
```

## プロジェクト構成

Gradle マルチプロジェクト構成（モノレポ）を採用しています。

```
├── app/                       ... メインアプリケーション
│   ├── build.gradle.kts
│   └── src/
│       ├── main/
│       │   ├── java/com/example/quiz/
│       │   │   ├── controller/    ... Controller 層
│       │   │   ├── service/       ... Service 層
│       │   │   ├── model/         ... データモデル（Record）
│       │   │   └── dataloader/    ... データロード
│       │   └── resources/
│       │       ├── templates/     ... Thymeleaf テンプレート
│       │       ├── static/css/    ... スタイルシート
│       │       └── data/          ... クイズデータ JSON
│       ├── test/                  ... Unit Test
│       └── integrationTest/       ... Integration Test
├── system-test/                   ... System Test（独立サブプロジェクト）
│   ├── build.gradle.kts
│   └── src/test/                  ... Playwright + JUnit 5
├── docs/                          ... ドキュメント
├── Dockerfile                     ... アプリのコンテナイメージ定義
├── build.gradle.kts               ... ルートプロジェクト
└── settings.gradle.kts            ... サブプロジェクト定義
```

## コンテナ実行

アプリは Spring Boot 実行可能 JAR をマルチステージビルドでコンテナ化しています。

```bash
# イメージのビルド
docker build -t quiz-app .

# コンテナの起動
docker run --rm -p 8080:8080 quiz-app
```

ブラウザで http://localhost:8080 にアクセスしてください。

## CI/CD（GitHub Actions + Azure Container Apps / App Service）

PR ごとに専用のプレビュー環境を **2 種類の Azure ホスティング**（Azure Container Apps と Azure App Service）へ
並行作成し、それぞれ **PR 固有の URL** で公開します。
デプロイ後、各 URL に対して E2E（Playwright System Test）を自動実行します。

### ワークフロー一覧

| ワークフロー | 種別 | トリガー | デプロイ先 | 内容 |
|---|---|---|---|---|
| `ci-pr-local.yml` | CI | PR (main): open / synchronize / reopen | なし（ローカル） | Build → Unit → Integration → System Test（ランナー内でローカル起動して検証） |
| `ci-pr-remote.yml` | CI | PR (main): open / synchronize / reopen / close | PR 専用プレビュー環境（固有 URL） | PR ごとに **Container App**（コンテナ）と **App Service Web App**（Java SE 21 / bootJar）を並行作成 → 各固有 URL でデプロイ → E2E 実行 → URL を PR にコメント。クローズ時に両方を削除 |
| `cd-merge.yml` | CD | push (main)（= マージ） / 手動 | ステージング環境 | bootJar を Azure Artifacts (Maven) へ発行し、**ACA** と **App Service** の 2 つのステージング環境へ並行デプロイ（リリース時に本番 prod へ展開する想定） |
| `infra-bootstrap.yml` | セットアップ | 手動（`workflow_dispatch`） | － | 新規リソースグループ・ACR・マネージド ID・ACA 環境・App Service プランを作成（初回 1 回のみ） |
| `ttl-cleanup.yml` | 運用 | 15 分毎 / 手動 | － | TTL 期限切れ（最終 push から 1 時間）のプレビュー環境（Container App / Web App）を削除 |

### PR ごとの URL の仕組み

App Service のデプロイスロットに相当する仕組みとして、PR ごとに独立したプレビュー環境を作成します。

- **Azure Container Apps**: 共有 ACA 環境内に Container App
  （`<AZURE_CONTAINERAPP_NAME>-pr-<PR番号>`）を作成。各 Container App は固有の Ingress FQDN を持つ。
- **Azure App Service**: 共有 App Service プラン上に Web App
  （`<APP_SERVICE_WEBAPP_PREFIX>-pr-<PR番号>`）を作成。`*.azurewebsites.net` の固有 FQDN を持つ。

いずれも PR 単位で URL が分離され、PR クローズ時または TTL 期限切れで自動削除されます。

### マージ時のステージングデプロイ（`cd-merge.yml`）

`main` への push（PR マージ）または手動実行で、以下を行います（リリース時に本番 prod へ展開する想定）。

- **Java 成果物の発行**: `bootJar` を Azure DevOps の Azure Artifacts（Maven フィード）へ
  `com.example:what-number:0.0.1-<short-sha>` として発行する。
- **ステージングへの並行デプロイ**:
  - Azure Container Apps: `<AZURE_CONTAINERAPP_NAME>-staging`
  - Azure App Service: `<APP_SERVICE_WEBAPP_PREFIX>-staging`（Java SE 21）

各デプロイ後に HTTP 200 を確認するスモークチェックを実行します。

### セットアップ手順

1. **Microsoft Entra ID にアプリ登録**を作成し、GitHub Actions 用の
   **OIDC フェデレーション資格情報**を構成する。
   サービスプリンシパルには、初回の `infra-bootstrap` 実行のために対象サブスクリプション/リソースグループへ
   ロール割り当て権限（`Owner` もしくは `Contributor` + `User Access Administrator`）を付与する。
2. リポジトリ → **Settings → Secrets and variables → Actions → Variables** に以下を登録する。
3. **`infra-bootstrap.yml` を手動実行**して共有インフラ（新規リソースグループ含む）を作成する。
4. 以後、PR を作成すると `ci-pr-remote.yml` が自動的にプレビュー環境をデプロイし、E2E を実行する。

### 必要な GitHub Actions Variables

| 変数名 | 説明 | 例 |
|---|---|---|
| `AZURE_CLIENT_ID` | OIDC 用アプリ登録のクライアント ID | `00000000-0000-...` |
| `AZURE_TENANT_ID` | テナント ID | `00000000-0000-...` |
| `AZURE_SUBSCRIPTION_ID` | サブスクリプション ID | `00000000-0000-...` |
| `AZURE_RESOURCE_GROUP` | 新規作成するリソースグループ名 | `rg-quiz-app` |
| `AZURE_LOCATION` | リージョン | `japaneast` |
| `ACR_NAME` | Azure Container Registry 名（グローバルで一意・英数字のみ） | `quizappacr12345` |
| `ACA_ENVIRONMENT` | Container Apps 環境名 | `cae-quiz-app` |
| `ACR_PULL_IDENTITY_NAME` | ACR Pull 用ユーザー割り当てマネージド ID 名 | `id-quiz-app-acrpull` |
| `AZURE_CONTAINERAPP_NAME` | 本番／プレビューの基底アプリ名（Container Apps） | `quiz-app` |
| `APP_SERVICE_PLAN` | App Service プラン名（Linux） | `asp-quiz-app-preview` |
| `APP_SERVICE_WEBAPP_PREFIX` | App Service プレビュー／ステージング Web App 名の接頭辞（グローバルで一意） | `quiz-app-12345` |
| `AZURE_DEVOPS_ORG` | Azure DevOps 組織名（Azure Artifacts 発行先） | `fuj1g0n-demo` |
| `AZURE_DEVOPS_PROJECT` | Azure DevOps プロジェクト名（project スコープフィード） | `what-number` |
| `AZURE_ARTIFACTS_FEED` | Azure Artifacts のフィード名（project スコープ） | `what-number` |

> 任意（未設定時は既定値）の変数:
>
> | 変数名 | 説明 | 既定値 |
> |---|---|---|
> | `APP_SERVICE_LOCATION` | App Service プランのリージョン（VM クォータのある別リージョンを指定可能） | `AZURE_LOCATION` |
> | `APP_SERVICE_PLAN_SKU` | App Service プランの SKU（VM クォータが無い環境では `F1`） | `B1` |

### 必要な GitHub Actions Secrets

| シークレット名 | 説明 |
|---|---|
| `AZURE_DEVOPS_PAT` | Azure DevOps の Personal Access Token（Azure Artifacts の Read & Write スコープ）。`cd-merge.yml` の成果物発行で使用 |

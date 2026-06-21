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

## CI/CD（GitHub Actions + Azure Container Apps）

PR ごとに専用の Azure Container App を作成し、**PR 固有の URL** でプレビュー環境を公開します。
デプロイ後、その URL に対して E2E（Playwright System Test）を自動実行します。

### ワークフロー一覧

| ワークフロー | 種別 | トリガー | デプロイ先 | 内容 |
|---|---|---|---|---|
| `ci-pr-local.yml` | CI | PR (main): open / synchronize / reopen | なし（ローカル） | Build → Unit → Integration → System Test（ランナー内でローカル起動して検証） |
| `ci-pr-remote.yml` | CI | PR (main): open / synchronize / reopen / close | PR 専用プレビュー環境（固有 URL） | PR ごとの Container App を作成 → 固有 URL でデプロイ → E2E 実行 → URL を PR にコメント。クローズ時に削除 |
| `cd-merge.yml` | CD | push (main)（= マージ） / 手動 | 本番環境 | 本番用 Container App をビルド・デプロイ |
| `infra-bootstrap.yml` | セットアップ | 手動（`workflow_dispatch`） | － | 新規リソースグループ・ACR・マネージド ID・ACA 環境を作成（初回 1 回のみ） |
| `ttl-cleanup.yml` | 運用 | 15 分毎 / 手動 | － | TTL 期限切れ（最終 push から 1 時間）のプレビュー環境を削除 |

### PR ごとの URL の仕組み

App Service のデプロイスロットに相当する仕組みとして、PR ごとに独立した Container App
（`<AZURE_CONTAINERAPP_NAME>-pr-<PR番号>`）を共有 ACA 環境内に作成します。
各 Container App は固有の Ingress FQDN を持つため、PR 単位で URL が分離されます。

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
| `AZURE_CONTAINERAPP_NAME` | 本番／プレビューの基底アプリ名 | `quiz-app` |

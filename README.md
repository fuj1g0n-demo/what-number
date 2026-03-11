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
│       │   │   ├── controller/   ... Controller 層
│       │   │   ├── service/       ... Service 層
│       │   │   ├── model/         ... データモデル（Record）
│       │   │   └── dataloader/    ... データロード
│       │   └── resources/
│       │       ├── templates/     ... Thymeleaf テンプレート
│       │       ├── static/css/    ... スタイルシート
│       │       └── data/          ... クイズデータ JSON
│       ├── test/                  ... Unit Test
│       └── integrationTest/       ... Integration Test
├── system-test/               ... System Test（独立サブプロジェクト）
│   ├── build.gradle.kts
│   └── src/test/              ... Playwright + JUnit 5
├── docs/                      ... ドキュメント
├── build.gradle.kts           ... ルートプロジェクト
└── settings.gradle.kts        ... サブプロジェクト定義
```
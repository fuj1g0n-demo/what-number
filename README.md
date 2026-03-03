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
# ビルド
./gradlew build

# アプリケーション起動
./gradlew bootRun
```

ブラウザで http://localhost:8080 にアクセスしてください。

## テスト

```bash
# Unit Test
./gradlew test

# Integration Test
./gradlew integrationTest

# System Test（事前にアプリケーション起動が必要）
./gradlew systemTest
```

## プロジェクト構成

```
src/
├── main/
│   ├── java/com/example/quiz/
│   │   ├── controller/   ... Controller 層
│   │   ├── service/       ... Service 層
│   │   ├── model/         ... データモデル（Record）
│   │   └── dataloader/    ... データロード
│   └── resources/
│       ├── templates/     ... Thymeleaf テンプレート
│       ├── static/css/    ... スタイルシート
│       └── data/          ... クイズデータ JSON
├── test/                  ... Unit Test
├── integrationTest/       ... Integration Test
└── systemTest/            ... System Test（Playwright）
```
---
name: gradle-build
description: 'Gradle（Kotlin DSL）によるビルド・依存管理の規約。マルチプロジェクト構成、Spring Boot BOM への委任、gradle.properties によるバージョン管理を定義する。WHEN: "build.gradle.kts を変更", "依存を追加", "ビルド設定", "Gradle", ビルドスクリプト・依存関係の変更を行うとき。'
---

# Gradle ビルド・依存管理規約

## プロジェクト構成

Gradle マルチプロジェクト構成（モノレポ）:

| サブプロジェクト | 役割 |
|---|---|
| `app` | メインアプリケーション + Unit Test + Integration Test |
| `system-test` | Playwright E2E（独立サブプロジェクト。app に依存しない） |

- ルートの `build.gradle.kts` に共通設定、`settings.gradle.kts` にサブプロジェクト定義
- `system-test` はメインアプリのクラスに依存せず、HTTP 経由で接続する構成を維持する

## ビルドスクリプトの規約

- ビルドスクリプトは **Kotlin DSL（build.gradle.kts）** で型安全に記述する
- Gradle Wrapper（`./gradlew`）を必ず使用する（バージョン統一・CI 再現性のため）

## 依存管理

- **Spring Boot BOM** で管理可能なライブラリはバージョンを明記せず BOM に委任する
- 不要な外部ライブラリを追加しない（技術選定 5 原則「最小依存」「シンプルさ優先」）
- 依存追加時は `docs/design/tech-stack.md` の技術選定原則との整合性を確認する

## バージョン管理

- バージョンは `gradle.properties` の `version` が単一正本
- 開発中は `-SNAPSHOT` 付き。リリース時の書き換えは `cd-release.yml` が自動で行う（`branching-release` skill 参照）
- 手作業でのバージョン書き換えを禁止する

## レビュー観点

`docs/design/review-requirements.md` の I-7（最小依存）, I-8（ビルド設定）を参照。

---
name: testing-strategy
description: '本リポジトリの 3 レベルテスト戦略（Unit / Integration / System）。各レベルの技術スタック、ソースセット配置、Gradle 実行コマンド、Playwright E2E の構成を定義する。WHEN: "テストを書いて", "テストを実行", "run tests", "Playwright", "System Test", テストの実装・実行・配置を行うとき。'
---

# テスト戦略（3 レベル）

テスト戦略の設計根拠は `docs/design/tech-stack.md` の「テスト戦略と技術選定」が正本。

## テストレベルと実行コマンド

| テスト種別 | 技術スタック | 配置（ソースセット） | 実行コマンド |
|---|---|---|---|
| Unit Test | JUnit 5 + Mockito + AssertJ | `app/src/test/` | `./gradlew :app:test` |
| Integration Test | JUnit 5 + Spring Boot Test | `app/src/integrationTest/` | `./gradlew :app:integrationTest` |
| System Test | Playwright（Java）+ JUnit 5 | `system-test/src/test/` | `./gradlew :system-test:test` |

- ビルド（Unit + Integration を含む）: `./gradlew :app:build`
- カバレッジレポート（Unit + Integration）: `./gradlew :app:jacocoFullReport`

## Unit Test の規約

- 個々のクラス・メソッド単位で検証する
- 外部依存は Mockito でモック化する
- アサーションは AssertJ の流暢な API を使用する
- テストメソッド名・アサーションメッセージから意図・期待結果が読み取れるようにする

## Integration Test の規約

- `@SpringBootTest` / `@WebMvcTest` + MockMvc を使用し、Controller-Service-Repository 間の結合を検証する
- ソースセット `integrationTest` に配置する
- 画面表示の失敗・エラー系（存在しない ID 等）は Integration Test（Controller 層の異常系）でカバーする

## System Test の規約

- `system-test` は **独立サブプロジェクト**。メインアプリのクラスに依存せず、HTTP 経由で起動済みアプリに接続する
- 実行前にアプリケーションの起動が必要: `./gradlew :app:bootRun`（デフォルト http://localhost:8080）
- 接続先 URL はシステムプロパティ `test.base-url` で外部から指定可能
- テストケースは **Gherkin 形式** の設計に基づいて実装する
- 検証対象は「正常動作の画面表示」に限定する:
  - 一覧画面（`/`）: 正常表示のみ
  - 出題画面（`/quiz/{id}`）: 有効 ID での正常表示のみ
  - 結果画面（`/quiz/{id}/answer`）: 正解・不正解の両パターン
- スクリーンショットは `system-test/build/screenshots/` に保存され、CI で artifact としてアップロードされる

## 共通規約

- 各テストは他のテストに依存せず、単独で実行可能であること
- テストデータのセットアップ・クリーンアップを適切に行うこと
- レビュー観点は `docs/design/review-requirements.md` の T-1〜T-6 を参照

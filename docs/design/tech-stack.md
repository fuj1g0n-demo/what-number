# 技術スタック定義

## 概要

本ドキュメントは「この数字、なんの数字？」クイズアプリケーションの技術スタックを定義する。
デモ用途のWebアプリケーションとして、開発生産性・保守性・テスト容易性を重視した選定を行う。

---

## 言語・ランタイム

| 項目 | 選定 | バージョン | 選定理由 |
|---|---|---|---|
| 言語 | Java | 21（LTS） | 長期サポート、Record・Sealed Classes 等の最新言語機能を活用可能 |
| ランタイム | JVM | — | Java 21 対応の JVM |

### Java 21 を選定する根拠

- 2023年9月リリースの最新 LTS（Long Term Support）バージョン
- Virtual Threads（Project Loom）により軽量な並行処理が可能
- Pattern Matching、Record Patterns 等でボイラープレートを削減
- Spring Boot 3.x が Java 17+ を必須としており、21 は推奨バージョン

---

## フレームワーク・ライブラリ

### バックエンド

| カテゴリ | 選定 | 選定理由 |
|---|---|---|
| Web フレームワーク | Spring Boot 3.4 系 | Java 21 完全対応、豊富なエコシステム、Web アプリ構築の標準 |
| テンプレートエンジン | Thymeleaf | Spring Boot との公式統合、Natural Templating によるプレビュー可能な HTML |
| JSON 処理 | Jackson（Spring Boot 同梱） | Spring Boot のデフォルト JSON ライブラリ、追加依存不要 |
| バリデーション | Jakarta Bean Validation（Hibernate Validator） | Spring Boot 標準のバリデーション機構 |

### フロントエンド

| カテゴリ | 選定 | 選定理由 |
|---|---|---|
| レンダリング方式 | サーバーサイドレンダリング（SSR） | Thymeleaf による SSR でシンプルな構成を維持、SPA の複雑さを回避 |
| CSS フレームワーク | Bootstrap 5 | CDN 利用可能、デモ用途に十分なデザイン品質、学習コスト低 |
| JavaScript | Vanilla JS（必要最小限） | デモ用途のため SPA フレームワーク不要、依存を最小化 |

### SSR（サーバーサイドレンダリング）を採用する根拠

- デモ用クイズアプリであり、リッチなクライアントサイド状態管理は不要
- Thymeleaf + Spring Boot の組み合わせで開発・テストがシンプルになる
- フロントエンドビルドパイプライン（Node.js / npm）が不要となり、構成を簡素化
- Playwright による System Test は SSR / SPA いずれでも対応可能

---

## データ管理

| 項目 | 選定 | 選定理由 |
|---|---|---|
| データ形式 | JSON ファイル | 静的なクイズデータのみ、RDB は過剰 |
| 配置場所 | クラスパス上（resources ディレクトリ） | Spring Boot の ResourceLoader で容易にアクセス可能 |
| ロードタイミング | アプリケーション起動時 | ApplicationRunner または @PostConstruct で初期化 |
| メモリ上のデータ構造 | Java Record + List/Map | イミュータブルなデータモデルで安全性を確保 |

### RDB を使用しない根拠

- クイズの問題データは静的であり、CRUD 操作が不要
- データ量が小規模（メモリ上に全件保持可能）
- DB サーバーの運用・接続管理が不要となり、デモ環境構築が容易
- JSON ファイルの編集のみでデータ追加・変更が可能

---

## ビルド・依存管理

| 項目 | 選定 | 選定理由 |
|---|---|---|
| ビルドツール | Gradle | 柔軟なビルドスクリプト、Spring Boot との親和性 |
| ビルドスクリプト DSL | Kotlin DSL（build.gradle.kts） | 型安全、IDE 補完が効く、Groovy DSL より保守性が高い |
| Gradle Wrapper | 使用する | チーム全員が同一バージョンの Gradle を使用することを保証 |
| 依存管理 | Spring Boot BOM（Bill of Materials） | Spring 関連ライブラリのバージョン整合性を自動管理 |

---

## テスト戦略と技術選定

### テストピラミッド対応

プロジェクトで定義された 3 種類のテストに対する技術選定:

| テスト種別 | 技術スタック | 実行方式 | 対象 |
|---|---|---|---|
| Unit Test | JUnit 5 + Mockito + AssertJ | Gradle `test` タスク | 個々のクラス・メソッド |
| Integration Test | JUnit 5 + Spring Boot Test（@SpringBootTest） | Gradle `integrationTest` タスク | Controller-Service-Repository 間の結合 |
| System Test | Playwright（Java） + JUnit 5 | Gradle `systemTest` タスク | ブラウザ経由のエンドツーエンド |

### 各テストレベルの詳細

#### Unit Test

- **JUnit 5**: Java の標準テストフレームワーク、パラメータ化テスト等の高度な機能
- **Mockito**: 依存オブジェクトのモック生成、Spring Boot Test との統合が容易
- **AssertJ**: 流暢なアサーション API、可読性の高いテストコード

#### Integration Test

- **@SpringBootTest**: Spring コンテキスト全体または一部を起動してテスト
- **@WebMvcTest**: Controller 層のみの軽量テスト
- **MockMvc**: HTTP リクエスト・レスポンスのテスト
- Gradle でソースセット分離し、Unit Test と独立して実行可能とする

#### System Test

- **Playwright（Java 版）**: モダンなブラウザ自動化ライブラリ、Chromium / Firefox / WebKit 対応
- **JUnit 5 統合**: Playwright を JUnit 5 のテストライフサイクルで管理
- テスト対象のアプリケーションを起動した状態で、実ブラウザ経由の E2E テストを実行
- Gherkin 形式のテストケース設計に基づき、Playwright でテストシナリオを実装

### Playwright を選定する根拠（Selenium との比較）

- 自動待機（Auto-wait）機構により、フレーキーテストが少ない
- ブラウザインストールが自動化されており、環境構築が容易
- Java API が公式提供されており、JUnit 5 との統合がシンプル
- 複数ブラウザ（Chromium / Firefox / WebKit）のクロスブラウザテストが容易

---

## プロジェクト構成

### ディレクトリ構成方針

```
プロジェクトルート/
├── src/
│   ├── main/
│   │   ├── java/          ... アプリケーションコード
│   │   └── resources/
│   │       ├── templates/ ... Thymeleaf テンプレート
│   │       ├── static/    ... 静的リソース（CSS, JS, 画像）
│   │       ├── data/      ... クイズデータ JSON
│   │       └── application.yml
│   ├── test/              ... Unit Test
│   ├── integrationTest/   ... Integration Test
│   └── systemTest/        ... System Test（Playwright）
├── docs/
│   ├── specs/             ... 要件定義
│   └── design/            ... 基本設計・アーキテクチャ
├── build.gradle.kts
├── settings.gradle.kts
└── gradle/
    └── wrapper/
```

### パッケージ構成方針

- レイヤードアーキテクチャを採用（Controller → Service → Repository / DataLoader）
- 機能ドメインごとにサブパッケージを分割

---

## CI/CD

| 項目 | 選定 | 選定理由 |
|---|---|---|
| CI サービス | GitHub Actions | GitHub Flow との自然な統合、リポジトリ内で完結 |
| CI パイプライン | PR 時に自動実行 | Unit Test → Integration Test → ビルド を自動実行 |
| System Test 実行 | CI 上で実行 | Playwright のヘッドレスモードにより CI 上でも E2E テスト実行可能 |

### CI ワークフロー方針

- PR 作成・更新時にすべての自動テスト（Unit / Integration / System）を実行
- main ブランチへのマージ後にもテストを実行し、デプロイ可能状態を検証
- Gradle Wrapper を使用し、CI 環境でのビルド再現性を確保

---

## バージョン一覧（想定）

| 技術要素 | バージョン |
|---|---|
| Java | 21（LTS） |
| Spring Boot | 3.4.x |
| Thymeleaf | Spring Boot BOM 管理 |
| Gradle | 8.x |
| JUnit | 5.x（Spring Boot BOM 管理） |
| Mockito | Spring Boot BOM 管理 |
| AssertJ | Spring Boot BOM 管理 |
| Playwright for Java | 最新安定版 |
| Bootstrap | 5.x（CDN） |

---

## 技術選定の原則

1. **シンプルさ優先**: デモ用途のため、過度に複雑な技術は採用しない
2. **Spring Boot エコシステム活用**: 可能な限り Spring Boot 標準の仕組みを使い、独自実装を減らす
3. **テスト容易性**: 3 種類のテストレベルそれぞれに適した技術を選定
4. **ローカル完結**: 外部サービスへの依存を最小化し、ローカル環境で完結して動作可能
5. **最小依存**: 不要なライブラリを追加しない、Spring Boot BOM で管理可能なものは BOM に任せる

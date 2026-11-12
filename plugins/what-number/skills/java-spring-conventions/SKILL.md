---
name: java-spring-conventions
description: '本リポジトリの Java / Spring Boot 実装規約。Java 21 言語機能の活用、コンストラクタインジェクション、レイヤードアーキテクチャ（Controller → Service → Repository / DataLoader）の責務分離、イミュータブルデータモデルを定義する。WHEN: "Java コードを実装", "Controller/Service を追加", "Spring Boot", Java コードの実装・変更・レビューを行うとき。'
---

# Java / Spring Boot 実装規約

技術選定の根拠・全体像は `docs/design/tech-stack.md` が正本。本 skill は実装時に守るべき規約を要約する。

## Java 21 言語機能の活用

- Record、Sealed Classes、Pattern Matching 等の Java 21 言語機能を適切に活用し、不要なボイラープレートを残さない
- データモデルは **Java Record + List/Map によるイミュータブルな構造** で表現する
- クイズデータは静的 JSON ファイル（`app/src/main/resources/data/`）から起動時にロードする（RDB は使用しない）

## Spring Boot 規約

- DI は **コンストラクタインジェクション** を推奨する（フィールドインジェクションは使用しない）
- `@Configuration`、`@Service`、`@Controller` 等のアノテーション規約に準拠する
- 可能な限り Spring Boot 標準の仕組みを使い、独自実装を減らす（技術選定 5 原則「Spring Boot エコシステム活用」）
- JSON 処理は Jackson（Spring Boot 同梱）、バリデーションは Jakarta Bean Validation（Hibernate Validator）を使用する

## レイヤードアーキテクチャ

パッケージ構成: `com.example.quiz` 配下に `controller` / `service` / `model` / `dataloader`

- 依存方向は **Controller → Service → Repository / DataLoader** の一方向とする
- レイヤーの跨ぎ越し（Controller から Repository / DataLoader を直接呼ぶ等）を禁止する
- 機能ドメインごとにサブパッケージを分割する

## セキュリティ・堅牢性

- ユーザー入力には Jakarta Bean Validation によるバリデーションを適用する
- 予期しない例外に対する適切なエラーハンドリングを行い、スタックトレースをユーザーに露出させない
- テンプレート側の XSS 対策は `thymeleaf-frontend` skill を参照

## 依存追加の原則

- 不要な外部ライブラリを追加しない（技術選定 5 原則「最小依存」）
- Spring Boot BOM で管理可能なバージョンは BOM に委任する（詳細は `gradle-build` skill）

## レビュー観点との対応

実装 PR のレビューでは `docs/design/review-requirements.md` の I-1〜I-8, S-1〜S-3 が適用される。
実装時点でこれらの観点を満たすこと。

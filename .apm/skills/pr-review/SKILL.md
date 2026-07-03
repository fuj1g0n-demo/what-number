---
name: pr-review
description: '本リポジトリの PR レビュー運用手順。docs/design/review-requirements.md のチェック項目適用、AI 自動判定と人間レビュー必須項目の役割分担、Must/Want 指摘区分、Approve・マージ条件を定義する。WHEN: "PR をレビューして", "review this PR", "レビュー要件を確認", PR の作成・レビュー・マージ判断を行うとき。'
---

# PR レビュー運用

## 必読ドキュメント

レビュー実施前に必ず `docs/design/review-requirements.md` を読み、PR のカテゴリ
（全 PR 共通 / design / implementation / manual-test）に対応するチェック項目を特定すること。
チェック項目の一覧・詳細は同ドキュメントが唯一の正本であり、本 skill には複製しない。

## 判定方式の役割分担

各チェック項目には判定方式が定義されている:

- **🤖 AI 項目**: AI が自動で合否を判定する
- **👤 人間 項目**: AI は関連する差分・情報の抽出と分析結果・懸念点の提示までを行い、**最終判定は必ず人間レビュアーに委ねる**。AI が 👤 人間 項目の合否を最終判定してはならない

## レビュー手順

1. PR のラベル・参照 issue から PR カテゴリを特定する
2. `docs/design/review-requirements.md` の「全 PR 共通チェック項目」と、カテゴリ別チェック項目を列挙する
3. 🤖 AI 項目を 1 件ずつ検証し、合否を判定する
4. 👤 人間 項目について、関連差分の抽出と分析結果・懸念点を整理して提示する
5. 結果をチェック項目番号（C-1, D-1, I-1, T-1, S-1, M-1 等）付きで報告する

## 指摘の区分

- レビュー指摘は「**Must（必須修正）**」と「**Want（推奨・任意）**」を明示する
- Must 指摘がすべて解消されるまで Approve しない

## Approve・マージ条件

- PR マージには最低 1 名の人間レビュアーの承認が必須
- CI（Unit / Integration / System Test）がすべてパスしていない PR はマージしない
- すべての 👤 人間 項目について人間レビュアーの OK が得られるまでマージしない
- 設計 PR は技術スタック定義の 5 原則（`docs/design/tech-stack.md`）との整合性を重点的に確認する
- 実装 PR はテスト計画との整合性（T-1）を最優先で確認する

## 実装時の注意

コードを実装・変更する際も、対応するカテゴリのレビュー観点を満たすように実装すること。
実装規約の詳細は `java-spring-conventions`, `testing-strategy`, `thymeleaf-frontend`, `gradle-build` の各 skill を参照する。

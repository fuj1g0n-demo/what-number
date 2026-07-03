---
name: thymeleaf-frontend
description: 'Thymeleaf SSR + Bootstrap 5 によるフロントエンド実装規約。Natural Templating、th:text による XSS 対策、CDN 利用、Vanilla JS 最小化を定義する。WHEN: "テンプレートを作成", "画面を実装", "HTML/CSS を変更", "Thymeleaf", フロントエンド・テンプレートの実装・変更を行うとき。'
---

# Thymeleaf / フロントエンド実装規約

## レンダリング方式

- サーバーサイドレンダリング（SSR）のみ。SPA フレームワークは使用しない
- テンプレートは `app/src/main/resources/templates/`、静的リソースは `app/src/main/resources/static/` に配置する

## Natural Templating

- テンプレートは **HTML として単体プレビュー可能** な Natural Templating として記述する
- ブラウザで直接開いた場合にも壊れないマークアップを維持する

## XSS 対策

- 出力には `th:text`（自動エスケープ）を使用する
- `th:utext` の不用意な使用を禁止する（使用する場合は明確な理由と安全性の担保が必要）

## CSS / JavaScript

- CSS フレームワークは **Bootstrap 5（CDN）** を使用する
- JavaScript は **Vanilla JS を必要最小限** にとどめる
- JavaScript フレームワーク・ライブラリの追加、フロントエンドビルドパイプライン（Node.js / npm）の導入を禁止する

## 検証

- 画面の視覚的確認には `screenshot` skill を利用できる
- レビュー観点は `docs/design/review-requirements.md` の I-5, I-6, S-2 を参照

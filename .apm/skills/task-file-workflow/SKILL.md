---
name: task-file-workflow
description: 'GitHub issue の代わりにリポジトリ内 docs/tasks/ の Markdown ファイルでタスクを管理する方式。ディレクトリ構成、タスクファイルのフロントマター、ステータス遷移を定義する。WHEN: "タスクファイルで管理", "task file", "docs/tasks にタスクを作成", ローカルファイル駆動でユーザーストーリー・タスクを管理するとき。'
---

# タスクファイル駆動ワークフロー

タスクの管理を GitHub issue ではなく、リポジトリ内の `docs/tasks/` ディレクトリに配置する
**Markdown ファイル** で行う方式。issue 管理（`issue-workflow` skill）の代替手段である。

## ディレクトリ構成

```
docs/tasks/
├── <ユーザーストーリーID>/
│   ├── user-story.md          # ユーザーストーリー本体
│   ├── design/                # 設計タスク
│   │   └── <連番>-<短い説明>.md
│   ├── implementation/        # 実装タスク
│   │   └── <連番>-<短い説明>.md
│   └── manual-test/           # 手動テストタスク
│       └── <連番>-<短い説明>.md
```

## ユーザーストーリー ID

ユーザーから指定された名称、または内容を端的に表す短い英数字ケバブケース文字列を使用する
（例: `quiz-hint-feature`, `score-display`）。

## タスクファイルのフォーマット

各タスクファイル（`user-story.md` およびサブタスクファイル）は以下の YAML フロントマターを含むこと:

```markdown
---
title: <タスクタイトル>
category: <user-story | design | implementation | manual-test>
status: <not-started | in-progress | done | verified>
depends-on: []  # 依存するサブタスクファイルの相対パス（例: ["implementation/01-add-model.md"]）
---

# <タスクタイトル>

<本文>
```

## ステータスの定義

| ステータス | 意味 |
|---|---|
| `not-started` | 未着手 |
| `in-progress` | 作業中 |
| `done` | 作業完了・検証待ち |
| `verified` | 検証完了 |

## 品質チェックとの連携

- ユーザーストーリーの品質チェック（`user-story-quality-check` skill）に合格した場合のみ、`user-story.md` のステータスを `verified` に更新する
- チェックで問題を検出した場合はステータスを `not-started` のままとし、問題点をユーザーストーリーファイルの末尾に記録する
- 確認結果の構造化コメントは `user-story.md` の末尾に追記する

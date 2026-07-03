---
name: issue-workflow
description: '本リポジトリの GitHub issue 管理プロセス。4 カテゴリ（user-story / design / implementation / manual-test）のラベル運用、gh CLI による sub-issue 作成手順、確認済みラベルの統制、issue のクローズ・reopen 規則を定義する。WHEN: "issue を作成", "sub-issue を作って", "ラベルを付与", "issue をクローズ/reopen", issue 管理プロセスに従った作業を行うとき。'
---

# Issue 管理ワークフロー

## Issue カテゴリ

Issue は以下の 4 カテゴリで管理し、対応するラベルを付与する。

| カテゴリ | ラベル | 説明 |
|---|---|---|
| ユーザーストーリー | `user-story` | ユーザー視点の要件記述 |
| 設計 | `design` | 要件・基本設計・アーキテクチャの追加・変更 |
| 実装 | `implementation` | 詳細設計・コーディング・自動テスト |
| 手動テスト | `manual-test` | 自動テストでカバーできないテスト |

## Issue の親子関係

- 設計・実装・手動テストカテゴリの issue は、必ずユーザーストーリー issue の **sub-issue（GitHub ネイティブ Sub-issues 機能）** とする
- ユーザーストーリーが最上位の親 issue となる
- sub-issue のタイトルには親ユーザーストーリーとの関連がわかる記述を含める

## 作業開始条件

ユーザーストーリー issue およびその sub-issue に対する作業を開始するには、親ユーザーストーリーに **「確認済み」ラベル** が付与されていること。

## ステータスラベルの統制

| ラベル | 付与条件 |
|---|---|
| `確認済み` | `user-story-quality-check` skill のチェックフローをすべて完遂し、合格した場合に付与 |
| `要確認` | 同チェックフローで問題を検出した場合に付与 |

- 「確認済み」ラベルは、`user-story-quality-check` skill のチェックフロー（ステップ 1〜6）を **すべて完遂し、構造化コメントを証跡として issue に記録した場合にのみ** 付与できる
- チェックフローを完遂していない状態で「確認済み」ラベルを付与してはならない
- ユーザーが「確認済み」ラベルを付与することはないが、引きはがすことはできる

## GitHub 操作の手順

GitHub の操作はすべて `gh` CLI で行う。MCP Server は使用しない。

- issue の取得: `gh issue view <番号> --json title,body,labels,comments`
- コメント投稿: `gh issue comment <番号> --body "<コメント>"`
- ラベル付与: `gh issue edit <番号> --add-label "<ラベル>"`
- ラベル除去: `gh issue edit <番号> --remove-label "<ラベル>"`
- issue 作成: `gh issue create --title "<タイトル>" --body "<本文>" --label "<ラベル>"`

### Sub-issue の作成手順

`gh issue create` には `--parent` フラグが存在しないため、2 ステップで作成する:

1. 子 issue を作成し、出力される issue URL を控える:
   ```bash
   gh issue create --title "<タイトル>" --body "<本文>" --label "<ラベル>"
   ```
2. 親 issue の node ID を取得し、GraphQL API で親子関係を設定する:
   ```bash
   PARENT_ID=$(gh issue view <親issue番号> --json id -q .id)
   CHILD_URL="<手順1で取得したissue URL>"
   gh api graphql -f query='mutation($parentId: ID!, $childUrl: String!) { addSubIssue(input: {issueId: $parentId, subIssueUrl: $childUrl}) { issue { id } subIssue { id } } }' -f parentId="$PARENT_ID" -f childUrl="$CHILD_URL"
   ```

## Issue のクローズとリオープン

- ユーザーストーリーのクローズ前に、関連するすべての成果物（コード、ドキュメント、README 等）が更新されていることを確認する
- クローズ後に対応漏れが発覚した場合は、**新規 issue を作成するのではなく、元の issue を reopen** する
- reopen した issue には、漏れた作業内容を本文またはコメントに追記する
- 追加修正の PR は、reopen した元の issue に `Closes #<issue番号>` で紐づける
- ブランチ命名も元の issue 番号を使用する（例: `feature/#<元issue番号>-<修正内容>`）

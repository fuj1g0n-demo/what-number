---
applyTo: "**"
---

# スクリーンショット撮影スキル

Playwright CLI を使用して、指定された URL のスクリーンショットを撮影する。

## 前提条件

- `npx playwright` が使用可能であること

## 手順

### ステップ 1: アプリケーション起動確認

スクリーンショット撮影前に、対象 URL にアクセスできるか確認する。以下のコマンドで疎通チェックを行う:

```bash
curl -s -o /dev/null -w "%{http_code}" http://localhost:8080
```

- HTTP ステータス 200 が返れば起動済み → ステップ 2 へ進む
- 接続エラーまたは 200 以外の場合 → アプリを起動する:
  1. アプリケーションを起動（バックグラウンドターミナル）:
     ```bash
     ./gradlew :app:bootRun
     ```
  2. アプリケーションが応答するまで待機:
     ```bash
     for i in $(seq 1 30); do curl -s -o /dev/null -w "%{http_code}" http://localhost:8080 | grep -q 200 && echo "Ready" && break; echo "Waiting... ($i)"; sleep 2; done
     ```

### ステップ 2: スクリーンショット撮影

1. ユーザーが URL を指定しない場合、デフォルトは `http://localhost:8080` を使用する
2. 保存先を指定されない場合、`build/screenshots/` ディレクトリに保存する
3. ファイル名を指定されない場合、`screenshot-<タイムスタンプ>.png` とする
4. 以下のコマンドをターミナルで実行する:

```bash
mkdir -p build/screenshots
npx playwright screenshot --full-page <URL> <保存先パス>
```

## オプション対応

ユーザーが以下を指示した場合、対応するオプションを付与する:

| ユーザーの指示 | CLI オプション |
|---|---|
| 特定の要素が表示されてから撮って | `--wait-for-selector "<セレクタ>"` |
| ○秒待ってから撮って | `--wait-for-timeout <ミリ秒>` |
| ページ全体ではなく表示部分だけ | `--full-page` を外す |
| モバイル表示で撮って | `--device "iPhone 15"` |
| ダークモードで撮って | `--color-scheme dark` |

## 実行例

```bash
# 基本（フルページ）
npx playwright screenshot --full-page http://localhost:8080 build/screenshots/screenshot-20260319-120000.png

# モバイルデバイス表示
npx playwright screenshot --full-page --device "iPhone 15" http://localhost:8080 build/screenshots/screenshot-mobile.png

# 特定要素の読み込みを待機
npx playwright screenshot --full-page --wait-for-selector ".quiz-list" http://localhost:8080 build/screenshots/screenshot-quiz.png
```

## エラー時の対応

- コマンドが接続エラーを返した場合、「アプリケーションが起動していない可能性があります。`./gradlew :app:bootRun` でアプリケーションを起動してください。」とユーザーに伝える
- Playwright がインストールされていない場合、`npx playwright install chromium` の実行を提案する

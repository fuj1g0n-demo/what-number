# IDE フォーマッタ分析

> IDE 間のフォーマット不一致の原因と、google-java-format が唯一の正解である根拠を整理する。

## 1. 問題: IDE 間のフォーマット不一致

各 IDE はそれぞれ **異なるフォーマッタエンジン** を持ち、同じ「Google Style 設定」を使っても出力が一致しない。

| IDE | エンジン | 行折り返し方式 | google-java-format との一致度 |
|---|---|---|---|
| Eclipse | Eclipse JDT Formatter | 列制限に達したら折り返し | ~90-95% |
| IntelliJ IDEA | IntelliJ 独自エンジン | アライメント (括弧位置揃え) | ~90-95% |
| VS Code | 拡張機能依存 | 拡張機能による | 拡張機能による |

**不一致の根本原因**: 行折り返しのアルゴリズムが各エンジンで根本的に異なるため、300+ パラメータを調整しても完全一致は不可能。

## 2. google-java-format が唯一の正解である根拠

Google Java Style Guide Section 3:

> "code that conforms to google-java-format's output is said to conform to the style guide."

つまり **google-java-format の出力が Style Guide のフォーマット定義そのもの** であり、代替エンジンでは定義上完全準拠を達成できない。

| 代替エンジン | 完全一致不可の理由 |
|---|---|
| Eclipse JDT Formatter | ヒューリスティックベースの行折り返し |
| IntelliJ Formatter | アライメント方式が異なる。Spotless からの呼び出し非対応 |
| Palantir Java Format | google-java-format フォークだが独自スタイルに固定 |

## 3. 「IDE に合わせる」戦略が機能しない理由

| 問題 | 説明 |
|---|---|
| どの IDE に合わせるのか | Eclipse に合わせれば IntelliJ と不一致。逆も然り |
| IDE バージョン差異 | IDE バージョンアップでフォーマッタの挙動が変わりうる |
| 全員同じ IDE を強制するのか | 「IDE に依存しない」が本来の要件 |
| Style Guide 準拠を名乗れない | Eclipse/IntelliJ の出力は google-java-format と一致しないため |

**結論**: 統一の方向は **「IDE をツールに合わせる」** が正解であり、多層防御アーキテクチャで差異を吸収する。

## 4. IDE フォーマットアクションの 2 方式

| 観点 | 方式 A: google-java-format プラグイン | 方式 B: Spotless 呼び出し |
|---|---|---|
| 設定の一元性 | IDE ごとに個別設定 | ビルドファイルに一元化 |
| CI との一致 | ほぼ一致 (微細な差異あり) | **完全一致** |
| Spotless 追加設定の反映 | ❌ (ヘッダ等が非反映) | ✅ 全設定が反映 |
| 速度 | 即時 (~100ms) | 1-3 秒 (Gradle Daemon) |
| セットアップ難度 | 低い | やや高い |

**判断基準**: Spotless にファイルヘッダ等の追加設定がある場合 → 方式 B 推奨。google-java-format のみの場合 → 方式 A + 後段で補完。

> 本プロジェクトでは `licenseHeader()` でファイルヘッダを管理するため、**方式 B が有効**。方式 A 単体ではヘッダが挿入されない。

# syntax=docker/dockerfile:1

# ---------------------------------------------------------------------------
# Build stage: Spring Boot 実行可能 JAR をビルドする
# （Gradle バージョンは gradle-wrapper.properties を単一正本として wrapper が解決）
# ---------------------------------------------------------------------------
FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace

# Gradle wrapper とビルドスクリプトを先にコピー（依存解決のレイヤーキャッシュを効かせる）
COPY gradlew ./
COPY gradle ./gradle
COPY settings.gradle.kts build.gradle.kts ./
COPY app/build.gradle.kts app/build.gradle.kts
COPY system-test/build.gradle.kts system-test/build.gradle.kts
RUN chmod +x gradlew

# アプリケーションソースをコピー
COPY app/src app/src

# bootJar をビルド（テストは CI 側で実行するためここではスキップ）
RUN ./gradlew --no-daemon :app:bootJar \
 && cp "$(ls app/build/libs/*.jar | grep -v 'plain')" /app.jar

# ---------------------------------------------------------------------------
# Runtime stage: 軽量な JRE で JAR を実行する
# ---------------------------------------------------------------------------
# [SECURITY DEMO ONLY] 後続の apt スナップショット手順（jammy 固定）と整合させる
# ため、ベースイメージを明示的に jammy 系へピン留めしている。デモ後は
# `eclipse-temurin:21-jre` に戻すこと。
FROM eclipse-temurin:21-jre-jammy AS runtime
WORKDIR /app

# ===== [CODE-TO-CLOUD MAPPING 検証] OCI ラベル埋め込み =======================
# 検証目的: イメージ manifest に OCI annotations / Docker LABEL を埋め込んだ場合、
#           Defender for Cloud CLI が出力する SARIF がこの情報を拾い出すか
#           （code-to-cloud のソース情報を SARIF へ反映できるか）を実証する。
#           固有プローブ値 sarif-label-probe-20260624 を SARIF 内で検索して判定する。
LABEL org.opencontainers.image.source="https://github.com/fuj1g0n-demo-01/what-number" \
      org.opencontainers.image.revision="demo-security-vuln-xss-and-vulnerable-dep" \
      org.opencontainers.image.url="https://github.com/fuj1g0n-demo-01/what-number" \
      org.opencontainers.image.title="what-number" \
      com.microsoft.defender.dockerfile.path="Dockerfile" \
      com.example.codetocloud.test="sarif-label-probe-20260624"
# ===========================================================================

# ===== [SECURITY DEMO ONLY — 意図的に脆弱] ==================================
# Microsoft Defender Vulnerability Management (MDVM) / Defender for Cloud による
# コンテナイメージスキャンの検出デモ用に、既知の脆弱性を多数含む「古いバージョン」の
# システムツール（curl / wget / git）とその依存ライブラリを意図的に導入する。
#
# 仕組み: Ubuntu 公式のスナップショットアーカイブ（2022-06-01 時点の jammy）の pool から
#         GA 当時の未パッチな .deb を URL 直接指定でダウンロードし、dpkg で登録する。
#         apt の候補解決・依存解決は使わない（最新ベースの新しいライブラリと古いパッケージで
#         依存バージョンが衝突して "held broken packages" になる／パッチ版が候補に選ばれて
#         "Can't find a source" になるのを避けるため）。dpkg --force-all で依存不一致を無視して
#         登録するだけ（実行はしない。スキャンは dpkg データベースを読むため検出される）。
#         例) curl 7.81.0-1 / libcurl4 7.81.0-1 / git 2.34.1 / wget 1.21.2 など多数の CVE を含む。
#
# 注意: これはアプリ（Java）レイヤーではなく OS パッケージ層の脆弱性を再現している。
#       本番では絶対に使用しないこと。デモ後はこのブロックとベースの jammy 固定を
#       併せて必ず削除すること。
# ---------------------------------------------------------------------------
RUN set -eux; \
    # ダウンローダ(wget)と TLS 検証用 ca-certificates を現行リポジトリから確保（snapshot は https にリダイレクト）
    apt-get update; \
    apt-get install -y --no-install-recommends ca-certificates wget; \
    # 2022-06-01 スナップショットの pool から古い .deb を URL 直接ダウンロード
    SNAP="http://snapshot.ubuntu.com/ubuntu/20220601T000000Z"; \
    mkdir -p /tmp/vulndebs; cd /tmp/vulndebs; \
    wget --tries=3 --timeout=30 -q \
      "$SNAP/pool/main/c/curl/curl_7.81.0-1_amd64.deb" \
      "$SNAP/pool/main/c/curl/libcurl4_7.81.0-1_amd64.deb" \
      "$SNAP/pool/main/g/git/git_2.34.1-1ubuntu1_amd64.deb" \
      "$SNAP/pool/main/g/git/git-man_2.34.1-1ubuntu1_all.deb" \
      "$SNAP/pool/main/w/wget/wget_1.21.2-2ubuntu1_amd64.deb"; \
    # 依存解決を行わず dpkg で登録する（実行はしないがスキャンに検出される）
    dpkg -i --force-all ./*.deb || true; \
    cd /; rm -rf /tmp/vulndebs /var/lib/apt/lists/*
# ===========================================================================

# 非 root ユーザーで実行する
RUN groupadd --system spring && useradd --system --gid spring spring

COPY --from=build --chown=spring:spring /app.jar app.jar
USER spring:spring

EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]

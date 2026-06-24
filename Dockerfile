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

# ===== [SECURITY DEMO ONLY — 意図的に脆弱] ==================================
# Microsoft Defender Vulnerability Management (MDVM) / Defender for Cloud による
# コンテナイメージスキャンの検出デモ用に、既知の脆弱性を多数含む「古いバージョン」の
# システムツール（curl / wget / git）とその依存ライブラリを意図的に導入する。
#
# 仕組み: Ubuntu 公式のスナップショットアーカイブ（2022-06-01 時点の jammy）を
#         apt ソースに固定し、GA 当時の未パッチなパッケージを取得する。
#         例) curl 7.81.0-1 / git 1:2.34.1-1ubuntu1 など多数の CVE を含む。
#
# 注意: これはアプリ（Java）レイヤーではなく OS パッケージ層の脆弱性を再現している。
#       本番では絶対に使用しないこと。デモ後はこのブロックとベースの jammy 固定を
#       併せて必ず削除すること。
# ---------------------------------------------------------------------------
RUN set -eux; \
    # snapshot.ubuntu.com は https にリダイレクトするため TLS 検証用 ca-certificates を先に確保
    apt-get update; \
    apt-get install -y --no-install-recommends ca-certificates; \
    # apt ソースを 2022 年時点の Ubuntu スナップショットに固定（古い脆弱なパッケージを取得）
    printf 'deb [trusted=yes check-valid-until=no] http://snapshot.ubuntu.com/ubuntu/20220601T000000Z jammy main universe\n' > /etc/apt/sources.list; \
    rm -f /etc/apt/sources.list.d/*; \
    apt-get update; \
    apt-get install -y --no-install-recommends --allow-downgrades curl wget git; \
    rm -rf /var/lib/apt/lists/*
# ===========================================================================

# 非 root ユーザーで実行する
RUN groupadd --system spring && useradd --system --gid spring spring

COPY --from=build --chown=spring:spring /app.jar app.jar
USER spring:spring

EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]

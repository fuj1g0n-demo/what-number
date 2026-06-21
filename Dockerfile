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
FROM eclipse-temurin:21-jre AS runtime
WORKDIR /app

# 非 root ユーザーで実行する
RUN groupadd --system spring && useradd --system --gid spring spring

COPY --from=build --chown=spring:spring /app.jar app.jar
USER spring:spring

EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]

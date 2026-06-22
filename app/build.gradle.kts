plugins {
    java
    jacoco
    `maven-publish`
    id("org.springframework.boot") version "3.4.3"
    id("io.spring.dependency-management") version "1.1.7"
}

// group / version はリポジトリルートの gradle.properties を単一正本とする
// （全サブプロジェクトへ適用される）。リリースは release.yml が version を書き換える。

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // Spring Boot
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-thymeleaf")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // DevTools
    developmentOnly("org.springframework.boot:spring-boot-devtools")

    // Unit Test
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

// ---------------------------------------------------------------------------
// Integration Test ソースセット
// ---------------------------------------------------------------------------
sourceSets {
    create("integrationTest") {
        compileClasspath += sourceSets.main.get().output
        runtimeClasspath += sourceSets.main.get().output
    }
}

val integrationTestImplementation by configurations.getting {
    extendsFrom(configurations.testImplementation.get())
}
val integrationTestRuntimeOnly by configurations.getting {
    extendsFrom(configurations.testRuntimeOnly.get())
}

tasks.register<Test>("integrationTest") {
    description = "Runs integration tests."
    group = "verification"
    testClassesDirs = sourceSets["integrationTest"].output.classesDirs
    classpath = sourceSets["integrationTest"].runtimeClasspath
    shouldRunAfter(tasks.test)
    useJUnitPlatform()
}

// ---------------------------------------------------------------------------
// テスト共通設定
// ---------------------------------------------------------------------------
tasks.withType<Test> {
    useJUnitPlatform()
}

// ---------------------------------------------------------------------------
// JaCoCo カバレッジ設定
// ---------------------------------------------------------------------------

// Unit Test のカバレッジ
tasks.test {
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(false)
    }
}

// Integration Test のカバレッジ
tasks.named<Test>("integrationTest") {
    finalizedBy("jacocoIntegrationTestReport")
}

tasks.register<JacocoReport>("jacocoIntegrationTestReport") {
    dependsOn("integrationTest")
    sourceSets(sourceSets.main.get())
    executionData(layout.buildDirectory.file("jacoco/integrationTest.exec"))
    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(false)
    }
}

// 全テスト統合カバレッジレポート
tasks.register<JacocoReport>("jacocoFullReport") {
    description = "Generates a combined code coverage report for unit + integration tests."
    group = "verification"
    dependsOn(tasks.test, tasks.named("integrationTest"))
    sourceSets(sourceSets.main.get())
    executionData(
        layout.buildDirectory.file("jacoco/test.exec"),
        layout.buildDirectory.file("jacoco/integrationTest.exec")
    )
    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(false)
    }
}

tasks.named("check") {
    dependsOn(tasks.named("integrationTest"))
}

// ---------------------------------------------------------------------------
// Azure Artifacts (Maven フィード) への発行設定
//   - 実行可能 bootJar を成果物として発行する
//   - バージョンは CI から -PartifactVersion で上書き可能（既定は project.version）
//   - 接続情報は環境変数で注入（CI 専用。ローカルの通常ビルドには影響しない）
//       AZURE_ARTIFACTS_FEED_URL : フィードの Maven エンドポイント URL
//       AZURE_ARTIFACTS_USER     : 任意のユーザー名（PAT 使用時は値は問われない）
//       AZURE_ARTIFACTS_PAT      : Azure DevOps Personal Access Token
// ---------------------------------------------------------------------------
publishing {
    publications {
        create<MavenPublication>("bootApp") {
            groupId = "com.example"
            artifactId = "what-number"
            version = (project.findProperty("artifactVersion") as String?) ?: project.version.toString()
            artifact(tasks.named("bootJar"))
        }
    }
    repositories {
        maven {
            name = "azureArtifacts"
            url = uri(providers.environmentVariable("AZURE_ARTIFACTS_FEED_URL").orElse("").get())
            credentials {
                username = providers.environmentVariable("AZURE_ARTIFACTS_USER").orElse("AzureDevOps").get()
                password = providers.environmentVariable("AZURE_ARTIFACTS_PAT").orElse("").get()
            }
        }
        // GitHub Packages (Maven レジストリ)
        //   GITHUB_PACKAGES_URL : https://maven.pkg.github.com/<owner>/<repo>
        //   GITHUB_ACTOR        : 実行ユーザー
        //   GITHUB_TOKEN        : packages:write 権限を持つトークン
        maven {
            name = "githubPackages"
            url = uri(providers.environmentVariable("GITHUB_PACKAGES_URL").orElse("").get())
            credentials {
                username = providers.environmentVariable("GITHUB_ACTOR").orElse("").get()
                password = providers.environmentVariable("GITHUB_TOKEN").orElse("").get()
            }
        }
    }
}

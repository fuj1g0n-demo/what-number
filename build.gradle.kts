plugins {
    java
    id("org.springframework.boot") version "3.4.3"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "com.example"
version = "0.0.1-SNAPSHOT"

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
// System Test ソースセット
// ---------------------------------------------------------------------------
sourceSets {
    create("systemTest") {
        compileClasspath += sourceSets.main.get().output
        runtimeClasspath += sourceSets.main.get().output
    }
}

val systemTestImplementation by configurations.getting {
    extendsFrom(configurations.testImplementation.get())
}
val systemTestRuntimeOnly by configurations.getting {
    extendsFrom(configurations.testRuntimeOnly.get())
}

dependencies {
    "systemTestImplementation"("com.microsoft.playwright:playwright:1.49.0")
}

tasks.register<Test>("systemTest") {
    description = "Runs system tests with Playwright."
    group = "verification"
    testClassesDirs = sourceSets["systemTest"].output.classesDirs
    classpath = sourceSets["systemTest"].runtimeClasspath
    shouldRunAfter(tasks.named("integrationTest"))
    useJUnitPlatform()
}

// ---------------------------------------------------------------------------
// テスト共通設定
// ---------------------------------------------------------------------------
tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.named("check") {
    dependsOn(tasks.named("integrationTest"))
}

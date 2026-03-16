plugins {
    java
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.4")
    testImplementation("com.microsoft.playwright:playwright:1.49.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
    systemProperty("test.base-url",
        System.getProperty("test.base-url", "http://localhost:8080"))
    environment("PLAYWRIGHT_BROWSERS_TO_INSTALL", "chromium")
    environment("PLAYWRIGHT_SKIP_VALIDATE_HOST_REQUIREMENTS", "true")
}

package com.example.quiz;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * System Test: Playwright を使用したブラウザ経由の E2E テスト。
 * <p>
 * 事前にアプリケーションを起動しておく必要がある。
 * デフォルトでは http://localhost:8080 に接続する。
 * テスト結果のスクリーンショットは system-test/build/screenshots/ に保存される。
 */
class QuizSystemTest {

    private static final String BASE_URL = System.getProperty("test.base-url", "http://localhost:8080");
    private static final Path SCREENSHOT_DIR = Paths.get("build", "screenshots");

    private static Playwright playwright;
    private static Browser browser;
    private Page page;

    @BeforeAll
    static void setUp() {
        SCREENSHOT_DIR.toFile().mkdirs();
        playwright = Playwright.create();
        browser = playwright.chromium().launch(
                new BrowserType.LaunchOptions().setHeadless(true)
        );
    }

    @AfterAll
    static void tearDown() {
        if (browser != null) {
            browser.close();
        }
        if (playwright != null) {
            playwright.close();
        }
    }

    @BeforeEach
    void openPage() {
        page = browser.newPage();
    }

    @AfterEach
    void closePage(TestInfo testInfo) {
        if (page != null) {
            Path path = SCREENSHOT_DIR.resolve(testInfo.getDisplayName() + ".png");
            page.screenshot(new Page.ScreenshotOptions()
                    .setPath(path)
                    .setFullPage(true));
            page.close();
        }
    }

    @Test
    void topPage_displaysQuizList() {
        page.navigate(BASE_URL);
        String title = page.title();
        assertTrue(title.contains("この数字、なんの数字？"),
                "トップページのタイトルに「この数字、なんの数字？」が含まれること");
    }
}

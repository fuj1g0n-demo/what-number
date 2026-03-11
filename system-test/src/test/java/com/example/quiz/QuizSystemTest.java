package com.example.quiz;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * System Test: Playwright を使用したブラウザ経由の E2E テスト。
 * <p>
 * 事前にアプリケーションを起動しておく必要がある。
 * デフォルトでは http://localhost:8080 に接続する。
 */
class QuizSystemTest {

    private static final String BASE_URL = System.getProperty("test.base-url", "http://localhost:8080");

    private static Playwright playwright;
    private static Browser browser;

    @BeforeAll
    static void setUp() {
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

    @Test
    void topPage_displaysQuizList() {
        try (Page page = browser.newPage()) {
            page.navigate(BASE_URL);
            String title = page.title();
            assertTrue(title.contains("この数字、なんの数字？"),
                    "トップページのタイトルに「この数字、なんの数字？」が含まれること");
        }
    }
}

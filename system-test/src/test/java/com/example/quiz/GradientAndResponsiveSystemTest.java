package com.example.quiz;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * System Test: グラデーション背景およびレスポンシブデザインの検証。
 * <p>
 * Playwright を使用して、body の背景にグラデーションが適用されていること、
 * navbar に navbar-gradient クラスが使用されていること、
 * 各画面幅で水平スクロールが発生しないことを確認する。
 * <p>
 * 事前にアプリケーションを起動しておく必要がある。
 * デフォルトでは http://localhost:8080 に接続する。
 */
class GradientAndResponsiveSystemTest {

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

    // -----------------------------------------------------------------------
    // グラデーション背景
    // -----------------------------------------------------------------------

    @Test
    void topPage_hasGradientBackground() {
        try (Page page = browser.newPage()) {
            page.navigate(BASE_URL);
            assertBodyHasGradient(page);
        }
    }

    @Test
    void quizPage_hasGradientBackground() {
        try (Page page = browser.newPage()) {
            page.navigate(BASE_URL + "/quiz/1");
            assertBodyHasGradient(page);
        }
    }

    @Test
    void resultPage_hasGradientBackground() {
        try (Page page = browser.newPage()) {
            // クイズに回答して結果画面へ遷移
            page.navigate(BASE_URL + "/quiz/1");
            Locator answerButton = page.locator("button[name='selectedAnswer']").first();
            answerButton.click();
            page.waitForURL("**/quiz/1/answer**");
            assertBodyHasGradient(page);
        }
    }

    @Test
    void navbar_usesGradientClass() {
        try (Page page = browser.newPage()) {
            page.navigate(BASE_URL);
            Locator navbar = page.locator("nav.navbar-gradient");
            assertTrue(navbar.count() > 0,
                    "ナビゲーションバーに navbar-gradient クラスが適用されていること");
        }
    }

    // -----------------------------------------------------------------------
    // レスポンシブデザイン — 水平スクロールが発生しないこと
    // -----------------------------------------------------------------------

    @Test
    void mobile_noHorizontalScroll() {
        assertNoHorizontalScroll(375, 667);
    }

    @Test
    void tablet_noHorizontalScroll() {
        assertNoHorizontalScroll(768, 1024);
    }

    @Test
    void desktop_noHorizontalScroll() {
        assertNoHorizontalScroll(1280, 800);
    }

    // -----------------------------------------------------------------------
    // ヘルパーメソッド
    // -----------------------------------------------------------------------

    private void assertBodyHasGradient(Page page) {
        String bg = (String) page.evaluate(
                "() => window.getComputedStyle(document.body).backgroundImage");
        assertTrue(bg != null && bg.contains("gradient"),
                "body の backgroundImage にグラデーションが含まれること: " + bg);
    }

    private void assertNoHorizontalScroll(int width, int height) {
        String[] paths = {"/", "/quiz/1"};
        try (BrowserContext context = browser.newContext(
                new Browser.NewContextOptions().setViewportSize(width, height))) {
            Page page = context.newPage();
            for (String path : paths) {
                page.navigate(BASE_URL + path);
                boolean hasHorizontalScroll = (boolean) page.evaluate(
                        "() => document.documentElement.scrollWidth > document.documentElement.clientWidth");
                assertFalse(hasHorizontalScroll,
                        "画面幅 " + width + "px, パス " + path + " で水平スクロールが発生しないこと");
            }
        }
    }
}

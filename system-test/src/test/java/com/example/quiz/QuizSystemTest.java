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

import static org.junit.jupiter.api.Assertions.assertEquals;
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
    private static final String QUIZ_LIST_ITEM_SELECTOR = "a.list-group-item";
    private static final String CHOICE_BUTTON_SELECTOR = "form button[type='submit']";
    private static final int EXPECTED_QUIZ_COUNT = 3;
    private static final int EXPECTED_CHOICE_COUNT = 4;

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
        var quizLinks = page.locator(QUIZ_LIST_ITEM_SELECTOR);

        assertTrue(page.title().contains("この数字、なんの数字？"),
                "トップページのタイトルに「この数字、なんの数字？」が含まれること");
        assertTrue(page.locator("h1").textContent().contains("クイズ一覧"),
                "一覧画面にクイズ一覧見出しが表示されること");
        assertEquals(EXPECTED_QUIZ_COUNT, quizLinks.count(),
                "クイズ一覧に3件のクイズが表示されること");
        assertTrue(quizLinks.nth(0).textContent().contains("数字: 3,776"),
                "1件目のクイズに数字 3,776 が表示されること");
        assertTrue(quizLinks.nth(0).getAttribute("href").endsWith("/quiz/1"),
                "1件目のクイズから /quiz/1 に遷移できること");
        assertTrue(quizLinks.nth(1).textContent().contains("数字: 47"),
                "2件目のクイズに数字 47 が表示されること");
        assertTrue(quizLinks.nth(1).getAttribute("href").endsWith("/quiz/2"),
                "2件目のクイズから /quiz/2 に遷移できること");
        assertTrue(quizLinks.nth(2).textContent().contains("数字: 634"),
                "3件目のクイズに数字 634 が表示されること");
        assertTrue(quizLinks.nth(2).getAttribute("href").endsWith("/quiz/3"),
                "3件目のクイズから /quiz/3 に遷移できること");
    }

    @Test
    void quizPage_displaysQuestionAndChoices() {
        page.navigate(BASE_URL + "/quiz/1");
        var choiceButtons = page.locator(CHOICE_BUTTON_SELECTOR);

        assertTrue(page.title().contains("Q1 - この数字、なんの数字？"),
                "出題画面のタイトルにクイズ番号とアプリ名が表示されること");
        assertEquals("3,776", page.locator("h2.display-3").textContent().trim(),
                "出題画面に数字 3,776 が表示されること");
        assertEquals("この数字、なんの数字？", page.locator("p.lead").textContent().trim(),
                "出題画面に問題文が表示されること");
        assertEquals(EXPECTED_CHOICE_COUNT, choiceButtons.count(),
                "出題画面に4つの選択肢ボタンが表示されること");
        assertEquals("東京スカイツリーの高さ（メートル）", choiceButtons.nth(0).textContent().trim(),
                "1つ目の選択肢が表示されること");
        assertEquals("富士山の標高（メートル）", choiceButtons.nth(1).textContent().trim(),
                "2つ目の選択肢が表示されること");
        assertEquals("日本の島の数", choiceButtons.nth(2).textContent().trim(),
                "3つ目の選択肢が表示されること");
        assertEquals("東京タワーの高さ（メートル）", choiceButtons.nth(3).textContent().trim(),
                "4つ目の選択肢が表示されること");
    }
}

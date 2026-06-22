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
import java.util.List;

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
    private static final String EXPECTED_QUESTION = "この数字、なんの数字？";
    private static final List<String> EXPECTED_QUIZ_NUMBERS = List.of("3,776", "47", "634");
    private static final List<String> EXPECTED_QUIZ_PATHS = List.of("/quiz/1", "/quiz/2", "/quiz/3");
    private static final List<String> EXPECTED_CHOICES = List.of(
            "東京スカイツリーの高さ（メートル）",
            "富士山の標高（メートル）",
            "日本の島の数",
            "東京タワーの高さ（メートル）"
    );

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
        assertEquals(EXPECTED_QUIZ_NUMBERS.size(), quizLinks.count(),
                "クイズ一覧に3件のクイズが表示されること");
        for (int i = 0; i < EXPECTED_QUIZ_NUMBERS.size(); i++) {
            assertTrue(quizLinks.nth(i).textContent().contains("数字: " + EXPECTED_QUIZ_NUMBERS.get(i)),
                    (i + 1) + "件目のクイズに数字が表示されること");
            assertTrue(quizLinks.nth(i).getAttribute("href").endsWith(EXPECTED_QUIZ_PATHS.get(i)),
                    (i + 1) + "件目のクイズから対応する出題画面に遷移できること");
        }
    }

    @Test
    void quizPage_displaysQuestionAndChoices() {
        page.navigate(BASE_URL + "/quiz/1");
        var choiceButtons = page.locator(CHOICE_BUTTON_SELECTOR);

        assertTrue(page.title().contains("Q1 - " + EXPECTED_QUESTION),
                "出題画面のタイトルにクイズ番号とアプリ名が表示されること");
        assertEquals(EXPECTED_QUIZ_NUMBERS.getFirst(), page.locator("h2.display-3").textContent().trim(),
                "出題画面に数字 3,776 が表示されること");
        assertEquals(EXPECTED_QUESTION, page.locator("p.lead").textContent().trim(),
                "出題画面に問題文が表示されること");
        assertEquals(EXPECTED_CHOICES.size(), choiceButtons.count(),
                "出題画面に4つの選択肢ボタンが表示されること");
        for (int i = 0; i < EXPECTED_CHOICES.size(); i++) {
            assertEquals(EXPECTED_CHOICES.get(i), choiceButtons.nth(i).textContent().trim(),
                    (i + 1) + "つ目の選択肢が表示されること");
        }
    }
}

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
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
    private static final String PAGE_HEADING_SELECTOR = "h1";
    private static final String QUIZ_LIST_ITEM_SELECTOR = "a.list-group-item";
    private static final String CHOICE_BUTTON_SELECTOR = "form button[type='submit']";
    private static final String QUIZ_NUMBER_DISPLAY_SELECTOR = "h2.display-3";
    private static final String QUESTION_TEXT_SELECTOR = "p.lead";
    private static final String QUIZ_NUMBER_LABEL_PREFIX = "数字: ";
    private static final String EXPECTED_QUESTION = "この数字、なんの数字？";
    private static final List<QuizListItemExpectation> EXPECTED_QUIZZES = List.of(
            new QuizListItemExpectation(1, "3,776", "/quiz/1", List.of(
                    "東京スカイツリーの高さ（メートル）",
                    "富士山の標高（メートル）",
                    "日本の島の数",
                    "東京タワーの高さ（メートル）"
            )),
            new QuizListItemExpectation(2, "47", "/quiz/2", List.of()),
            new QuizListItemExpectation(3, "634", "/quiz/3", List.of())
    );
    private static final Map<Integer, QuizListItemExpectation> EXPECTED_QUIZZES_BY_ID = EXPECTED_QUIZZES.stream()
            .collect(Collectors.toUnmodifiableMap(QuizListItemExpectation::id, Function.identity()));
    private static final int TARGET_QUIZ_ID = 1;

    private static Playwright playwright;
    private static Browser browser;
    private Page page;

    private record QuizListItemExpectation(int id, String number, String path, List<String> choices) {
    }

    private String requiredTextContent(com.microsoft.playwright.Locator locator, String message) {
        String text = locator.textContent();
        assertNotNull(text, message);
        return text.trim();
    }

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
        assertTrue(requiredTextContent(page.locator(PAGE_HEADING_SELECTOR), "一覧見出しが取得できること").contains("クイズ一覧"),
                "一覧画面にクイズ一覧見出しが表示されること");
        assertEquals(EXPECTED_QUIZZES.size(), quizLinks.count(),
                "クイズ一覧に3件のクイズが表示されること");
        for (int i = 0; i < EXPECTED_QUIZZES.size(); i++) {
            var expectedQuiz = EXPECTED_QUIZZES.get(i);
            assertTrue(requiredTextContent(quizLinks.nth(i), (i + 1) + "件目のクイズ項目が取得できること")
                            .contains(QUIZ_NUMBER_LABEL_PREFIX + expectedQuiz.number()),
                    (i + 1) + "件目のクイズに数字が表示されること");
            assertTrue(quizLinks.nth(i).getAttribute("href").endsWith(expectedQuiz.path()),
                    (i + 1) + "件目のクイズから対応する出題画面に遷移できること");
        }
    }

    @Test
    void quizPage_displaysQuestionAndChoices() {
        var expectedQuiz = EXPECTED_QUIZZES_BY_ID.get(TARGET_QUIZ_ID);
        page.navigate(BASE_URL + "/quiz/" + TARGET_QUIZ_ID);
        var choiceButtons = page.locator(CHOICE_BUTTON_SELECTOR);

        assertTrue(page.title().contains("Q" + TARGET_QUIZ_ID + " - " + EXPECTED_QUESTION),
                "出題画面のタイトルにクイズ番号とアプリ名が表示されること");
        assertEquals(expectedQuiz.number(),
                requiredTextContent(page.locator(QUIZ_NUMBER_DISPLAY_SELECTOR), "出題画面の数字が取得できること"),
                "出題画面に数字 " + expectedQuiz.number() + " が表示されること");
        assertEquals(EXPECTED_QUESTION,
                requiredTextContent(page.locator(QUESTION_TEXT_SELECTOR), "出題画面の問題文が取得できること"),
                "出題画面に問題文が表示されること");
        assertEquals(expectedQuiz.choices().size(), choiceButtons.count(),
                "出題画面に4つの選択肢ボタンが表示されること");
        for (int i = 0; i < expectedQuiz.choices().size(); i++) {
            assertEquals(expectedQuiz.choices().get(i),
                    requiredTextContent(choiceButtons.nth(i), (i + 1) + "つ目の選択肢が取得できること"),
                    (i + 1) + "つ目の選択肢が表示されること");
        }
    }
}

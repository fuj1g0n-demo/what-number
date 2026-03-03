package com.example.quiz;

import com.microsoft.playwright.*;
import org.junit.jupiter.api.*;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

/**
 * System Test: Playwright を使用したブラウザ経由の E2E テスト。
 * <p>
 * 事前にアプリケーションを起動しておく必要がある。
 * デフォルトでは http://localhost:8080 に接続する。
 * スクリーンショットは build/screenshots/ に保存される。
 */
class QuizSystemTest {

    private static final String BASE_URL = System.getProperty("test.base-url", "http://localhost:8080");
    private static final Path SCREENSHOT_DIR = Paths.get("build", "screenshots");

    private static Playwright playwright;
    private static Browser browser;

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

    private void screenshot(Page page, String name) {
        page.screenshot(new Page.ScreenshotOptions()
                .setPath(SCREENSHOT_DIR.resolve(name + ".png"))
                .setFullPage(true));
    }

    /**
     * Scenario: トップページに 10 問のクイズが一覧表示される
     */
    @Test
    void scenario1_topPage_displays10Quizzes() {
        try (Page page = browser.newPage()) {
            // Given ユーザーがトップページにアクセスする
            page.navigate(BASE_URL);

            // Then 10 問のクイズが一覧に表示される
            String title = page.title();
            assertTrue(title.contains("この数字、なんの数字？"),
                    "トップページのタイトルに「この数字、なんの数字？」が含まれること");

            Locator quizItems = page.locator(".list-group-item");
            assertEquals(10, quizItems.count(),
                    "トップページに 10 問のクイズが一覧表示されること");

            // And 各クイズには問題番号と数字が表示される
            for (int i = 0; i < quizItems.count(); i++) {
                String text = quizItems.nth(i).textContent();
                assertTrue(text.contains("Q" + (i + 1)),
                        "クイズ " + (i + 1) + " に問題番号が表示されること");
                assertTrue(text.contains("数字:"),
                        "クイズ " + (i + 1) + " に数字が表示されること");
            }

            screenshot(page, "01_top_page_10quizzes");
        }
    }

    /**
     * Scenario: クイズに挑戦して正解する
     */
    @Test
    void scenario2_answerQuiz_correctly() {
        try (Page page = browser.newPage()) {
            // Given ユーザーがトップページにアクセスする
            page.navigate(BASE_URL);

            // When ユーザーが任意のクイズを選択する（Q1: Microsoft 創業年 1975）
            page.locator(".list-group-item").first().click();

            // Then クイズの数字と 4 つの選択肢が表示される
            page.waitForSelector(".display-3");
            String number = page.locator(".display-3").textContent();
            assertEquals("1975", number.trim(),
                    "クイズ1の数字「1975」が表示されること");

            Locator choices = page.locator("button[name='selectedAnswer']");
            assertEquals(4, choices.count(),
                    "4 つの選択肢が表示されること");

            screenshot(page, "02_quiz_page_q1");

            // When ユーザーが正しい選択肢を選んで回答する（answer=0: 最初の選択肢）
            choices.nth(0).click();

            // Then 正解であることが表示される
            page.waitForSelector(".alert-success");
            String successText = page.locator(".alert-success").textContent();
            assertTrue(successText.contains("正解"),
                    "正解であることが表示されること");

            // And Microsoft に関連する解説が表示される
            String explanation = page.locator(".alert-info").textContent();
            assertTrue(explanation.contains("Microsoft"),
                    "Microsoft に関連する解説が表示されること");

            screenshot(page, "03_result_correct_q1");
        }
    }

    /**
     * Scenario: クイズに挑戦して不正解になる
     */
    @Test
    void scenario3_answerQuiz_incorrectly() {
        try (Page page = browser.newPage()) {
            // Given ユーザーがトップページにアクセスする
            page.navigate(BASE_URL);

            // When ユーザーが任意のクイズを選択する（Q1）
            page.locator(".list-group-item").first().click();

            // Then クイズの数字と 4 つの選択肢が表示される
            page.waitForSelector(".display-3");
            Locator choices = page.locator("button[name='selectedAnswer']");
            assertEquals(4, choices.count());

            // When ユーザーが誤った選択肢を選んで回答する（answer=0 なので、3 を選択）
            choices.nth(3).click();

            // Then 不正解であることが表示される
            page.waitForSelector(".alert-danger");
            String failText = page.locator(".alert-danger").textContent();
            assertTrue(failText.contains("不正解"),
                    "不正解であることが表示されること");

            // And 正解と Microsoft に関連する解説が表示される
            String explanation = page.locator(".alert-info").textContent();
            assertTrue(explanation.contains("Microsoft"),
                    "Microsoft に関連する解説が表示されること");

            screenshot(page, "04_result_incorrect_q1");
        }
    }

    /**
     * Scenario: すべてのクイズが Microsoft に関連する内容である
     */
    @Test
    void scenario4_allQuizzes_areMicrosoftRelated() {
        try (Page page = browser.newPage()) {
            // Given ユーザーがトップページにアクセスする
            page.navigate(BASE_URL);

            // When ユーザーが各クイズに順番に回答する
            for (int quizId = 1; quizId <= 10; quizId++) {
                page.navigate(BASE_URL + "/quiz/" + quizId);
                page.waitForSelector(".display-3");

                // 最初の選択肢を選んで回答（正誤問わず解説を確認）
                Locator choices = page.locator("button[name='selectedAnswer']");
                assertEquals(4, choices.count(),
                        "Q" + quizId + ": 4 つの選択肢が表示されること");

                screenshot(page, String.format("05_quiz_q%02d", quizId));

                choices.first().click();

                // Then すべてのクイズの解説に Microsoft に関連する事実が含まれる
                page.waitForSelector(".alert-info");
                String explanation = page.locator(".alert-info").textContent();
                assertTrue(explanation.contains("Microsoft"),
                        "Q" + quizId + " の解説に Microsoft が含まれること");

                screenshot(page, String.format("06_result_q%02d", quizId));
            }
        }
    }
}

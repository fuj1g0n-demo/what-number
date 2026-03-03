package com.example.quiz.service;

import com.example.quiz.dataloader.QuizDataLoader;
import com.example.quiz.model.Quiz;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QuizServiceTest {

    @Mock
    private QuizDataLoader quizDataLoader;

    private QuizService quizService;

    private final List<Quiz> sampleQuizzes = List.of(
            new Quiz(1, "1975", "この数字、なんの数字？",
                    List.of("Microsoft が設立された年", "Windows 1.0 が発売された年", "MS-DOS が発表された年", "Xbox が発売された年"), 0,
                    "1975年はMicrosoftが設立された年です。"),
            new Quiz(2, "262", "この数字、なんの数字？",
                    List.of("Microsoft の保有特許数（万件）", "LinkedIn 買収額（億ドル）", "GitHub 買収額（億ドル）", "Azure のサービス数"), 1,
                    "262億ドルはMicrosoftがLinkedInを買収した際の金額です。"),
            new Quiz(3, "75", "この数字、なんの数字？",
                    List.of("Microsoft 365 の搭載アプリ数", "GitHub 買収額（億ドル）", "Azure のリージョン数", "Windows のメジャーバージョン総数"), 1,
                    "75億ドルはMicrosoftがGitHubを買収した際の金額です。"),
            new Quiz(4, "2001", "この数字、なんの数字？",
                    List.of("Windows XP が発売された年", "初代 Xbox が発売された年", "Microsoft Azure が開始された年", "Internet Explorer が誕生した年"), 1,
                    "2001年は初代Xboxが北米で発売された年です。"),
            new Quiz(5, "60", "この数字、なんの数字？",
                    List.of("Azure のリージョン数", "Microsoft のオフィス所在国数", "Windows のサポート言語数", "Microsoft 365 の月額料金（ドル）"), 0,
                    "60以上はAzureが世界中に展開するリージョンの数です。"),
            new Quiz(6, "1985", "この数字、なんの数字？",
                    List.of("MS-DOS が発表された年", "Microsoft が株式公開した年", "Windows 1.0 が発売された年", "Microsoft Office が発表された年"), 2,
                    "1985年はWindows 1.0が発売された年です。"),
            new Quiz(7, "100,000", "この数字、なんの数字？",
                    List.of("Azure のアクティブユーザー数", "GitHub の総リポジトリ数（万）", "Microsoft の従業員数（2024年時点の概数）", "Bing の1日あたりの検索回数（万）"), 2,
                    "約100,000人はMicrosoftのグローバル従業員数です。"),
            new Quiz(8, "3", "この数字、なんの数字？",
                    List.of("Azure が提供する AI サービスの数", "Windows 11 の主要エディション数", "Microsoft の創業者の人数", "Microsoft 365 に含まれる主要アプリの数"), 1,
                    "3はWindows 11の主要エディション数です。"),
            new Quiz(9, "197", "この数字、なんの数字？",
                    List.of("Minecraft の月間アクティブユーザー数（百万人）", "Teams の月間アクティブユーザー数（百万人）", "Outlook のメール送信数（億通/日）", "Xbox Game Pass のゲーム数"), 0,
                    "約1億9,700万人はMinecraftの月間アクティブユーザー数です。"),
            new Quiz(10, "10", "この数字、なんの数字？",
                    List.of("Microsoft が発表した Surface の世代数", "Windows 10 のバージョン番号", "Microsoft Edge のメジャーバージョン数", "Azure DevOps のサービス数"), 1,
                    "10はWindows 10のバージョン番号です。")
    );

    @BeforeEach
    void setUp() {
        quizService = new QuizService(quizDataLoader);
        when(quizDataLoader.getQuizzes()).thenReturn(sampleQuizzes);
    }

    @Test
    void getAllQuizzes_returnsAllQuizzes() {
        List<Quiz> result = quizService.getAllQuizzes();
        assertThat(result).hasSize(10);
    }

    @Test
    void getQuizById_existingId_returnsQuiz() {
        Optional<Quiz> result = quizService.getQuizById(1);
        assertThat(result).isPresent();
        assertThat(result.get().number()).isEqualTo("1975");
    }

    @Test
    void getQuizById_nonExistingId_returnsEmpty() {
        Optional<Quiz> result = quizService.getQuizById(999);
        assertThat(result).isEmpty();
    }

    @Test
    void checkAnswer_correctAnswer_returnsTrue() {
        boolean result = quizService.checkAnswer(1, 0);
        assertThat(result).isTrue();
    }

    @Test
    void checkAnswer_wrongAnswer_returnsFalse() {
        boolean result = quizService.checkAnswer(1, 1);
        assertThat(result).isFalse();
    }

    @Test
    void getTotalCount_returnsCorrectCount() {
        int result = quizService.getTotalCount();
        assertThat(result).isEqualTo(10);
    }

    @Test
    void allQuizzes_haveRequiredFields() {
        List<Quiz> result = quizService.getAllQuizzes();
        assertThat(result).allSatisfy(quiz -> {
            assertThat(quiz.id()).isPositive();
            assertThat(quiz.number()).isNotBlank();
            assertThat(quiz.question()).isNotBlank();
            assertThat(quiz.choices()).isNotNull();
            assertThat(quiz.explanation()).isNotBlank();
        });
    }

    @Test
    void allQuizzes_haveFourChoices() {
        List<Quiz> result = quizService.getAllQuizzes();
        assertThat(result).allSatisfy(quiz ->
                assertThat(quiz.choices()).hasSize(4)
        );
    }

    @Test
    void allQuizzes_haveValidAnswerIndex() {
        List<Quiz> result = quizService.getAllQuizzes();
        assertThat(result).allSatisfy(quiz ->
                assertThat(quiz.answer()).isBetween(0, 3)
        );
    }
}

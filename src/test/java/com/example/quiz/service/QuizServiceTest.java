package com.example.quiz.service;

import com.example.quiz.dataloader.QuizDataLoader;
import com.example.quiz.model.Quiz;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
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
                    List.of("Microsoft が設立された年", "Windows 1.0 が発売された年",
                            "IBM PC が発売された年", "インターネットが一般公開された年"),
                    0, "1975年はMicrosoftが設立された年です。"),
            new Quiz(2, "75", "この数字、なんの数字？",
                    List.of("Microsoft 365のアプリ数", "Azureのリージョン数（60以上ある地域の概数）",
                            "Windowsの累計バージョン数", "Microsoft が事業展開する国の数"),
                    1, "75はAzureのリージョン数の概数です。"),
            new Quiz(3, "75億", "この数字、なんの数字？",
                    List.of("Microsoft の2024年度の年間売上高（ドル）", "GitHub のリポジトリ総数",
                            "Microsoft がGitHub を買収した金額（ドル）", "Xbox の累計販売台数"),
                    2, "75億ドルは、2018年にMicrosoftがGitHubを買収した金額です。"),
            new Quiz(4, "262億", "この数字、なんの数字？",
                    List.of("Microsoft がActivision Blizzardを買収した金額（ドル）",
                            "Microsoft がLinkedInを買収した金額（ドル）",
                            "Azureの年間売上高（ドル）", "Microsoft の時価総額（ドル）"),
                    1, "262億ドルは、2016年にMicrosoftがLinkedInを買収した金額です。"),
            new Quiz(5, "2001", "この数字、なんの数字？",
                    List.of("Windows XP が発売された年", "初代 Xbox が発売された年",
                            "Microsoft Azure が発表された年", "Internet Explorer 6 がリリースされた年"),
                    1, "2001年は初代Xboxが北米で発売された年です。"),
            new Quiz(6, "4億", "この数字、なんの数字？",
                    List.of("Outlook のユーザー数", "Xbox Game Pass の加入者数",
                            "Microsoft Teams の月間アクティブユーザー数",
                            "Microsoft 365 の有料サブスクリプション数"),
                    2, "4億人はMicrosoft Teamsの月間アクティブユーザー数の概数です。"),
            new Quiz(7, "228,000", "この数字、なんの数字？",
                    List.of("Microsoft の全世界の従業員数（概数）", "Azure のサービス数",
                            "Microsoft が保有する特許の数", "Microsoft 本社キャンパスの面積（平方メートル）"),
                    0, "約228,000人はMicrosoftの全世界の従業員数です。"),
            new Quiz(8, "690億", "この数字、なんの数字？",
                    List.of("Microsoft がNokiaの携帯電話事業を買収した金額（ドル）",
                            "Microsoft がActivision Blizzardを買収した金額（ドル）",
                            "Microsoft の年間研究開発費（ドル）", "Windows 11 のインストール数"),
                    1, "690億ドルは、2023年にMicrosoftがActivision Blizzardを買収した金額です。"),
            new Quiz(9, "1985", "この数字、なんの数字？",
                    List.of("MS-DOS が発売された年", "初代 Windows が発売された年",
                            "Microsoft が株式を上場した年", "Excel の初版がリリースされた年"),
                    1, "1985年は初代Windows（Windows 1.0）が発売された年です。"),
            new Quiz(10, "100億", "この数字、なんの数字？",
                    List.of("Microsoft がOpenAIに投資した累計額（ドル）の概数",
                            "Bing の月間検索クエリ数", "Microsoft Store のアプリ総数",
                            "OneDrive のユーザー数"),
                    0, "約100億ドル以上は、MicrosoftがOpenAIに投資した累計額の概数です。")
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
        boolean result = quizService.checkAnswer(1, 3);
        assertThat(result).isFalse();
    }

    @Test
    void getTotalCount_returnsCorrectCount() {
        int result = quizService.getTotalCount();
        assertThat(result).isEqualTo(10);
    }

    @Nested
    class MicrosoftQuizDataValidation {

        @Test
        void allQuizzes_haveRequiredFields() {
            List<Quiz> quizzes = quizService.getAllQuizzes();
            for (Quiz quiz : quizzes) {
                assertThat(quiz.id()).isPositive();
                assertThat(quiz.number()).isNotBlank();
                assertThat(quiz.question()).isNotBlank();
                assertThat(quiz.choices()).isNotNull();
                assertThat(quiz.answer()).isGreaterThanOrEqualTo(0);
                assertThat(quiz.explanation()).isNotBlank();
            }
        }

        @Test
        void allQuizzes_haveFourChoices() {
            List<Quiz> quizzes = quizService.getAllQuizzes();
            for (Quiz quiz : quizzes) {
                assertThat(quiz.choices()).hasSize(4);
            }
        }

        @Test
        void allQuizzes_haveAnswerIndexInValidRange() {
            List<Quiz> quizzes = quizService.getAllQuizzes();
            for (Quiz quiz : quizzes) {
                assertThat(quiz.answer())
                        .as("Quiz id=%d の answer が 0〜3 の範囲であること", quiz.id())
                        .isBetween(0, 3);
            }
        }

        @Test
        void totalQuizCount_isTen() {
            assertThat(quizService.getAllQuizzes()).hasSize(10);
        }
    }
}

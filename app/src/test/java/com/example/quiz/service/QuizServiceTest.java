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
            new Quiz(1, "3,776", "この数字、なんの数字？",
                    List.of("A", "B", "C", "D"), 1, "解説1"),
            new Quiz(2, "47", "この数字、なんの数字？",
                    List.of("A", "B", "C", "D"), 2, "解説2")
    );

    @BeforeEach
    void setUp() {
        quizService = new QuizService(quizDataLoader);
        when(quizDataLoader.getQuizzes()).thenReturn(sampleQuizzes);
    }

    @Test
    void getAllQuizzes_returnsAllQuizzes() {
        List<Quiz> result = quizService.getAllQuizzes();
        assertThat(result).hasSize(2);
    }

    @Test
    void getQuizById_existingId_returnsQuiz() {
        Optional<Quiz> result = quizService.getQuizById(1);
        assertThat(result).isPresent();
        assertThat(result.get().number()).isEqualTo("3,776");
    }

    @Test
    void getQuizById_nonExistingId_returnsEmpty() {
        Optional<Quiz> result = quizService.getQuizById(999);
        assertThat(result).isEmpty();
    }

    @Test
    void checkAnswer_correctAnswer_returnsTrue() {
        boolean result = quizService.checkAnswer(1, 1);
        assertThat(result).isTrue();
    }

    @Test
    void checkAnswer_wrongAnswer_returnsFalse() {
        boolean result = quizService.checkAnswer(1, 0);
        assertThat(result).isFalse();
    }

    @Test
    void getTotalCount_returnsCorrectCount() {
        int result = quizService.getTotalCount();
        assertThat(result).isEqualTo(2);
    }
}

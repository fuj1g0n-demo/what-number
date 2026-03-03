package com.example.quiz.controller;

import com.example.quiz.model.Quiz;
import com.example.quiz.service.QuizService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.Mockito.when;

@WebMvcTest(QuizController.class)
class QuizControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private QuizService quizService;

    private final Quiz sampleQuiz = new Quiz(
            1, "1975", "この数字、なんの数字？",
            List.of("Microsoft が設立された年", "Windows 1.0 が発売された年", "MS-DOS が発表された年", "Xbox が発売された年"),
            0, "1975年はMicrosoftが設立された年です。"
    );

    @Test
    void index_displaysQuizList() throws Exception {
        when(quizService.getAllQuizzes()).thenReturn(List.of(sampleQuiz));

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attributeExists("quizzes"));
    }

    @Test
    void showQuiz_existingId_displaysQuiz() throws Exception {
        when(quizService.getQuizById(1)).thenReturn(Optional.of(sampleQuiz));
        when(quizService.getTotalCount()).thenReturn(10);

        mockMvc.perform(get("/quiz/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("quiz"))
                .andExpect(model().attributeExists("quiz"));
    }

    @Test
    void answer_correctAnswer_showsResult() throws Exception {
        when(quizService.getQuizById(1)).thenReturn(Optional.of(sampleQuiz));
        when(quizService.checkAnswer(1, 0)).thenReturn(true);
        when(quizService.getTotalCount()).thenReturn(10);

        mockMvc.perform(post("/quiz/1/answer").param("selectedAnswer", "0"))
                .andExpect(status().isOk())
                .andExpect(view().name("result"))
                .andExpect(model().attribute("correct", true));
    }
}

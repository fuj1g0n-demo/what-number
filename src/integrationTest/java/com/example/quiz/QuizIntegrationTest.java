package com.example.quiz;

import com.example.quiz.model.Quiz;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration Test: Spring コンテキスト全体を起動し、
 * Controller → Service → DataLoader の結合をテストする。
 */
@SpringBootTest
@AutoConfigureMockMvc
class QuizIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void indexPage_displays10Quizzes() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attribute("quizzes", hasSize(10)));
    }

    @Test
    void quizPage_displaysNumberAndChoices() throws Exception {
        MvcResult result = mockMvc.perform(get("/quiz/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("quiz"))
                .andExpect(model().attributeExists("quiz"))
                .andReturn();

        Quiz quiz = (Quiz) result.getModelAndView().getModel().get("quiz");
        assertThat(quiz.number()).isEqualTo("1975");
        assertThat(quiz.choices()).hasSize(4);
    }

    @Test
    void quizPage_eachQuizLoadsSuccessfully() throws Exception {
        for (int id = 1; id <= 10; id++) {
            mockMvc.perform(get("/quiz/" + id))
                    .andExpect(status().isOk())
                    .andExpect(view().name("quiz"))
                    .andExpect(model().attributeExists("quiz"));
        }
    }

    @Test
    void answer_correctAnswer_showsCorrectResultAndExplanation() throws Exception {
        // Quiz 1: answer index is 0
        MvcResult result = mockMvc.perform(post("/quiz/1/answer").param("selectedAnswer", "0"))
                .andExpect(status().isOk())
                .andExpect(view().name("result"))
                .andExpect(model().attribute("correct", true))
                .andExpect(model().attributeExists("quiz"))
                .andReturn();

        Quiz quiz = (Quiz) result.getModelAndView().getModel().get("quiz");
        assertThat(quiz.explanation()).contains("Microsoft");
    }

    @Test
    void answer_wrongAnswer_showsIncorrectResultAndExplanation() throws Exception {
        // Quiz 1: answer index is 0, so selecting 1 is wrong
        MvcResult result = mockMvc.perform(post("/quiz/1/answer").param("selectedAnswer", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("result"))
                .andExpect(model().attribute("correct", false))
                .andExpect(model().attributeExists("quiz"))
                .andReturn();

        Quiz quiz = (Quiz) result.getModelAndView().getModel().get("quiz");
        assertThat(quiz.explanation()).contains("Microsoft");
    }
}

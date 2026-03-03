package com.example.quiz;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

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
    void indexPage_loadsSuccessfully() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"));
    }

    @Test
    void quizPage_loadsSuccessfully() throws Exception {
        mockMvc.perform(get("/quiz/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("quiz"));
    }
}

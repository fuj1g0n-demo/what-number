package com.example.quiz.controller;

import com.example.quiz.model.Quiz;
import com.example.quiz.service.QuizService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * クイズ画面の Controller。
 */
@Controller
public class QuizController {

    private final QuizService quizService;

    public QuizController(QuizService quizService) {
        this.quizService = quizService;
    }

    /**
     * トップページ（クイズ一覧）を表示する。
     */
    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("quizzes", quizService.getAllQuizzes());
        return "index";
    }

    /**
     * 個別のクイズ問題を表示する。
     */
    @GetMapping("/quiz/{id}")
    public String showQuiz(@PathVariable int id, Model model) {
        Quiz quiz = quizService.getQuizById(id)
                .orElseThrow(() -> new IllegalArgumentException("Quiz not found: " + id));
        model.addAttribute("quiz", quiz);
        model.addAttribute("totalCount", quizService.getTotalCount());
        return "quiz";
    }

    /**
     * 回答を受け取り、結果を表示する。
     */
    @PostMapping("/quiz/{id}/answer")
    public String answer(@PathVariable int id,
                         @RequestParam int selectedAnswer,
                         Model model) {
        Quiz quiz = quizService.getQuizById(id)
                .orElseThrow(() -> new IllegalArgumentException("Quiz not found: " + id));
        boolean correct = quizService.checkAnswer(id, selectedAnswer);

        model.addAttribute("quiz", quiz);
        model.addAttribute("correct", correct);
        model.addAttribute("selectedAnswer", selectedAnswer);
        model.addAttribute("totalCount", quizService.getTotalCount());
        return "result";
    }
}

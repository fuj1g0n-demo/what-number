package com.example.quiz.service;

import com.example.quiz.dataloader.QuizDataLoader;
import com.example.quiz.model.Quiz;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * クイズに関するビジネスロジックを提供するサービス。
 */
@Service
public class QuizService {

    private final QuizDataLoader quizDataLoader;

    public QuizService(QuizDataLoader quizDataLoader) {
        this.quizDataLoader = quizDataLoader;
    }

    /**
     * 全クイズを取得する。
     */
    public List<Quiz> getAllQuizzes() {
        return quizDataLoader.getQuizzes();
    }

    /**
     * 指定 ID のクイズを取得する。
     */
    public Optional<Quiz> getQuizById(int id) {
        return quizDataLoader.getQuizzes().stream()
                .filter(q -> q.id() == id)
                .findFirst();
    }

    /**
     * 回答が正解かどうかを判定する。
     */
    public boolean checkAnswer(int quizId, int selectedAnswer) {
        return getQuizById(quizId)
                .map(q -> q.answer() == selectedAnswer)
                .orElse(false);
    }

    /**
     * クイズの総数を返す。
     */
    public int getTotalCount() {
        return quizDataLoader.getQuizzes().size();
    }
}

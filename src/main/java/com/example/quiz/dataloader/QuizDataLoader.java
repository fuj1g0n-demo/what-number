package com.example.quiz.dataloader;

import com.example.quiz.model.Quiz;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

/**
 * アプリケーション起動時に JSON ファイルからクイズデータをロードするコンポーネント。
 */
@Component
public class QuizDataLoader {

    private static final Logger log = LoggerFactory.getLogger(QuizDataLoader.class);
    private static final String DATA_FILE = "data/quizzes.json";

    private final ObjectMapper objectMapper;
    private List<Quiz> quizzes = Collections.emptyList();

    public QuizDataLoader(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    void loadQuizzes() throws IOException {
        try (InputStream is = new ClassPathResource(DATA_FILE).getInputStream()) {
            quizzes = Collections.unmodifiableList(
                    objectMapper.readValue(is, new TypeReference<List<Quiz>>() {})
            );
            log.info("Loaded {} quizzes from {}", quizzes.size(), DATA_FILE);
        }
    }

    /**
     * ロード済みのクイズ一覧を返す（イミュータブル）。
     */
    public List<Quiz> getQuizzes() {
        return quizzes;
    }
}

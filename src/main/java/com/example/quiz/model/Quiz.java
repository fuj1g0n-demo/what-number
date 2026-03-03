package com.example.quiz.model;

import java.util.List;

/**
 * クイズの問題を表す Record。
 *
 * @param id      問題 ID
 * @param number  「この数字」として提示する数字
 * @param question 質問文（例: 「この数字、なんの数字？」）
 * @param choices 選択肢のリスト
 * @param answer  正解の選択肢インデックス（0始まり）
 * @param explanation 解説文
 */
public record Quiz(
        int id,
        String number,
        String question,
        List<String> choices,
        int answer,
        String explanation
) {
}

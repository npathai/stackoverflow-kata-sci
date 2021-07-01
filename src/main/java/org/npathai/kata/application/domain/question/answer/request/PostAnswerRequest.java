package org.npathai.kata.application.domain.question.answer.request;

public class PostAnswerRequest {

    private String body;

    private PostAnswerRequest(String body) {
        this.body = body;
    }

    public static PostAnswerRequest valid(String body) {
        return new PostAnswerRequest(body);
    }
}

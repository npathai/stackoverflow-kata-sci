package org.npathai.kata.application.domain.question.answer;

import org.npathai.kata.application.domain.question.answer.dto.Answer;

import java.util.Random;

public final class AnswerBuilder {
    private String id;
    private String body;
    private String authorId;
    private String questionId;

    private AnswerBuilder() {
        int random = new Random().nextInt(Integer.MAX_VALUE);
        id = "Answer" + random;
        body = "Answer body for id: " + random;
        authorId = "User" + random;
        questionId = "Question" + random;
    }

    public static AnswerBuilder anAnswer() {
        return new AnswerBuilder();
    }

    public AnswerBuilder withId(String id) {
        this.id = id;
        return this;
    }

    public AnswerBuilder withBody(String body) {
        this.body = body;
        return this;
    }

    public AnswerBuilder withAuthorId(String authorId) {
        this.authorId = authorId;
        return this;
    }

    public AnswerBuilder withQuestionId(String questionId) {
        this.questionId = questionId;
        return this;
    }

    public Answer build() {
        Answer answer = new Answer();
        answer.setId(id);
        answer.setBody(body);
        answer.setAuthorId(authorId);
        answer.setQuestionId(questionId);
        return answer;
    }
}

package org.npathai.kata.application.domain.question;

import org.npathai.kata.application.domain.question.dto.Question;
import org.npathai.kata.application.domain.tag.dto.Tag;

import java.util.Collections;
import java.util.List;
import java.util.Random;

import static org.npathai.kata.application.domain.tag.TagBuilder.aTag;

public final class QuestionBuilder {
    private String id;
    private String title;
    private String body;
    private List<Tag> tags;
    private long createdAt;
    private String authorId;
    private int answerCount;
    private int score;

    private QuestionBuilder() {
        int random = new Random().nextInt(Integer.MAX_VALUE);
        id = "Question" + random;
        title = "Title" + random;
        body = "Question body for question: " + id;
        tags = Collections.emptyList();
        createdAt = System.currentTimeMillis();
        authorId = "Author" + random;
        tags = List.of(aTag().build());
    }

    public static QuestionBuilder aQuestion() {
        return new QuestionBuilder();
    }

    public QuestionBuilder withId(String id) {
        this.id = id;
        return this;
    }

    public QuestionBuilder withTitle(String title) {
        this.title = title;
        return this;
    }

    public QuestionBuilder withBody(String body) {
        this.body = body;
        return this;
    }

    public QuestionBuilder withTags(List<Tag> tags) {
        this.tags = tags;
        return this;
    }

    public QuestionBuilder withCreatedAt(long createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public QuestionBuilder withAuthorId(String authorId) {
        this.authorId = authorId;
        return this;
    }

    public QuestionBuilder withAnswerCount(int answerCount) {
        this.answerCount = answerCount;
        return this;
    }

    public QuestionBuilder withScore(int score) {
        this.score = score;
        return this;
    }

    public Question build() {
        Question question = new Question();
        question.setId(id);
        question.setTitle(title);
        question.setBody(body);
        question.setTags(tags);
        question.setCreatedAt(createdAt);
        question.setAuthorId(authorId);
        question.setAnswerCount(answerCount);
        question.setScore(score);
        return question;
    }
}

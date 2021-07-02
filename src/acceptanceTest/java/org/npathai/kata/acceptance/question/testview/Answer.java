package org.npathai.kata.acceptance.question.testview;

import lombok.Data;

@Data
public class Answer {
    private String id;
    private String authorId;
    private String questionId;
    private String body;
    private int score;
}

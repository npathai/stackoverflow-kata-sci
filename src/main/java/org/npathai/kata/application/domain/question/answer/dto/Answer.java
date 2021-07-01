package org.npathai.kata.application.domain.question.answer.dto;

import lombok.Data;

@Data
public class Answer {
    private String id;
    private String body;
    private String authorId;
    private String questionId;
}

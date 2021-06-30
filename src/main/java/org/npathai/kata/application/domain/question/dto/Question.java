package org.npathai.kata.application.domain.question.dto;

import lombok.Data;

import java.util.List;

@Data
public class Question {
    private String id;
    private String title;
    private String body;
    private List<String> tags;
    private long createdAt;
}

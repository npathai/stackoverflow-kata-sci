package org.npathai.kata.acceptance.question.testview;

import lombok.Data;

import java.util.List;

@Data
public class Question {
    private String id;
    private String title;
    private String body;
    private List<String> tags;
    private String status;
    private String reason;
    private String createdAt;
}

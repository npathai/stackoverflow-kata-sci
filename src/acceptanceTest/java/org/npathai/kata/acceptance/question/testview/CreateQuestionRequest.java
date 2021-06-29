package org.npathai.kata.acceptance.question.testview;

import lombok.Data;

import java.util.List;

@Data
public class CreateQuestionRequest {
    private String title;
    private String body;
    private List<String> tags;
    private String userId;
}

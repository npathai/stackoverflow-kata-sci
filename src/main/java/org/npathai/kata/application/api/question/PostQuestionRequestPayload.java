package org.npathai.kata.application.api.question;

import lombok.Data;

import java.util.List;

@Data
public class PostQuestionRequestPayload {
    private String title;
    private String body;
    private List<String> tags;
}

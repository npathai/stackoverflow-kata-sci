package org.npathai.kata.acceptance.question.testview;

import lombok.Data;

import java.util.List;

@Data
public class QuestionWithAnswers {
    private Question question;
    private List<Answer> answers;
}

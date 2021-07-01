package org.npathai.kata.application.domain.question.dto;

import lombok.Data;
import org.npathai.kata.application.domain.question.answer.dto.Answer;

import java.util.List;

@Data
public class QuestionWithAnswers {
    private Question question;
    private List<Answer> answers;
}

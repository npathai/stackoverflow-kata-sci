package org.npathai.kata.application.api.question;

import org.npathai.kata.application.domain.question.QuestionService;
import org.npathai.kata.application.domain.question.dto.Question;
import org.springframework.http.ResponseEntity;

public class QuestionController {
    public ResponseEntity<Question> createQuestion(String userId, PostQuestionRequestPayload payload) {
        throw new UnsupportedOperationException();
    }
}
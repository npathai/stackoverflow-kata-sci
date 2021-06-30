package org.npathai.kata.application.api.question;

import org.npathai.kata.application.api.validation.BadRequestParametersException;
import org.npathai.kata.application.domain.question.QuestionService;
import org.npathai.kata.application.domain.question.dto.Question;
import org.npathai.kata.application.domain.question.request.PostQuestionRequest;
import org.springframework.http.ResponseEntity;

public class QuestionController {

    private final QuestionService questionService;
    private final PostQuestionRequestPayloadValidator postQuestionRequestPayloadValidator;

    public QuestionController(QuestionService questionService,
                              PostQuestionRequestPayloadValidator postQuestionRequestPayloadValidator) {
        this.questionService = questionService;
        this.postQuestionRequestPayloadValidator = postQuestionRequestPayloadValidator;
    }

    public ResponseEntity<Question> createQuestion(String userId, PostQuestionRequestPayload payload) throws BadRequestParametersException {
        try {
            PostQuestionRequest request = postQuestionRequestPayloadValidator.validate(payload);
            Question question = questionService.post(request);
            return ResponseEntity.created(null).body(question);
        } catch (BadRequestParametersException ex) {
            return ResponseEntity.badRequest().build();
        }
    }
}
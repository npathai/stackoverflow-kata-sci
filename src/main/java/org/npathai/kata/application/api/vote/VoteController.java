package org.npathai.kata.application.api.vote;

import org.npathai.kata.application.api.validation.BadRequestParametersException;
import org.npathai.kata.application.domain.question.QuestionId;
import org.npathai.kata.application.domain.question.QuestionService;
import org.npathai.kata.application.domain.user.UserId;
import org.npathai.kata.application.domain.vote.dto.Score;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

public class VoteController {

    private final QuestionService questionService;
    private final VoteRequestPayloadValidator validator;

    public VoteController(QuestionService questionService, VoteRequestPayloadValidator validator) {
        this.questionService = questionService;
        this.validator = validator;
    }

    public ResponseEntity<Score> voteQuestion(String userId, String questionId, VoteRequestPayload payload) {
        try {
            Score score = questionService.voteQuestion(UserId.validated(userId), QuestionId.validated(questionId),
                    validator.validate(payload));
            return ResponseEntity.ok().body(score);
        } catch (BadRequestParametersException ex) {
            return ResponseEntity.badRequest().build();
        }
    }
}

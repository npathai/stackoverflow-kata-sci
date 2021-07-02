package org.npathai.kata.application.api.vote;

import org.npathai.kata.application.api.validation.BadRequestParametersException;
import org.npathai.kata.application.domain.ImpermissibleOperationException;
import org.npathai.kata.application.domain.question.QuestionId;
import org.npathai.kata.application.domain.question.QuestionService;
import org.npathai.kata.application.domain.user.InsufficientReputationException;
import org.npathai.kata.application.domain.user.UserId;
import org.npathai.kata.application.domain.vote.dto.Score;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/q")
public class VoteController {

    private final QuestionService questionService;
    private final VoteRequestPayloadValidator validator;

    public VoteController(QuestionService questionService, VoteRequestPayloadValidator validator) {
        this.questionService = questionService;
        this.validator = validator;
    }

    @PostMapping("/{questionId}/votes")
    public ResponseEntity<Score> voteQuestion(@RequestHeader String userId, @PathVariable String questionId,
                                              @RequestBody VoteRequestPayload payload) {
        try {
            Score score = questionService.voteQuestion(UserId.validated(userId), QuestionId.validated(questionId),
                    validator.validate(payload));
            return ResponseEntity.ok().body(score);
        } catch (BadRequestParametersException | ImpermissibleOperationException ex) {
            return ResponseEntity.badRequest().build();
        } catch (InsufficientReputationException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}

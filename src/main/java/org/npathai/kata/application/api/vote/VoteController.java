package org.npathai.kata.application.api.vote;

import lombok.SneakyThrows;
import org.npathai.kata.application.api.validation.BadRequestParametersException;
import org.npathai.kata.application.domain.ImpermissibleOperationException;
import org.npathai.kata.application.domain.question.QuestionId;
import org.npathai.kata.application.domain.question.QuestionService;
import org.npathai.kata.application.domain.question.answer.dto.Answer;
import org.npathai.kata.application.domain.question.answer.dto.AnswerId;
import org.npathai.kata.application.domain.user.InsufficientReputationException;
import org.npathai.kata.application.domain.user.UserId;
import org.npathai.kata.application.domain.vote.VoteRequest;
import org.npathai.kata.application.domain.vote.dto.Score;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/")
public class VoteController {

    private final QuestionService questionService;
    private final VoteRequestPayloadValidator validator;

    public VoteController(QuestionService questionService, VoteRequestPayloadValidator validator) {
        this.questionService = questionService;
        this.validator = validator;
    }

    @PostMapping("/q/{questionId}/votes")
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

    @DeleteMapping("/q/{questionId}/votes")
    public ResponseEntity<Score> cancelVote(@RequestHeader String userId, @PathVariable String questionId) {
        try {
            Score score = questionService.cancelVote(UserId.validated(userId), QuestionId.validated(questionId));
            return ResponseEntity.ok(score);
        } catch (BadRequestParametersException ex) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/q/{questionId}/a/{answerId}/votes")
    public ResponseEntity<Score> voteAnswer(@RequestHeader String userId, @PathVariable String questionId,
                                            @PathVariable String answerId, @RequestBody VoteRequestPayload payload) {
        try {
            VoteRequest request = validator.validate(payload);
            Score score = questionService.voteAnswer(UserId.validated(userId), AnswerId.validated(answerId), request);
            return ResponseEntity.ok(score);
        } catch (BadRequestParametersException | ImpermissibleOperationException ex) {
            return ResponseEntity.badRequest().build();
        } catch (InsufficientReputationException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @DeleteMapping("/q/{questionId}/a/{answerId}/votes")
    public ResponseEntity<Score> cancelAnswerVote(@RequestHeader String userId, @PathVariable String questionId,
                                                  @PathVariable String answerId) {
        try {
            Score score = questionService.cancelAnswerVote(UserId.validated(userId), AnswerId.validated(answerId));
            return ResponseEntity.ok(score);
        } catch (BadRequestParametersException ex) {
            return ResponseEntity.badRequest().build();
        }
    }
}

package org.npathai.kata.application.api.question;

import lombok.SneakyThrows;
import org.npathai.kata.application.api.question.answer.PostAnswerRequestPayload;
import org.npathai.kata.application.api.question.answer.PostAnswerRequestPayloadValidator;
import org.npathai.kata.application.api.validation.BadRequestParametersException;
import org.npathai.kata.application.domain.question.QuestionClosedException;
import org.npathai.kata.application.domain.question.QuestionId;
import org.npathai.kata.application.domain.question.QuestionService;
import org.npathai.kata.application.domain.question.answer.dto.Answer;
import org.npathai.kata.application.domain.question.answer.request.PostAnswerRequest;
import org.npathai.kata.application.domain.question.dto.CloseVoteSummary;
import org.npathai.kata.application.domain.question.dto.Question;
import org.npathai.kata.application.domain.question.dto.QuestionWithAnswers;
import org.npathai.kata.application.domain.question.request.PostQuestionRequest;
import org.npathai.kata.application.domain.services.UnknownEntityException;
import org.npathai.kata.application.domain.user.UserId;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/q")
public class QuestionController {

    private final QuestionService questionService;
    private final PostQuestionRequestPayloadValidator postQuestionRequestPayloadValidator;
    private final PostAnswerRequestPayloadValidator postAnswerRequestPayloadValidator;

    public QuestionController(QuestionService questionService,
                              PostQuestionRequestPayloadValidator postQuestionRequestPayloadValidator,
                              PostAnswerRequestPayloadValidator postAnswerRequestPayloadValidator) {
        this.questionService = questionService;
        this.postQuestionRequestPayloadValidator = postQuestionRequestPayloadValidator;
        this.postAnswerRequestPayloadValidator = postAnswerRequestPayloadValidator;
    }

    @PostMapping
    public ResponseEntity<Question> createQuestion(@RequestHeader String userId, @RequestBody PostQuestionRequestPayload payload) {
        try {
            PostQuestionRequest request = postQuestionRequestPayloadValidator.validate(payload);
            Question question = questionService.post(UserId.validated(userId), request);
            return ResponseEntity.created(null).body(question);
        } catch (BadRequestParametersException ex) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/recent")
    public ResponseEntity<Page<Question>> recentQuestions() {
        return ResponseEntity.ok(questionService.getRecentQuestions());
    }

    @PostMapping("/{questionId}/a")
    @SneakyThrows
    public ResponseEntity<Answer> createAnswer(@RequestHeader String userId, @PathVariable String questionId,
                                               @RequestBody PostAnswerRequestPayload payload) {
        try {
            PostAnswerRequest request = postAnswerRequestPayloadValidator.validate(payload);
            return ResponseEntity.created(null).body(questionService.postAnswer(UserId.validated(userId),
                    QuestionId.validated(questionId), request));
        } catch (BadRequestParametersException | QuestionClosedException ex) {
            return ResponseEntity.badRequest().build();
        }
    }


    @GetMapping("/{questionId}")
    public ResponseEntity<QuestionWithAnswers> getQuestionById(@PathVariable String questionId) {
        try {
            QuestionWithAnswers  questionWithAnswers = questionService.getQuestion(QuestionId.validated(questionId));
            return ResponseEntity.ok().body(questionWithAnswers);
        } catch(BadRequestParametersException ex) {
            return ResponseEntity.badRequest().build();
        } catch (UnknownEntityException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    @SneakyThrows
    @PostMapping("/{questionId}/close-votes")
    public ResponseEntity<CloseVoteSummary> closeVote(@RequestHeader String userId, @PathVariable String questionId) {
        try {
            return ResponseEntity.ok(questionService.closeVote(UserId.validated(userId), QuestionId.validated(questionId)));
        } catch (BadRequestParametersException ex) {
            return ResponseEntity.badRequest().build();
        } catch (UnknownEntityException ex) {
            return ResponseEntity.notFound().build();
        }
    }
}
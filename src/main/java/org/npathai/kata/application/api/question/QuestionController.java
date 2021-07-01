package org.npathai.kata.application.api.question;

import org.npathai.kata.application.api.validation.BadRequestParametersException;
import org.npathai.kata.application.domain.question.QuestionService;
import org.npathai.kata.application.domain.question.dto.Question;
import org.npathai.kata.application.domain.question.request.PostQuestionRequest;
import org.npathai.kata.application.domain.user.UserId;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/q")
public class QuestionController {

    private final QuestionService questionService;
    private final PostQuestionRequestPayloadValidator postQuestionRequestPayloadValidator;

    public QuestionController(QuestionService questionService,
                              PostQuestionRequestPayloadValidator postQuestionRequestPayloadValidator) {
        this.questionService = questionService;
        this.postQuestionRequestPayloadValidator = postQuestionRequestPayloadValidator;
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
}
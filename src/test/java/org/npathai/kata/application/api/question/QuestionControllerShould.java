package org.npathai.kata.application.api.question;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.npathai.kata.application.api.validation.BadRequestParametersException;
import org.npathai.kata.application.domain.question.QuestionService;
import org.npathai.kata.application.domain.question.dto.Question;
import org.npathai.kata.application.domain.question.request.PostQuestionRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class QuestionControllerShould {
    private static final String USER_ID = "1";
    public static final String QUESTION_TITLE = "First question";
    public static final String QUESTION_BODY = "First question body";
    public static final List<String> QUESTION_TAGS = List.of("java", "kata");
    private static final String QUESTION_ID = "1";
    private static final PostQuestionRequest VALID_REQUEST = PostQuestionRequest.valid(QUESTION_TITLE, QUESTION_BODY, QUESTION_TAGS);

    @Mock
    QuestionService questionService;

    @Mock
    PostQuestionRequestPayloadValidator validator;

    @InjectMocks
    QuestionController questionController;

    @Test
    public void returnCreatedQuestion() throws BadRequestParametersException {
        PostQuestionRequestPayload payload = aRequestPayload();
        Question question = aQuestion();
        given(validator.validate(payload)).willReturn(VALID_REQUEST);
        given(questionService.post(VALID_REQUEST)).willReturn(question);

        ResponseEntity<Question> response = questionController.createQuestion(USER_ID, payload);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isSameAs(question);
    }

    @Test
    public void returnStatusBadRequestWhenPayloadIsInvalid() throws BadRequestParametersException {
        PostQuestionRequestPayload payload = aRequestPayload();

        given(validator.validate(payload)).willThrow(BadRequestParametersException.class);

        ResponseEntity<Question> response = questionController.createQuestion(USER_ID, payload);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNull();
    }

    private PostQuestionRequestPayload aRequestPayload() {
        PostQuestionRequestPayload payload = new PostQuestionRequestPayload();
        payload.setTitle(QUESTION_TITLE);
        payload.setBody(QUESTION_BODY);
        payload.setTags(QUESTION_TAGS);
        return payload;
    }

    private Question aQuestion() {
        Question question = new Question();
        question.setId(QUESTION_ID);
        question.setTitle(QUESTION_TITLE);
        question.setBody(QUESTION_BODY);
        question.setCreatedAt(System.currentTimeMillis());
        question.setTags(QUESTION_TAGS);
        return question;
    }
}

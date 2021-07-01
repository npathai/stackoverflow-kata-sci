package org.npathai.kata.application.api.question;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.npathai.kata.application.api.validation.BadRequestParametersException;
import org.npathai.kata.application.domain.question.QuestionService;
import org.npathai.kata.application.domain.question.dto.Question;
import org.npathai.kata.application.domain.question.request.PostQuestionRequest;
import org.npathai.kata.application.domain.tag.dto.Tag;
import org.npathai.kata.application.domain.user.UserId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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

    @Nested
    public class PostQuestionShould {
        @Test
        public void returnCreatedQuestion() throws BadRequestParametersException {
            PostQuestionRequestPayload payload = aRequestPayload();
            Question question = aQuestion();
            given(validator.validate(payload)).willReturn(VALID_REQUEST);
            given(questionService.post(UserId.validated(USER_ID), VALID_REQUEST)).willReturn(question);

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
    }

    @Nested
    public class RecentQuestionsShould {

        private List<Question> questions;

        @BeforeEach
        public void setUp() {
            questions = IntStream.range(0, 2)
                    .mapToObj(id -> aQuestion(String.valueOf(id)))
                    .collect(Collectors.toList());
        }

        @Test
        public void returnListOfQuestions() {
            Page<Question> questionPage = new PageImpl<>(questions);
            given(questionService.getRecentQuestions()).willReturn(questionPage);

            ResponseEntity<Page<Question>> firstPage = questionController.recentQuestions();

            assertThat(firstPage.getBody()).isSameAs(questionPage);
        }

        @Test
        public void returnsStatusCode200WhenReturningQuestions() {
            Page<Question> questionPage = new PageImpl<>(questions);

            given(questionService.getRecentQuestions()).willReturn(questionPage);

            ResponseEntity<Page<Question>> firstPage = questionController.recentQuestions();

            assertThat(firstPage.getStatusCode()).isEqualTo(HttpStatus.OK);
        }
    }

    private PostQuestionRequestPayload aRequestPayload() {
        PostQuestionRequestPayload payload = new PostQuestionRequestPayload();
        payload.setTitle(QUESTION_TITLE);
        payload.setBody(QUESTION_BODY);
        payload.setTags(QUESTION_TAGS);
        return payload;
    }

    private Question aQuestion(String id) {
        Question question = new Question();
        question.setId(id);
        question.setTitle(QUESTION_TITLE);
        question.setBody(QUESTION_BODY);
        question.setCreatedAt(System.currentTimeMillis());
        question.setTags(List.of(
                aTag("1", "java"),
                aTag("2", "kata")
        ));
        question.setAuthorId(USER_ID);
        return question;
    }

    private Question aQuestion() {
        return aQuestion(QUESTION_ID);
    }

    private Tag aTag(String id, String name) {
        Tag tag = new Tag();
        tag.setId(id);
        tag.setName(name);
        return tag;
    }
}

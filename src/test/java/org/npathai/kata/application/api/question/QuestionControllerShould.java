package org.npathai.kata.application.api.question;

import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.npathai.kata.application.api.question.answer.PostAnswerRequestPayload;
import org.npathai.kata.application.api.question.answer.PostAnswerRequestPayloadValidator;
import org.npathai.kata.application.api.validation.BadRequestParametersException;
import org.npathai.kata.application.domain.question.QuestionClosedException;
import org.npathai.kata.application.domain.question.QuestionId;
import org.npathai.kata.application.domain.question.QuestionService;
import org.npathai.kata.application.domain.question.answer.dto.Answer;
import org.npathai.kata.application.domain.question.answer.request.PostAnswerRequest;
import org.npathai.kata.application.domain.question.dto.VoteSummary;
import org.npathai.kata.application.domain.question.dto.Question;
import org.npathai.kata.application.domain.question.dto.QuestionWithAnswers;
import org.npathai.kata.application.domain.question.request.PostQuestionRequest;
import org.npathai.kata.application.domain.services.UnknownEntityException;
import org.npathai.kata.application.domain.tag.dto.Tag;
import org.npathai.kata.application.domain.user.InsufficientReputationException;
import org.npathai.kata.application.domain.user.UserId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class QuestionControllerShould {
    private static final String USER_ID = "1";
    public static final String QUESTION_TITLE = "First question";
    public static final String QUESTION_BODY = "First question body";
    public static final List<String> QUESTION_TAGS = List.of("java", "kata");
    private static final String QUESTION_ID = "1";
    private static final PostQuestionRequest VALID_REQUEST = PostQuestionRequest.valid(QUESTION_TITLE, QUESTION_BODY, QUESTION_TAGS);
    private static final String ANSWERER_ID = "2";
    public static final String ANSWER_BODY = "This is my answer";
    public static final String ANSWER_ID = "1";
    private static final PostAnswerRequest VALID_POST_ANSWER_REQUEST = PostAnswerRequest.valid(ANSWER_BODY);

    @Mock
    QuestionService questionService;

    @Mock
    PostQuestionRequestPayloadValidator questionPayloadValidator;

    @Mock
    PostAnswerRequestPayloadValidator answerPayloadValidator;

    @InjectMocks
    QuestionController questionController;

    @Nested
    public class PostQuestionShould {

        @Test
        public void returnCreatedQuestion() throws BadRequestParametersException {
            PostQuestionRequestPayload payload = aRequestPayload();
            Question question = aQuestion();
            given(questionPayloadValidator.validate(payload)).willReturn(VALID_REQUEST);
            given(questionService.post(UserId.validated(USER_ID), VALID_REQUEST)).willReturn(question);

            ResponseEntity<Question> response = questionController.createQuestion(USER_ID, payload);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody()).isSameAs(question);
        }

        @Test
        public void returnStatusBadRequestWhenPayloadIsInvalid() throws BadRequestParametersException {
            PostQuestionRequestPayload payload = aRequestPayload();

            given(questionPayloadValidator.validate(payload)).willThrow(BadRequestParametersException.class);

            ResponseEntity<Question> response = questionController.createQuestion(USER_ID, payload);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(response.getBody()).isNull();
        }

        @Test
        public void returnStatusBadRequestWhenUserIdIsInvalid() {
            PostQuestionRequestPayload payload = aRequestPayload();

            ResponseEntity<Question> response = questionController.createQuestion("", payload);

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

    @Nested
    public class PostingAnswerShould {
        private PostAnswerRequestPayload payload;
        private Answer answer;


        @BeforeEach
        public void setUp() {
            payload = new PostAnswerRequestPayload();
            payload.setBody(ANSWER_BODY);

            answer = new Answer();
            answer.setId(ANSWER_ID);
            answer.setAuthorId(QuestionControllerShould.ANSWERER_ID);
            answer.setBody(QuestionControllerShould.ANSWER_BODY);
            answer.setQuestionId(QUESTION_ID);
        }

        @Test
        @SneakyThrows
        public void returnCreatedAnswer() {
            given(answerPayloadValidator.validate(payload)).willReturn(VALID_POST_ANSWER_REQUEST);
            given(questionService.postAnswer(UserId.validated(ANSWERER_ID), QuestionId.validated(QUESTION_ID),
                    VALID_POST_ANSWER_REQUEST)).willReturn(answer);

            ResponseEntity<Answer> response = questionController.createAnswer(ANSWERER_ID, QUESTION_ID, payload);

            assertThat(response.getBody()).isSameAs(answer);
        }

        @Test
        @SneakyThrows
        public void returns201CreatedStatusCode() {
            given(answerPayloadValidator.validate(payload)).willReturn(VALID_POST_ANSWER_REQUEST);
            given(questionService.postAnswer(UserId.validated(ANSWERER_ID), QuestionId.validated(QUESTION_ID),
                    VALID_POST_ANSWER_REQUEST)).willReturn(answer);

            assertThat(questionController.createAnswer(ANSWERER_ID, QUESTION_ID, payload).getStatusCode())
                    .isEqualTo(HttpStatus.CREATED);
        }

        @Test
        public void returns400BadRequestStatusCodeWhenRequestIsInvalid() throws BadRequestParametersException {
            given(answerPayloadValidator.validate(payload)).willThrow(BadRequestParametersException.class);

            assertThat(questionController.createAnswer(ANSWERER_ID, QUESTION_ID, payload).getStatusCode())
                    .isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        @SneakyThrows
        public void return400BadRequestWhenQuestionIsClosed() {
            given(answerPayloadValidator.validate(payload)).willReturn(VALID_POST_ANSWER_REQUEST);
            given(questionService.postAnswer(UserId.validated(ANSWERER_ID), QuestionId.validated(QUESTION_ID),
                    VALID_POST_ANSWER_REQUEST)).willThrow(QuestionClosedException.class);

            assertThat(questionController.createAnswer(ANSWERER_ID, QUESTION_ID, payload).getStatusCode())
                    .isEqualTo(HttpStatus.BAD_REQUEST);
        }
    }

    @Nested
    public class GetQuestionShould {

        @Test
        @SneakyThrows
        public void returnQuestionWithAnswers() {

            Question question  = aQuestion(QUESTION_ID);

            Answer answer = new Answer();
            answer.setId(ANSWER_ID);
            answer.setAuthorId(QuestionControllerShould.ANSWERER_ID);
            answer.setBody(QuestionControllerShould.ANSWER_BODY);
            answer.setQuestionId(QUESTION_ID);

            QuestionWithAnswers questionWithAnswers = new QuestionWithAnswers();
            questionWithAnswers.setQuestion(question);
            questionWithAnswers.setAnswers(List.of(answer));

            given(questionService.getQuestion(QuestionId.validated(QUESTION_ID))).willReturn(questionWithAnswers);

            ResponseEntity<QuestionWithAnswers> response = questionController.getQuestionById(QUESTION_ID);
            assertThat(response.getBody()).isSameAs(questionWithAnswers);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        }

        @Test
        public void return400BadRequestStatusCodeWhenQuestionIdIsInvalid() {
            ResponseEntity<QuestionWithAnswers> response = questionController.getQuestionById("");
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        public void return404NotFoundWhenQuestionNotFound() {
            given(questionService.getQuestion(any())).willThrow(UnknownEntityException.class);

            assertThat(questionController.getQuestionById("unknown").getStatusCode())
                    .isEqualTo(HttpStatus.NOT_FOUND);
        }
    }

    @Nested
    public class CloseVoteShould {
        private static final String CLOSE_VOTER_ID = "CV1";

        @Test
        @SneakyThrows
        public void returnCloseVoteSummary() {
            VoteSummary voteSummary = new VoteSummary();
            voteSummary.setCastVotes(1);
            voteSummary.setRemainingVotes(3);

            given(questionService.closeVote(UserId.validated(CLOSE_VOTER_ID), QuestionId.validated(QUESTION_ID)))
                .willReturn(voteSummary);

            ResponseEntity<VoteSummary> response = questionController.closeVote(CLOSE_VOTER_ID, QUESTION_ID);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isSameAs(voteSummary);
        }

        @Test
        public void returns400BadRequestWhenQuestionIdIsInvalid() {
            ResponseEntity<VoteSummary> response = questionController.closeVote(USER_ID, "");
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        public void returns400BadRequestWhenUserIdIsInvalid() {
            ResponseEntity<VoteSummary> response = questionController.closeVote("", QUESTION_ID);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        @SneakyThrows
        public void return404NotFoundWhenQuestionNotFound() {
            given(questionService.closeVote(eq(UserId.validated(USER_ID)), any(QuestionId.class)))
                    .willThrow(UnknownEntityException.class);
            ResponseEntity<VoteSummary> response = questionController.closeVote(USER_ID, "unknown");
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        @SneakyThrows
        public void return404NotFoundWhenUserNotFound() {
            given(questionService.closeVote(any(UserId.class), eq(QuestionId.validated(QUESTION_ID))))
                    .willThrow(UnknownEntityException.class);
            ResponseEntity<VoteSummary> response = questionController.closeVote("unknown", QUESTION_ID);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        @SneakyThrows
        public void return403UnauthorizedWhenUserDoesNotHaveSufficientReputation() {
            given(questionService.closeVote(UserId.validated(CLOSE_VOTER_ID), QuestionId.validated(QUESTION_ID)))
                    .willThrow(InsufficientReputationException.class);

            assertThat(questionController.closeVote(CLOSE_VOTER_ID, QUESTION_ID).getStatusCode())
                    .isEqualTo(HttpStatus.UNAUTHORIZED);
        }
    }

    @Nested
    public class ReopenVoteShould {
        private static final String CLOSE_VOTER_ID = "CV1";

        @Test
        @SneakyThrows
        public void returnCloseVoteSummary() {
            VoteSummary voteSummary = new VoteSummary();
            voteSummary.setCastVotes(1);
            voteSummary.setRemainingVotes(3);

            given(questionService.reopenVote(UserId.validated(CLOSE_VOTER_ID), QuestionId.validated(QUESTION_ID)))
                    .willReturn(voteSummary);

            ResponseEntity<VoteSummary> response = questionController.reopenVote(CLOSE_VOTER_ID, QUESTION_ID);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isSameAs(voteSummary);
        }

        @Test
        public void returns400BadRequestWhenQuestionIdIsInvalid() {
            ResponseEntity<VoteSummary> response = questionController.reopenVote(USER_ID, "");
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        public void returns400BadRequestWhenUserIdIsInvalid() {
            ResponseEntity<VoteSummary> response = questionController.reopenVote("", QUESTION_ID);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        @SneakyThrows
        public void return404NotFoundWhenQuestionNotFound() {
            given(questionService.reopenVote(eq(UserId.validated(USER_ID)), any(QuestionId.class)))
                    .willThrow(UnknownEntityException.class);
            ResponseEntity<VoteSummary> response = questionController.reopenVote(USER_ID, "unknown");
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        @SneakyThrows
        public void return404NotFoundWhenUserNotFound() {
            given(questionService.reopenVote(any(UserId.class), eq(QuestionId.validated(QUESTION_ID))))
                    .willThrow(UnknownEntityException.class);
            ResponseEntity<VoteSummary> response = questionController.reopenVote("unknown", QUESTION_ID);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        @SneakyThrows
        public void return403UnauthorizedWhenUserDoesNotHaveSufficientReputation() {
            given(questionService.reopenVote(UserId.validated(CLOSE_VOTER_ID), QuestionId.validated(QUESTION_ID)))
                    .willThrow(InsufficientReputationException.class);

            assertThat(questionController.reopenVote(CLOSE_VOTER_ID, QUESTION_ID).getStatusCode())
                    .isEqualTo(HttpStatus.UNAUTHORIZED);
        }
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
        question.setAnswerCount(1);
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

package org.npathai.kata.acceptance.question.dsl;

import org.npathai.kata.acceptance.base.testview.Page;
import org.npathai.kata.acceptance.question.testview.*;
import org.npathai.kata.acceptance.tag.testview.Tag;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class QuestionDsl {
    private static final String QUESTION_BASE_URL = "/api/v1/q";
    private static final String RECENT_QUESTIONS_URL = QUESTION_BASE_URL + "/recent";
    private static final String GET_QUESTION_URL_TEMPLATE = QUESTION_BASE_URL + "/%s";

    private final TestRestTemplate restTemplate;

    public QuestionDsl(TestRestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public PostQuestionCommand aQuestion() {
        return new PostQuestionCommand();
    }

    public class PostQuestionCommand {
        private final CreateQuestionRequest request = new CreateQuestionRequest();
        private String userId;

        public PostQuestionCommand byUser(String userId) {
            this.userId = userId;
            return this;
        }

        public PostQuestionCommand withTitle(String title) {
            request.setTitle(title);
            return this;
        }

        public PostQuestionCommand withBody(String body) {
            request.setBody(body);
            return this;
        }

        public PostQuestionCommand withTags(List<String> tags) {
            request.setTags(tags);
            return this;
        }

        public Question exec() {
            HttpHeaders headers = new HttpHeaders();
            headers.add("userId", userId);

            HttpEntity<CreateQuestionRequest> httpRequest = new HttpEntity<>(request, headers);
            ResponseEntity<Question> response = restTemplate.exchange(QUESTION_BASE_URL, HttpMethod.POST,
                    httpRequest, new ParameterizedTypeReference<>() {});

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody()).satisfies(question -> {
                assertThat(question.getAuthorId()).isEqualTo(userId);
                assertThat(question.getTitle()).isEqualTo(request.getTitle());
                assertThat(question.getBody()).isEqualTo(request.getBody());
                assertThat(question.getTags()).map(Tag::getName)
                        .containsExactlyInAnyOrderElementsOf(request.getTags());
            });
            return response.getBody();
        }
    }

    public RecentQuestionsCommand recent() {
        return new RecentQuestionsCommand();
    }

    public class RecentQuestionsCommand {

        public Page<Question> exec() {
            HttpEntity<Void> httpRequest = new HttpEntity<>(null);

            ResponseEntity<Page<Question>> response = restTemplate.exchange(
                    RECENT_QUESTIONS_URL, HttpMethod.GET,
                    httpRequest, new ParameterizedTypeReference<>() {});

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();

            return response.getBody();
        }
    }

    public GetQuestionCommand view(String id) {
        return new GetQuestionCommand(id);
    }

    public class GetQuestionCommand {

        private final String questionId;

        public GetQuestionCommand(String questionId) {
            this.questionId = questionId;
        }

        public QuestionWithAnswers exec() {
            ResponseEntity<QuestionWithAnswers> response = execReturningResponseEntity();

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();

            return response.getBody();
        }

        public ResponseEntity<QuestionWithAnswers> execReturningResponseEntity() {
            HttpEntity<Void> httpRequest = new HttpEntity<>(null);

            return restTemplate.exchange(
                    String.format(GET_QUESTION_URL_TEMPLATE, questionId), HttpMethod.GET,
                    httpRequest, new ParameterizedTypeReference<>() {});
        }
    }

    public PostAnswerCommand anAnswer() {
        return new PostAnswerCommand();
    }

    public class PostAnswerCommand {

        private String questionId;
        private PostAnswerRequest postAnswerRequest = new PostAnswerRequest();
        private String userId;

        public PostAnswerCommand byUser(String userId) {
            this.userId = userId;
            return this;
        }

        public PostAnswerCommand onQuestion(String questionId) {
            this.questionId = questionId;
            return this;
        }

        public PostAnswerCommand withBody(String body) {
            postAnswerRequest.setBody(body);
            return this;
        }

        public Answer exec() {
            HttpHeaders headers = new HttpHeaders();
            headers.add("userId", userId);

            HttpEntity<PostAnswerRequest> request = new HttpEntity<>(postAnswerRequest, headers);

            ResponseEntity<Answer> response = restTemplate.exchange(QUESTION_BASE_URL + "/" + questionId + "/a", HttpMethod.POST,
                    request, new ParameterizedTypeReference<>() {});

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody()).isNotNull()
                .satisfies(answer -> {
                    assertThat(answer.getId()).isNotNull();
                    assertThat(answer.getAuthorId()).isEqualTo(userId);
                    assertThat(answer.getQuestionId()).isEqualTo(questionId);
                    assertThat(answer.getBody()).isEqualTo(postAnswerRequest.getBody());
                });

            return response.getBody();
        }
    }

}

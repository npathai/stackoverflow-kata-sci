package org.npathai.kata.acceptance.question.dsl;

import org.npathai.kata.acceptance.base.testview.Page;
import org.npathai.kata.acceptance.question.testview.CreateQuestionRequest;
import org.npathai.kata.acceptance.question.testview.CreateQuestionResponse;
import org.npathai.kata.acceptance.question.testview.Question;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class QuestionDsl {
    private static final String QUESTION_BASE_URL = "/api/v1/q";
    private static final String RECENT_QUESTIONS_PAGED_URL_TEMPLATE = QUESTION_BASE_URL + "/recent?page=%d&size=%d";

    private final TestRestTemplate restTemplate;

    public QuestionDsl(TestRestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public CreateQuestionCommand create() {
        return new CreateQuestionCommand();
    }

    public class CreateQuestionCommand {
        private final CreateQuestionRequest request = new CreateQuestionRequest();

        public CreateQuestionCommand byUser(String userId) {
            request.setUserId(userId);
            return this;
        }

        public CreateQuestionCommand withTitle(String title) {
            request.setTitle(title);
            return this;
        }

        public CreateQuestionCommand withBody(String body) {
            request.setBody(body);
            return this;
        }

        public CreateQuestionCommand withTags(List<String> tags) {
            request.setTags(tags);
            return this;
        }

        public Question exec() {
            HttpEntity<CreateQuestionRequest> httpRequest = new HttpEntity<>(request);

            ResponseEntity<Question> response = restTemplate.exchange(QUESTION_BASE_URL, HttpMethod.POST,
                    httpRequest, new ParameterizedTypeReference<>() {});

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody()).isNotNull();

            return response.getBody();
        }
    }

    public RecentQuestionsCommand recent() {
        return new RecentQuestionsCommand();
    }

    public class RecentQuestionsCommand {

        private int pageNo;
        private int pageSize;

        public RecentQuestionsCommand page(int pageNo) {
            this.pageNo = pageNo;
            return this;
        }

        public RecentQuestionsCommand perPage(int pageSize) {
            this.pageSize = pageSize;
            return this;
        }

        public Page<Question> exec() {
            HttpEntity<Void> httpRequest = new HttpEntity<>(null);

            ResponseEntity<Page<Question>> response = restTemplate.exchange(
                    String.format(RECENT_QUESTIONS_PAGED_URL_TEMPLATE, pageNo, pageSize), HttpMethod.POST,
                    httpRequest, new ParameterizedTypeReference<>() {});

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody()).isNotNull();

            return response.getBody();
        }
    }
}

package org.npathai.kata.acceptance.question.dsl;

import org.npathai.kata.acceptance.base.testview.Page;
import org.npathai.kata.acceptance.question.testview.CreateQuestionRequest;
import org.npathai.kata.acceptance.question.testview.Question;
import org.npathai.kata.acceptance.tag.testview.Tag;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class QuestionDsl {
    private static final String QUESTION_BASE_URL = "/api/v1/q";
    private static final String RECENT_QUESTIONS_URL = QUESTION_BASE_URL + "/recent";

    private final TestRestTemplate restTemplate;

    public QuestionDsl(TestRestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public CreateQuestionCommand post() {
        return new CreateQuestionCommand();
    }

    public class CreateQuestionCommand {
        private final CreateQuestionRequest request = new CreateQuestionRequest();
        private String userId;

        public CreateQuestionCommand byUser(String userId) {
            this.userId = userId;
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
                    RECENT_QUESTIONS_URL, HttpMethod.POST,
                    httpRequest, new ParameterizedTypeReference<>() {});

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody()).isNotNull();

            return response.getBody();
        }
    }
}

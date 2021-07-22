package org.npathai.kata.acceptance.question.dsl;

import org.npathai.kata.acceptance.base.testview.Page;
import org.npathai.kata.acceptance.question.testview.CloseVoteSummary;
import org.npathai.kata.acceptance.question.testview.CreateQuestionRequest;
import org.npathai.kata.acceptance.question.testview.Question;
import org.npathai.kata.acceptance.question.testview.QuestionWithAnswers;
import org.npathai.kata.acceptance.vote.testview.Score;
import org.npathai.kata.acceptance.vote.testview.VoteRequest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;

import java.util.List;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

public class QuestionDsl {
    static final String QUESTION_BASE_URL = "/api/v1/q";
    static final String RECENT_QUESTIONS_URL = QUESTION_BASE_URL + "/recent";
    static final String GET_QUESTION_URL_TEMPLATE = QUESTION_BASE_URL + "/%s";

    private final TestRestTemplate restTemplate;

    public QuestionDsl(TestRestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public PostQuestionCommand aQuestion() {
        return new PostQuestionCommand();
    }

    public class PostQuestionCommand {
        private final CreateQuestionRequest request = new CreateQuestionRequest();
        private final Random random = new Random();

        private String userId;

        PostQuestionCommand() {
            request.setTitle("A title: " + random.nextInt());
            request.setBody("A body: " + random.nextInt());
            request.setTags(List.of("java", "kata"));
        }

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
            ResponseEntity<Question> response = execReturningResponseEntity();
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody()).isNotNull();
            return response.getBody();
        }

        public ResponseEntity<Question> execReturningResponseEntity() {
            HttpHeaders headers = new HttpHeaders();
            headers.add("userId", userId);

            HttpEntity<CreateQuestionRequest> httpRequest = new HttpEntity<>(request, headers);
            return restTemplate.exchange(QUESTION_BASE_URL, HttpMethod.POST,
                    httpRequest, new ParameterizedTypeReference<>() {});
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

    public GetQuestionCommand getQuestionById(String id) {
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

    public VoteCommand aVote(String type) {
        return new VoteCommand(type);
    }

    public VoteCommand anUpVote() {
        return new VoteCommand("up");
    }

    public VoteCommand aDownVote() {
        return new VoteCommand("down");
    }

    public class VoteCommand {
        private final VoteRequest voteRequest = new VoteRequest();
        private String questionId;
        private String userId;

        public VoteCommand(String type) {
            voteRequest.setType(type);
        }

        public VoteCommand byUser(String userId) {
            this.userId = userId;
            return this;
        }

        public VoteCommand onQuestion(String questionId) {
            this.questionId = questionId;
            return this;
        }

        public Score exec() {
            ResponseEntity<Score> response = execReturningResponseEntity();

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();

            return response.getBody();
        }

        public ResponseEntity<Score> execReturningResponseEntity() {
            HttpHeaders headers = new HttpHeaders();
            headers.add("userId", userId);

            HttpEntity<VoteRequest> request = new HttpEntity<>(voteRequest, headers);

            return restTemplate.exchange(QUESTION_BASE_URL + "/" + questionId + "/votes", HttpMethod.POST,
                    request, new ParameterizedTypeReference<>() {});
        }
    }

    public CancelVoteCommand cancelVote() {
        return new CancelVoteCommand();
    }

    public class CancelVoteCommand {

        VoteRequest voteRequest = new VoteRequest();
        private String questionId;
        private String userId;

        public CancelVoteCommand byUser(String userId) {
            this.userId = userId;
            return this;
        }

        public CancelVoteCommand onQuestion(String questionId) {
            this.questionId = questionId;
            return this;
        }

        public Score exec() {
            HttpHeaders headers = new HttpHeaders();
            headers.add("userId", userId);

            HttpEntity<VoteRequest> request = new HttpEntity<>(voteRequest, headers);

            ResponseEntity<Score> response = restTemplate.exchange(QUESTION_BASE_URL + "/" + questionId + "/votes", HttpMethod.DELETE,
                    request, new ParameterizedTypeReference<>() {});

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();

            return response.getBody();
        }
    }

    public CloseVoteCommand aCloseVote() {
        return new CloseVoteCommand();
    }

    public class CloseVoteCommand {

        private String userId;
        private String questionId;

        public CloseVoteCommand byUser(String userId) {
            this.userId = userId;
            return this;
        }

        public CloseVoteCommand onQuestion(String questionId) {
            this.questionId = questionId;
            return this;
        }


        public CloseVoteSummary exec() {
            ResponseEntity<CloseVoteSummary> response = execReturningResponseEntity();

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();

            return response.getBody();
        }

        public ResponseEntity<CloseVoteSummary> execReturningResponseEntity() {
            HttpHeaders headers = new HttpHeaders();
            headers.add("userId", userId);

            HttpEntity<Void> request = new HttpEntity<>(null, headers);

            return restTemplate.exchange(QUESTION_BASE_URL + "/" + questionId + "/close-votes", HttpMethod.POST,
                    request, new ParameterizedTypeReference<>() {});
        }
    }

    public ReopenVoteCommand aReopenVote() {
        return new ReopenVoteCommand();
    }

    public class ReopenVoteCommand {

        private String userId;
        private String questionId;

        public ReopenVoteCommand byUser(String userId) {
            this.userId = userId;
            return this;
        }

        public ReopenVoteCommand onQuestion(String questionId) {
            this.questionId = questionId;
            return this;
        }


        public CloseVoteSummary exec() {
            ResponseEntity<CloseVoteSummary> response = execReturningResponseEntity();
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            return response.getBody();
        }

        public ResponseEntity<CloseVoteSummary> execReturningResponseEntity() {
            HttpHeaders headers = new HttpHeaders();
            headers.add("userId", userId);

            HttpEntity<Void> request = new HttpEntity<>(null, headers);

            return restTemplate.exchange(QUESTION_BASE_URL + "/" + questionId + "/reopen-votes", HttpMethod.POST,
                    request, new ParameterizedTypeReference<>() {});
        }
    }
}

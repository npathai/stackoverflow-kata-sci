package org.npathai.kata.acceptance.question.dsl;

import org.npathai.kata.acceptance.question.testview.Answer;
import org.npathai.kata.acceptance.question.testview.PostAnswerRequest;
import org.npathai.kata.acceptance.vote.testview.Score;
import org.npathai.kata.acceptance.vote.testview.VoteRequest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;

import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;
import static org.npathai.kata.acceptance.question.dsl.QuestionDsl.QUESTION_BASE_URL;

public class AnswerDsl {
    private final TestRestTemplate restTemplate;

    public AnswerDsl(TestRestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public PostAnswerCommand anAnswer() {
        return new PostAnswerCommand();
    }

    public class PostAnswerCommand {

        private final PostAnswerRequest request = new PostAnswerRequest();

        private String questionId;
        private String userId;

        PostAnswerCommand() {
            Random random = new Random();
            request.setBody("An answer body: " + random.nextInt());
        }

        public PostAnswerCommand byUser(String userId) {
            this.userId = userId;
            return this;
        }

        public PostAnswerCommand onQuestion(String questionId) {
            this.questionId = questionId;
            return this;
        }

        public PostAnswerCommand withBody(String body) {
            request.setBody(body);
            return this;
        }

        public Answer exec() {
            ResponseEntity<Answer> response = execReturningResponseEntity();
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            return response.getBody();
        }

        public ResponseEntity<Answer> execReturningResponseEntity() {
            HttpHeaders headers = new HttpHeaders();
            headers.add("userId", userId);

            HttpEntity<PostAnswerRequest> request = new HttpEntity<>(this.request, headers);

            return restTemplate.exchange(QUESTION_BASE_URL + "/" + questionId + "/a", HttpMethod.POST,
                    request, new ParameterizedTypeReference<>() {});
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
        private String answerId;
        private String userId;

        public VoteCommand(String type) {
            voteRequest.setType(type);
        }

        public VoteCommand byUser(String userId) {
            this.userId = userId;
            return this;
        }

        public VoteCommand onAnswer(String questionId, String answerId) {
            this.questionId = questionId;
            this.answerId = answerId;
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

            return restTemplate.exchange(QUESTION_BASE_URL + "/" + questionId + "/a/" + answerId + "/votes", HttpMethod.POST,
                    request, new ParameterizedTypeReference<>() {});
        }
    }

    public CancelVoteCommand cancelVote() {
        return new CancelVoteCommand();
    }

    public class CancelVoteCommand {

        VoteRequest voteRequest = new VoteRequest();
        private String questionId;
        private String answerId;
        private String userId;

        public CancelVoteCommand byUser(String userId) {
            this.userId = userId;
            return this;
        }

        public CancelVoteCommand onAnswer(String questionId, String answerId) {
            this.questionId = questionId;
            this.answerId = answerId;
            return this;
        }

        public Score exec() {
            HttpHeaders headers = new HttpHeaders();
            headers.add("userId", userId);

            HttpEntity<VoteRequest> request = new HttpEntity<>(voteRequest, headers);

            ResponseEntity<Score> response = restTemplate.exchange(QUESTION_BASE_URL + "/" + questionId + "/a/" + answerId + "/votes", HttpMethod.DELETE,
                    request, new ParameterizedTypeReference<>() {});

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();

            return response.getBody();
        }
    }
}

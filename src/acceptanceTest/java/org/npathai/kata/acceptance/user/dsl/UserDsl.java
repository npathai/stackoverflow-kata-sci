package org.npathai.kata.acceptance.user.dsl;

import org.npathai.kata.acceptance.user.testview.CreateUserRequest;
import org.npathai.kata.acceptance.user.testview.User;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

public class UserDsl {
    public static final String USER_BASE_URL = "/api/v1/u";

    private final TestRestTemplate restTemplate;

    public UserDsl(TestRestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public class CreateUserCommand {
        CreateUserRequest request = new CreateUserRequest();
        Random random = new Random();

        CreateUserCommand() {
            request.setUsername("User" + random.nextInt());
            request.setEmail(request.getUsername() + "@domain.com");
        }

        public CreateUserCommand withUsername(String username) {
            request.setUsername(username);
            return this;
        }

        public CreateUserCommand withEmail(String email) {
            request.setEmail(email);
            return this;
        }

        public User exec() {
            ResponseEntity<User> response = execReturningResponseEntity();
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody()).isNotNull();

            return response.getBody();
        }

        public ResponseEntity<User> execReturningResponseEntity() {
            HttpEntity<CreateUserRequest> httpRequest = new HttpEntity<>(request);

            return restTemplate.exchange(USER_BASE_URL, HttpMethod.POST,
                    httpRequest, new ParameterizedTypeReference<>() {});
        }
    }

    public CreateUserCommand registerUser() {
        return new CreateUserCommand();
    }

    public class GetUserByIdCommand {
        private final String userId;

        public GetUserByIdCommand(String userId) {
            this.userId = userId;
        }

        public User exec() {
            HttpEntity<Void> request = new HttpEntity<>(null);

            ResponseEntity<User> response = restTemplate.exchange(USER_BASE_URL + "/" + userId,
                    HttpMethod.GET,
                    request, new ParameterizedTypeReference<>() {});

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();

            return response.getBody();
        }
    }

    public GetUserByIdCommand getUserById(String id) {
        return new GetUserByIdCommand(id);
    }
}

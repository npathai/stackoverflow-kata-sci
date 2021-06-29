package org.npathai.kata.acceptance.user.dsl;

import org.npathai.kata.acceptance.user.testview.CreateUserRequest;
import org.npathai.kata.acceptance.user.testview.User;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

public class UserDsl {
    public static final String USER_BASE_URL = "/api/v1/u";

    private final TestRestTemplate restTemplate;

    public UserDsl(TestRestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public class CreateUserCommand {
        CreateUserRequest request = new CreateUserRequest();

        public CreateUserCommand withUsername(String username) {
            request.setUsername(username);
            return this;
        }

        public CreateUserCommand withEmail(String email) {
            request.setEmail(email);
            return this;
        }

        public User exec() {
            HttpEntity<CreateUserRequest> request = new HttpEntity<>(this.request);

            ResponseEntity<User> response = restTemplate.exchange(USER_BASE_URL, HttpMethod.POST,
                    request, new ParameterizedTypeReference<>() {});

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(response.getBody()).isNotNull();

            return response.getBody();
        }
    }

    public CreateUserCommand create() {
        return new CreateUserCommand();
    }
}

package org.npathai.kata.acceptance.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.npathai.kata.acceptance.base.AcceptanceTest;
import org.npathai.kata.acceptance.base.AcceptanceTestBase;
import org.npathai.kata.acceptance.base.ClearTables;
import org.npathai.kata.acceptance.user.dsl.UserDsl;
import org.npathai.kata.acceptance.user.testview.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Register user feature should")
public class RegisterUserFeatureShould extends AcceptanceTestBase {

    private UserDsl userDsl;
    private User user;

    @BeforeEach
    public void setUp() {
        userDsl = new UserDsl(restTemplate);
        user = userDsl.registerUser()
                .withUsername("jon.skeet")
                .withEmail("jon.skeet@gmail.com")
                .exec();
    }

    @AcceptanceTest
    @DisplayName("create user with provided information")
    public void createUserWithProvidedInformation() {
        assertThat(user.getReputation()).isEqualTo(1);
    }

    @AcceptanceTest
    @DisplayName("new user is starts with reputation of 1")
    public void createUserWithReputationOfOne() {
        assertThat(user.getReputation()).isEqualTo(1);
    }

    @AcceptanceTest
    @DisplayName("returns 400 BAD_REQUEST when request is invalid")
    public void returnBadRequestResponseWhenRequestIsInvalid() {
        ResponseEntity<User> user = userDsl.registerUser()
                .withEmail("invalid")
                .withUsername("")
                .execReturningResponseEntity();

        assertThat(user.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}

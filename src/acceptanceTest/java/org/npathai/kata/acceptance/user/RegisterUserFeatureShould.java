package org.npathai.kata.acceptance.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.npathai.kata.acceptance.base.AcceptanceTestBase;
import org.npathai.kata.acceptance.base.ClearTables;
import org.npathai.kata.acceptance.user.dsl.UserDsl;
import org.npathai.kata.acceptance.user.testview.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

public class RegisterUserFeatureShould extends AcceptanceTestBase {

    private UserDsl userDsl;

    @BeforeEach
    public void setUp() {
        userDsl = new UserDsl(restTemplate);
    }

    @ClearTables
    @Test
    public void createUserWithReputationOfOne() {
        User user = userDsl.registerUser()
                .withUsername("jon.skeet")
                .withEmail("jon.skeet@gmail.com")
                .exec();

        assertThat(user.getReputation()).isEqualTo(1);
    }

    @ClearTables
    @Test
    public void returnBadRequestResponseWhenRequestIsInvalid() {
        ResponseEntity<User> user = userDsl.registerUser()
                .withEmail("invalid")
                .withUsername("")
                .execReturningResponseEntity();

        assertThat(user.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}

package org.npathai.kata.acceptance.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.npathai.kata.acceptance.base.AcceptanceTestBase;
import org.npathai.kata.acceptance.user.dsl.UserDsl;
import org.npathai.kata.acceptance.user.testview.User;

import static org.assertj.core.api.Assertions.assertThat;

public class RegisterUserFeatureShould extends AcceptanceTestBase {

    private UserDsl userDsl;

    @BeforeEach
    public void setUp() {
        userDsl = new UserDsl(restTemplate);
    }

    @Test
    public void createUserWithReputationOfOne() {
        User user = userDsl.create()
                .withUsername("jon.skeet")
                .withEmail("jon.skeet@gmail.com")
                .exec();

        assertThat(user.getReputation()).isEqualTo(1);
    }

}

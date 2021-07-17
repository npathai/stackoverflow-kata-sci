package org.npathai.kata.application.domain.user.dto;

import lombok.SneakyThrows;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.npathai.kata.application.domain.user.UserBuilder.anUser;


class UserTest {

    @ParameterizedTest
    @SneakyThrows
    @ValueSource(ints = {
            6,
            7,
            10
    })
    public void nowAllowUserReputationToGoBelowOne(int delta) {
        int currentReputation = 5;

        User user = anUser()
                .withId("1")
                .withUsername("1")
                .withEmail("user@domain.com")
                .withReputation(currentReputation)
                .build();

        user.decrementReputationBy(delta);

        assertThat(user.getReputation()).isEqualTo(1);
    }
}
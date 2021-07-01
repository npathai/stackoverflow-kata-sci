package org.npathai.kata.application.domain.user;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.npathai.kata.application.api.validation.BadRequestParametersException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserIdShould {

    @Test
    public void throwExceptionWhenIdIsNull() {
        assertThatThrownBy(() -> UserId.validated(null)).isInstanceOf(BadRequestParametersException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "  ",
            "\n",
            "\t",
            "\r"
    })
    public void throwExceptionWhenStringIsBlank() {
        assertThatThrownBy(() -> UserId.validated("  ")).isInstanceOf(BadRequestParametersException.class);
    }
}
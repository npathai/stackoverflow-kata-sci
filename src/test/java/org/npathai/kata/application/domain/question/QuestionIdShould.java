package org.npathai.kata.application.domain.question;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.npathai.kata.application.api.validation.BadRequestParametersException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class QuestionIdShould {

    @Test
    public void throwExceptionWhenIdIsNull() {
        assertThatThrownBy(() -> QuestionId.validated(null)).isInstanceOf(BadRequestParametersException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "  ",
            "\n",
            "\t",
            "\r"
    })
    public void throwExceptionWhenStringIsBlank(String whitespace) {
        assertThatThrownBy(() -> QuestionId.validated(whitespace)).isInstanceOf(BadRequestParametersException.class);
    }

    @Test
    public void returnValidInstanceOfQuestionId() throws BadRequestParametersException {
        assertThat(QuestionId.validated("1").getId()).isEqualTo("1");
    }
}
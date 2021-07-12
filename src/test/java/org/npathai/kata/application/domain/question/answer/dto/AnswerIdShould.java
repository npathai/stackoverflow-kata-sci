package org.npathai.kata.application.domain.question.answer.dto;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.npathai.kata.application.api.validation.BadRequestParametersException;
import org.npathai.kata.application.util.WhitespaceSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AnswerIdShould {

    @Test
    @SneakyThrows
    public void returnValidatedInstanceOfAnswerId() {
        assertThat(AnswerId.validated("valid"))
                .isNotNull()
                .satisfies(id -> {
                    assertThat(id.getId()).isEqualTo("valid");
                });
    }

    @ParameterizedTest
    @WhitespaceSource
    public void throwsExceptionWhenIdIsBlankOrEmpty(String whitespace) {
        assertThatThrownBy(() -> AnswerId.validated(whitespace))
                .isInstanceOf(BadRequestParametersException.class);
    }
}
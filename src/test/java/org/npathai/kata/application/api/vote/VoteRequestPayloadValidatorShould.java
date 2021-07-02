package org.npathai.kata.application.api.vote;

import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.npathai.kata.application.api.validation.BadRequestParametersException;
import org.npathai.kata.application.domain.vote.VoteType;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VoteRequestPayloadValidatorShould {

    private VoteRequestPayloadValidator validator;

    @BeforeEach
    public void setUp() {
        validator = new VoteRequestPayloadValidator();
    }

    @Test
    public void throwExceptionWhenTypeIsInvalid() {
        VoteRequestPayload payload = new VoteRequestPayload();
        payload.setType("invalid");

        assertThatThrownBy(() -> validator.validate(payload)).isInstanceOf(BadRequestParametersException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"up", "down"})
    @SneakyThrows
    public void returnValidRequest(String type) {
        VoteRequestPayload payload = new VoteRequestPayload();
        payload.setType(type);

        assertThat(validator.validate(payload)).satisfies(voteRequest -> {
            assertThat(voteRequest.getType()).isEqualTo(VoteType.from(type));
        });
    }
}
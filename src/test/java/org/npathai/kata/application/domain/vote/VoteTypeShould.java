package org.npathai.kata.application.domain.vote;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VoteTypeShould {

        @ParameterizedTest
        @MethodSource("validVoteTypes")
        public void returnVoteTypeBaseOnValue(String value, VoteType expected) {
            assertThat(VoteType.from(value)).isEqualTo(expected);
        }

        @SuppressWarnings("unused")
        public static Stream<Arguments> validVoteTypes() {
            return Stream.of(
                    Arguments.of("up", VoteType.UP),
                    Arguments.of("down", VoteType.DOWN)
            );
        }

        @ParameterizedTest
        @ValueSource(strings = {"invalid", ""})
        public void throwIllegalArgumentExceptionWhenTypeValIsInvalid(String invalidType) {
            assertThatThrownBy(() -> VoteType.from(invalidType)).isInstanceOf(IllegalArgumentException.class);
        }
}
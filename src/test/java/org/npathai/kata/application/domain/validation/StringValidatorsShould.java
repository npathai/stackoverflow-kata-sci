package org.npathai.kata.application.domain.validation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.npathai.kata.application.api.validation.BadRequestParametersException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StringValidatorsShould {

    StringValidators validators = new StringValidators();

    @Nested
    public class NonNullOrBlankValidatorShould {

        @Nested
        public class ThrowExceptionWhenString {

            @Test
            @DisplayName("is null")
            public void isNull() {
                assertThatThrownBy(() -> validators.nonNullOrBlank(null))
                        .isInstanceOf(BadRequestParametersException.class);
            }

            @ParameterizedTest
            @ValueSource(strings = {
                    "",
                    " ",
                    "\t",
                    "\r",
                    "\n"
            })
            public void isBlank(String whitespace) {
                assertThatThrownBy(() -> validators.nonNullOrBlank(whitespace))
                        .isInstanceOf(BadRequestParametersException.class);
            }
        }

        @Test
        public void notThrowExceptionWhenStringIsValid() throws BadRequestParametersException {
            validators.nonNullOrBlank("Hello World!");
        }
    }
}
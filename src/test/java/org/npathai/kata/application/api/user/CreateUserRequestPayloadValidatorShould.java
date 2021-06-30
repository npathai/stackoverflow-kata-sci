package org.npathai.kata.application.api.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.npathai.kata.application.api.validation.BadRequestParametersException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Create user request payload validator should")
class CreateUserRequestPayloadValidatorShould {

    CreateUserRequestPayloadValidator validator = new CreateUserRequestPayloadValidator();
    private CreateUserRequestPayload payload;

    @BeforeEach
    public void setUp() {
        payload = aValidPayload();
    }

    @Nested
    @DisplayName("throw exception when")
    public class ThrowExceptionWhen {

        @Nested
        @DisplayName("email")
        public class Email {

            @Test
            @DisplayName("is null")
            public void isNull() {
                payload.setEmail(null);

                assertThatThrownBy(() -> validator.validate(payload))
                        .isInstanceOf(BadRequestParametersException.class);
            }

            @ParameterizedTest(name = "is \"{0}\"")
            @ValueSource(strings = {
                    "",
                    "user",
                    "@",
                    "@domain",
            })
            public void isInvalid(String email) {
                payload.setEmail(email);

                assertThatThrownBy(() -> validator.validate(payload))
                        .isInstanceOf(BadRequestParametersException.class);
            }
        }

        @Nested
        public class Username {

            @Test
            @DisplayName("is null")
            public void isNull() {
                payload.setUsername(null);

                assertThatThrownBy(() -> validator.validate(payload))
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
            public void isEmpty(String whitespace) {
                payload.setUsername(whitespace);

                assertThatThrownBy(() -> validator.validate(payload))
                        .isInstanceOf(BadRequestParametersException.class);
            }
        }
    }

    private CreateUserRequestPayload aValidPayload() {
        CreateUserRequestPayload payload = new CreateUserRequestPayload();
        payload.setUsername("username");
        payload.setEmail("email@domain.com");
        return payload;
    }
}
package org.npathai.kata.application.api.validation;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CollectionValidatorsShould {

    CollectionValidators validators = new CollectionValidators();

    @Nested
    public class NonNullOrEmpty {

        @Nested
        public class ThrowExceptionWhenCollection {

            @Test
            public void collectionIsNull() {
                assertThatThrownBy(() -> validators.nonNullOrEmpty(null))
                        .isInstanceOf(BadRequestParametersException.class);
            }
        }

        @Test
        public void notThrowExceptionWhenListIsNonEmpty() throws BadRequestParametersException {
            validators.nonNullOrEmpty(List.of(1));
        }
    }
}
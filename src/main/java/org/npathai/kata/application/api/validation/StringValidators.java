package org.npathai.kata.application.api.validation;

import java.util.Objects;

public class StringValidators {

    public static final Validator<String> NON_NULL_OR_BLANK = val -> {
        if (Objects.isNull(val) || val.isBlank()) {
            throw new BadRequestParametersException();
        }
    };

    public void nonNullOrBlank(String str) throws BadRequestParametersException {
        NON_NULL_OR_BLANK.validate(str);
    }
}

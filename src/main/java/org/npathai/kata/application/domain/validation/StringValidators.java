package org.npathai.kata.application.domain.validation;

import org.npathai.kata.application.api.validation.BadRequestParametersException;

import java.util.Objects;

public class StringValidators {

    public void nonNullOrBlank(String str) throws BadRequestParametersException {
        if (Objects.isNull(str) || str.isBlank()) {
            throw new BadRequestParametersException();
        }
    }
}

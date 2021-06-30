package org.npathai.kata.application.domain.validation;

import org.npathai.kata.application.api.validation.BadRequestParametersException;

import java.util.List;
import java.util.Objects;

public class CollectionValidators {

    public void nonNullOrEmpty(List<?> list) throws BadRequestParametersException {
        if (Objects.isNull(list) || list.isEmpty()) {
            throw new BadRequestParametersException();
        }
    }
}

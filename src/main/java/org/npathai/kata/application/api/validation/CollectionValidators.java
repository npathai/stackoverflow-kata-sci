package org.npathai.kata.application.api.validation;

import java.util.List;
import java.util.Objects;

public class CollectionValidators {

    public void nonNullOrEmpty(List<?> list) throws BadRequestParametersException {
        if (Objects.isNull(list) || list.isEmpty()) {
            throw new BadRequestParametersException();
        }
    }
}

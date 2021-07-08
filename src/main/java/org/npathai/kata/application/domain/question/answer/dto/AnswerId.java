package org.npathai.kata.application.domain.question.answer.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.npathai.kata.application.api.validation.BadRequestParametersException;
import org.npathai.kata.application.api.validation.StringValidators;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AnswerId {
    String id;

    public static AnswerId validated(String id) throws BadRequestParametersException {
        StringValidators.NON_NULL_OR_BLANK.validate(id);
        return new AnswerId(id);
    }
}

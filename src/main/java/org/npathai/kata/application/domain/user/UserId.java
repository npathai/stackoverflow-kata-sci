package org.npathai.kata.application.domain.user;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.npathai.kata.application.api.validation.BadRequestParametersException;
import org.npathai.kata.application.api.validation.StringValidators;
import org.npathai.kata.application.domain.services.ValueObject;

@ValueObject
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode
public class UserId {
    String id;

    public static UserId validated(String id) throws BadRequestParametersException {
        StringValidators.NON_NULL_OR_BLANK.validate(id);
        return new UserId(id);
    }
}

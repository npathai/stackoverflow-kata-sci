package org.npathai.kata.application.api.user;

import org.npathai.kata.application.api.validation.BadRequestParametersException;
import org.npathai.kata.application.domain.user.request.CreateUserRequest;

import java.util.Objects;
import java.util.regex.Pattern;

public class CreateUserRequestPayloadValidator {
    // Not the best of email validation, ideally you would want to offload to already available validator
    private final Pattern emailPattern = Pattern.compile("^(.+)@(.+)$");

    public CreateUserRequest validate(CreateUserRequestPayload request) throws BadRequestParametersException {
        validateEmail(request.getEmail());
        valideUsername(request.getUsername());
        return null;
    }

    private void validateEmail(String email) throws BadRequestParametersException {
        if (Objects.isNull(email)) {
            throw new BadRequestParametersException();
        }
        if (!emailPattern.matcher(email).matches()) {
            throw new BadRequestParametersException();
        }
    }

    private void valideUsername(String username) throws BadRequestParametersException {
        if (Objects.isNull(username) || username.isBlank()) {
            throw new BadRequestParametersException();
        }
    }
}

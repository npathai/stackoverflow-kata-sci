package org.npathai.kata.application.api.question.answer;

import org.npathai.kata.application.api.validation.BadRequestParametersException;
import org.npathai.kata.application.api.validation.StringValidators;
import org.npathai.kata.application.domain.question.answer.request.PostAnswerRequest;

public class PostAnswerRequestPayloadValidator {

    private final StringValidators stringValidators;

    public PostAnswerRequestPayloadValidator(StringValidators stringValidators) {
        this.stringValidators = stringValidators;
    }

    public PostAnswerRequest validate(PostAnswerRequestPayload payload) throws BadRequestParametersException {
        stringValidators.nonNullOrBlank(payload.getBody());

        return PostAnswerRequest.valid(payload.getBody());
    }
}

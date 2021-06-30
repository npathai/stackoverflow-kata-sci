package org.npathai.kata.application.api.question;

import org.npathai.kata.application.api.validation.BadRequestParametersException;
import org.npathai.kata.application.domain.question.request.PostQuestionRequest;
import org.npathai.kata.application.domain.validation.CollectionValidators;
import org.npathai.kata.application.domain.validation.StringValidators;

public class PostQuestionRequestPayloadValidator {

    private final StringValidators stringValidators;
    private final CollectionValidators collectionValidators;

    public PostQuestionRequestPayloadValidator(StringValidators stringValidators,
                                               CollectionValidators collectionValidators) {
        this.stringValidators = stringValidators;
        this.collectionValidators = collectionValidators;
    }

    public PostQuestionRequest validate(PostQuestionRequestPayload payload) throws BadRequestParametersException {
        stringValidators.nonNullOrBlank(payload.getTitle());
        stringValidators.nonNullOrBlank(payload.getBody());
        collectionValidators.nonNullOrEmpty(payload.getTags());
        return null;
    }
}

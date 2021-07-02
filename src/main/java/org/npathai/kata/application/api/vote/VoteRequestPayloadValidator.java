package org.npathai.kata.application.api.vote;

import org.npathai.kata.application.api.validation.BadRequestParametersException;
import org.npathai.kata.application.domain.vote.VoteRequest;
import org.npathai.kata.application.domain.vote.VoteType;

public class VoteRequestPayloadValidator {

    public VoteRequest validate(VoteRequestPayload payload) throws BadRequestParametersException {
        try {
            VoteType voteType = VoteType.from(payload.getType());
            return VoteRequest.valid(voteType);
        } catch (IllegalArgumentException ex) {
            throw new BadRequestParametersException(ex);
        }
    }
}

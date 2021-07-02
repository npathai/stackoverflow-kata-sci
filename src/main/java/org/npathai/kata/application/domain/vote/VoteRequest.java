package org.npathai.kata.application.domain.vote;

public class VoteRequest {

    private final VoteType type;

    private VoteRequest(VoteType type) {
        this.type = type;
    }


    public static VoteRequest valid(VoteType type) {
        return new VoteRequest(type);
    }
}

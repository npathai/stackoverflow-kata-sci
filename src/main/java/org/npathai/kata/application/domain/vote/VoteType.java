package org.npathai.kata.application.domain.vote;

public enum VoteType {
    UP("up"),
    DOWN("down");

    public final String val;

    VoteType(String val) {
        this.val = val;
    }

    public static VoteType from(String type) {
        if (UP.val.equals(type)) {
            return UP;
        } else if (DOWN.val.equals(type)) {
            return DOWN;
        } else {
            throw new IllegalArgumentException();
        }
    }
}

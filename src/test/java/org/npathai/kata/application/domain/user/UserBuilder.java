package org.npathai.kata.application.domain.user;

import org.npathai.kata.application.domain.user.dto.User;

import java.util.Random;

public final class UserBuilder {
    private String id;
    private String username;
    private String email;
    private long reputation;
    private int castUpVotes;
    private int castDownVotes;

    private UserBuilder() {
        int random = new Random().nextInt(Integer.MAX_VALUE);
        id = "User" + random;
        username = id;
        email = username + "@domain.com";
    }

    public static UserBuilder anUser() {
        return new UserBuilder();
    }

    public UserBuilder withId(String id) {
        this.id = id;
        return this;
    }

    public UserBuilder withUsername(String username) {
        this.username = username;
        return this;
    }

    public UserBuilder withEmail(String email) {
        this.email = email;
        return this;
    }

    public UserBuilder withReputation(long reputation) {
        this.reputation = reputation;
        return this;
    }

    public UserBuilder withCastUpVotes(int castUpVotes) {
        this.castUpVotes = castUpVotes;
        return this;
    }

    public UserBuilder withCastDownVotes(int castDownVotes) {
        this.castDownVotes = castDownVotes;
        return this;
    }

    public User build() {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setEmail(email);
        user.setReputation(reputation);
        user.setCastUpVotes(castUpVotes);
        user.setCastDownVotes(castDownVotes);
        return user;
    }
}

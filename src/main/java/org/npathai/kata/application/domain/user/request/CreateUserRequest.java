package org.npathai.kata.application.domain.user.request;

public class CreateUserRequest {

    private final String username;
    private final String email;

    CreateUserRequest(String username, String email) {
        this.username = username;
        this.email = email;
    }

    public static CreateUserRequest valid(String username, String email) {
        return new CreateUserRequest(username, email);
    }
}

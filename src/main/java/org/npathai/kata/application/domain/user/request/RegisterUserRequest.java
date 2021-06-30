package org.npathai.kata.application.domain.user.request;

public class RegisterUserRequest {

    private final String username;
    private final String email;

    RegisterUserRequest(String username, String email) {
        this.username = username;
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public static RegisterUserRequest valid(String username, String email) {
        return new RegisterUserRequest(username, email);
    }
}

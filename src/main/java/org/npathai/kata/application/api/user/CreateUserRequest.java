package org.npathai.kata.application.api.user;

import lombok.Data;

@Data
public class CreateUserRequest {
    private String username;
    private String email;
}

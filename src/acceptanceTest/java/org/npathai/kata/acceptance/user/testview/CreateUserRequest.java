package org.npathai.kata.acceptance.user.testview;

import lombok.Data;

@Data
public class CreateUserRequest {
    private String username;
    private String email;
}

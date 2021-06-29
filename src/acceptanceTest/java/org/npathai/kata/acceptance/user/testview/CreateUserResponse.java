package org.npathai.kata.acceptance.user.testview;

import lombok.Data;

@Data
public class CreateUserResponse {
    private String id;
    private String username;
    private String email;
}

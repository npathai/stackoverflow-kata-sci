package org.npathai.kata.application.api.user;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class RegisterUserRequestPayload {
    private String username;
    private String email;
}

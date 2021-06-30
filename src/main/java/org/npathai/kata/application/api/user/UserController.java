package org.npathai.kata.application.api.user;

import org.npathai.kata.application.domain.UserService;
import org.springframework.http.ResponseEntity;

public class UserController {

    public UserController(UserService userService) {

    }

    public ResponseEntity<PresentableUser> createUser(CreateUserRequest request) {
        throw new UnsupportedOperationException();
    }
}

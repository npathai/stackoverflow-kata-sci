package org.npathai.kata.application.api.user;

import org.npathai.kata.application.api.validation.BadRequestParametersException;
import org.npathai.kata.application.domain.UserService;
import org.npathai.kata.application.domain.user.dto.User;
import org.npathai.kata.application.domain.user.request.CreateUserRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/u")
public class UserController {
    private static final Logger LOG = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;
    private final CreateUserRequestPayloadValidator createUserRequestValidator;

    public UserController(UserService userService, CreateUserRequestPayloadValidator createUserRequestValidator) {
        this.userService = userService;
        this.createUserRequestValidator = createUserRequestValidator;
    }

    @PostMapping
    public ResponseEntity<User> createUser(CreateUserRequestPayload request) {
        try {
            CreateUserRequest validRequest = createUserRequestValidator.validate(request);
            User user = userService.create(validRequest);
            return ResponseEntity.created(null).body(user);
        } catch (BadRequestParametersException ex) {
            LOG.info("Received invalid request: {}", request);
            return ResponseEntity.badRequest().build();
        }
    }
}

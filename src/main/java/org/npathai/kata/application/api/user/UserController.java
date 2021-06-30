package org.npathai.kata.application.api.user;

import org.npathai.kata.application.api.validation.BadRequestParametersException;
import org.npathai.kata.application.domain.user.UserService;
import org.npathai.kata.application.domain.user.dto.User;
import org.npathai.kata.application.domain.user.request.RegisterUserRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/u")
public class UserController {
    private static final Logger LOG = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;
    private final RegisterUserRequestPayloadValidator createUserRequestValidator;

    public UserController(UserService userService, RegisterUserRequestPayloadValidator createUserRequestValidator) {
        this.userService = userService;
        this.createUserRequestValidator = createUserRequestValidator;
    }

    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody RegisterUserRequestPayload request) {
        try {
            RegisterUserRequest validRequest = createUserRequestValidator.validate(request);
            User user = userService.register(validRequest);
            return ResponseEntity.created(null).body(user);
        } catch (BadRequestParametersException ex) {
            LOG.info("Received invalid request: {}", request);
            return ResponseEntity.badRequest().build();
        }
    }
}

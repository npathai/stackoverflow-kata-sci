package org.npathai.kata.application.api.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.npathai.kata.application.api.validation.BadRequestParametersException;
import org.npathai.kata.application.domain.UserService;
import org.npathai.kata.application.domain.user.dto.User;
import org.npathai.kata.application.domain.user.request.CreateUserRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserControllerShould {

    public static final String USER_EMAIL = "jon.skeet@gmail.com";
    public static final String USERNAME = "jon.skeet";
    public static final String USER_ID = "1";
    public static final int USER_REPUTATION = 1;
    private static final CreateUserRequest VALID_REQUEST = CreateUserRequest.valid(USERNAME, USER_EMAIL);

    @Mock
    UserService userService;

    @Mock
    CreateUserRequestPayloadValidator validator;

    @InjectMocks
    UserController userController;

    @BeforeEach
    public void setUp() {

    }

    @Test
    public void returnCreatedUser() throws BadRequestParametersException {
        CreateUserRequestPayload createUserRequestPayload = new CreateUserRequestPayload();
        createUserRequestPayload.setUsername(USERNAME);
        createUserRequestPayload.setEmail(USER_EMAIL);

        User createdUser = aUser();
        given(validator.validate(createUserRequestPayload)).willReturn(VALID_REQUEST);
        given(userService.create(VALID_REQUEST)).willReturn(createdUser);

        ResponseEntity<User> response = userController.createUser(createUserRequestPayload);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).describedAs("Created user returned does not match the expectation")
                .isNotNull()
                .isSameAs(createdUser);
    }

    @Test
    public void returnStatusBadRequestWhenPayloadIsInvalid() throws BadRequestParametersException {
        CreateUserRequestPayload createUserRequestPayload = new CreateUserRequestPayload();
        createUserRequestPayload.setUsername(USERNAME);
        createUserRequestPayload.setEmail(USER_EMAIL);

        given(validator.validate(createUserRequestPayload)).willThrow(new BadRequestParametersException());

        ResponseEntity<User> response = userController.createUser(createUserRequestPayload);

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);

        verifyNoInteractions(userService);
    }

    private User aUser() {
        User user = new User();
        user.setId(USER_ID);
        user.setEmail(USER_EMAIL);
        user.setUsername(USERNAME);
        user.setReputation(USER_REPUTATION);
        return user;
    }
}

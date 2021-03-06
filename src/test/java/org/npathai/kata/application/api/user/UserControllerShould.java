package org.npathai.kata.application.api.user;

import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.npathai.kata.application.api.validation.BadRequestParametersException;
import org.npathai.kata.application.domain.services.UnknownEntityException;
import org.npathai.kata.application.domain.user.UserId;
import org.npathai.kata.application.domain.user.UserService;
import org.npathai.kata.application.domain.user.dto.User;
import org.npathai.kata.application.domain.user.request.RegisterUserRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
public class UserControllerShould {

    public static final String USER_EMAIL = "jon.skeet@gmail.com";
    public static final String USERNAME = "jon.skeet";
    public static final String USER_ID = "1";
    public static final int USER_REPUTATION = 1;
    private static final RegisterUserRequest VALID_REQUEST = RegisterUserRequest.valid(USERNAME, USER_EMAIL);

    @Mock
    UserService userService;

    @Mock
    RegisterUserRequestPayloadValidator validator;

    @InjectMocks
    UserController userController;

    @Test
    public void returnCreatedUser() throws BadRequestParametersException {
        RegisterUserRequestPayload registerUserRequestPayload = aRequestPayload();

        User createdUser = aUser();
        given(validator.validate(registerUserRequestPayload)).willReturn(VALID_REQUEST);
        given(userService.register(VALID_REQUEST)).willReturn(createdUser);

        ResponseEntity<User> response = userController.createUser(registerUserRequestPayload);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).describedAs("Created user returned does not match the expectation")
                .isNotNull()
                .isSameAs(createdUser);
    }

    @Test
    public void returnStatusBadRequestWhenPayloadIsInvalid() throws BadRequestParametersException {
        RegisterUserRequestPayload payload = aRequestPayload();

        given(validator.validate(payload)).willThrow(new BadRequestParametersException());

        ResponseEntity<User> response = userController.createUser(payload);

        assertThat(response.getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);

        verifyNoInteractions(userService);
    }

    @Nested
    public class GetUserByIdShould {

        private User user;

        @BeforeEach
        public void setUp() {
            user = aUser();
        }

        @Test
        @SneakyThrows
        public void returnUser() {
            given(userService.getUserById(UserId.validated(USER_ID))).willReturn(user);

            ResponseEntity<User> response =  userController.getById(USER_ID);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isSameAs(user);
        }

        @Test
        @SneakyThrows
        public void return404NotFoundWhenUserWithIdNotFound() {
            given(userService.getUserById(UserId.validated(USER_ID))).willThrow(UnknownEntityException.class);

            ResponseEntity<User> response =  userController.getById(USER_ID);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

        @Test
        public void return400BadRequestWhenUserIdIsInvalid() {
            ResponseEntity<User> response =  userController.getById("");

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }
    }

    private RegisterUserRequestPayload aRequestPayload() {
        RegisterUserRequestPayload payload = new RegisterUserRequestPayload();
        payload.setUsername(USERNAME);
        payload.setEmail(USER_EMAIL);
        return payload;
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

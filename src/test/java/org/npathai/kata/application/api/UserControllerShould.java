package org.npathai.kata.application.api;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.npathai.kata.application.api.user.CreateUserRequest;
import org.npathai.kata.application.api.user.PresentableUser;
import org.npathai.kata.application.api.user.UserController;
import org.npathai.kata.application.domain.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
public class UserControllerShould {

    @Mock
    UserService userService;

    @InjectMocks
    UserController userController;

    @BeforeEach
    public void setUp() {

    }
    
    @Test
    public void returnCreatedUser() {
        CreateUserRequest createUserRequest = new CreateUserRequest();
        createUserRequest.setUsername("jon.skeet");
        createUserRequest.setEmail("jon.skeet@gmail.com");

        ResponseEntity<PresentableUser> response = userController.createUser(createUserRequest);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
    }
}

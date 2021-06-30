package org.npathai.kata.application.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.npathai.kata.application.domain.services.IdGenerator;
import org.npathai.kata.application.domain.user.UserService;
import org.npathai.kata.application.domain.user.dto.User;
import org.npathai.kata.application.domain.user.persistence.UserRepository;
import org.npathai.kata.application.domain.user.request.RegisterUserRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserServiceShould {
    public static final String USER_EMAIL = "jon.skeet@gmail.com";
    public static final String USERNAME = "jon.skeet";
    public static final String USER_ID = "1";

    private RegisterUserRequest registerUserRequest;
    private User user;

    @Mock
    private UserRepository userRepository;

    @Mock
    private IdGenerator idGenerator;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    public void setUp() {
        registerUserRequest = RegisterUserRequest.valid(USERNAME, USER_EMAIL);
    }

    @Nested
    public class UserRegistrationShould {

        @BeforeEach
        public void setUp() {
            given(idGenerator.get()).willReturn(USER_ID);
            user = userService.register(registerUserRequest);
            assertThat(user).isNotNull();
        }

        @Test
        public void createUserWithGivenDetails() {
            assertThat(user.getEmail()).isEqualTo(USER_EMAIL);
            assertThat(user.getUsername()).isEqualTo(USERNAME);
        }

        @Test
        public void assignIdToUser() {
            assertThat(user.getId()).isEqualTo(USER_ID);
        }

        @Test
        public void createUserInitialReputationOfOne() {
            assertThat(user.getReputation()).isEqualTo(1);
        }

        @Test
        public void saveUserInRepository() {
            verify(userRepository).save(user);
        }
    }
}
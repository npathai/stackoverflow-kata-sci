package org.npathai.kata.application.api.user;

import org.npathai.kata.application.domain.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UserConfiguration {

    @Bean
    public UserService createUserService() {
        return new UserService();
    }

    @Bean
    public CreateUserRequestPayloadValidator createUserRequestPayloadValidator() {
        return new CreateUserRequestPayloadValidator();
    }
}

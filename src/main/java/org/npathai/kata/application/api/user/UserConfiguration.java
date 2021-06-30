package org.npathai.kata.application.api.user;

import org.npathai.kata.application.domain.user.UserService;
import org.npathai.kata.application.domain.services.IdGenerator;
import org.npathai.kata.application.domain.user.persistence.UserRepository;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UserConfiguration {

    private final ApplicationContext applicationContext;

    public UserConfiguration(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Bean
    public UserService createUserService() {
        return new UserService(applicationContext.getBean(UserRepository.class),
                applicationContext.getBean(IdGenerator.class));
    }

    @Bean
    public RegisterUserRequestPayloadValidator createUserRequestPayloadValidator() {
        return new RegisterUserRequestPayloadValidator();
    }
}

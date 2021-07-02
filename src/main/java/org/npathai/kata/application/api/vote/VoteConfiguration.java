package org.npathai.kata.application.api.vote;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class VoteConfiguration {

    @Bean
    public VoteRequestPayloadValidator voteRequestPayloadValidator() {
        return new VoteRequestPayloadValidator();
    }
}

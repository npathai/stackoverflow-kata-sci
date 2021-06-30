package org.npathai.kata.application.api.configuration;

import org.npathai.kata.application.domain.services.IdGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.util.UUID;

@Configuration
public class DomainConfiguration {

    @Bean
    public IdGenerator idGenerator() {
        return () -> UUID.randomUUID().toString();
    }

    @Bean
    public Clock systemClock() {
        return Clock.systemDefaultZone();
    }
}

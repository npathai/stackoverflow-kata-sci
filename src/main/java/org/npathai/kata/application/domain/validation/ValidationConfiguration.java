package org.npathai.kata.application.domain.validation;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ValidationConfiguration {

    @Bean
    public StringValidators stringValidators() {
        return new StringValidators();
    }

    @Bean
    public CollectionValidators collectionValidators() {
        return new CollectionValidators();
    }

}

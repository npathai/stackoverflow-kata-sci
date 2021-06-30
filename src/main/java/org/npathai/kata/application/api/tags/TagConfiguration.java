package org.npathai.kata.application.api.tags;

import org.npathai.kata.application.domain.tag.TagService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TagConfiguration {

    @Bean
    public TagService createTagService() {
        return new TagService();
    }
}

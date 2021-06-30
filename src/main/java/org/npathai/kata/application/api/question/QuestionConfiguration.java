package org.npathai.kata.application.api.question;

import org.npathai.kata.application.domain.question.QuestionService;
import org.npathai.kata.application.domain.question.persistence.QuestionRepository;
import org.npathai.kata.application.domain.services.IdGenerator;
import org.npathai.kata.application.domain.tag.TagService;
import org.npathai.kata.application.domain.validation.CollectionValidators;
import org.npathai.kata.application.domain.validation.StringValidators;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class QuestionConfiguration {

    private final ApplicationContext applicationContext;

    public QuestionConfiguration(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Bean
    public QuestionService createQuestionService() {
        return new QuestionService(
                applicationContext.getBean(TagService.class),
                applicationContext.getBean(QuestionRepository.class),
                applicationContext.getBean(IdGenerator.class),
                applicationContext.getBean(Clock.class)
        );
    }

    @Bean
    public PostQuestionRequestPayloadValidator postQuestionRequestPayloadValidator() {
        return new PostQuestionRequestPayloadValidator(
                applicationContext.getBean(StringValidators.class),
                applicationContext.getBean(CollectionValidators.class)
        );
    }
}

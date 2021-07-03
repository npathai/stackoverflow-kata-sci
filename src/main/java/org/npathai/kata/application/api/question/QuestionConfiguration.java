package org.npathai.kata.application.api.question;

import org.npathai.kata.application.api.question.answer.PostAnswerRequestPayloadValidator;
import org.npathai.kata.application.api.validation.CollectionValidators;
import org.npathai.kata.application.api.validation.StringValidators;
import org.npathai.kata.application.domain.question.QuestionService;
import org.npathai.kata.application.domain.question.answer.persistence.AnswerRepository;
import org.npathai.kata.application.domain.question.persistence.QuestionRepository;
import org.npathai.kata.application.domain.question.usecase.GetRecentQuestionsUseCase;
import org.npathai.kata.application.domain.question.usecase.PostQuestionUseCase;
import org.npathai.kata.application.domain.services.IdGenerator;
import org.npathai.kata.application.domain.tag.persistence.TagRepository;
import org.npathai.kata.application.domain.user.UserService;
import org.npathai.kata.application.domain.vote.VoteRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class QuestionConfiguration {

    @Bean
    public PostQuestionUseCase postQuestionUseCase(TagRepository tagRepository, QuestionRepository questionRepository,
                                                   IdGenerator questionIdGenerator, IdGenerator tagIdGenerator, Clock clock) {
        return new PostQuestionUseCase(questionRepository, tagRepository, questionIdGenerator, tagIdGenerator, clock);
    }

    @Bean
    public GetRecentQuestionsUseCase getRecentQuestionsUseCase(QuestionRepository questionRepository) {
        return new GetRecentQuestionsUseCase(questionRepository);
    }

    @Bean
    public QuestionService createQuestionService(PostQuestionUseCase postQuestionUseCase,
                                                 GetRecentQuestionsUseCase getRecentQuestionsUseCase,
                                                 QuestionRepository questionRepository,
                                                 AnswerRepository answerRepository, UserService userService,
                                                 VoteRepository voteRepository,
                                                 IdGenerator answerIdGenerator,
                                                 IdGenerator voteIdGenerator) {
        return new QuestionService(postQuestionUseCase, getRecentQuestionsUseCase, questionRepository, answerRepository, userService,
                voteRepository, answerIdGenerator, voteIdGenerator);
    }

    @Bean
    public PostQuestionRequestPayloadValidator postQuestionRequestPayloadValidator(StringValidators stringValidators,
                                                                                   CollectionValidators collectionValidators) {
        return new PostQuestionRequestPayloadValidator(stringValidators, collectionValidators);
    }

    @Bean
    public PostAnswerRequestPayloadValidator postAnswerRequestPayloadValidator(StringValidators stringValidators) {
        return new PostAnswerRequestPayloadValidator(stringValidators);
    }
}

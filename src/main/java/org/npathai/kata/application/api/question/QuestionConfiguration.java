package org.npathai.kata.application.api.question;

import org.npathai.kata.application.api.question.answer.PostAnswerRequestPayloadValidator;
import org.npathai.kata.application.api.validation.CollectionValidators;
import org.npathai.kata.application.api.validation.StringValidators;
import org.npathai.kata.application.domain.question.QuestionService;
import org.npathai.kata.application.domain.question.answer.persistence.AnswerRepository;
import org.npathai.kata.application.domain.question.persistence.CloseVoteRepository;
import org.npathai.kata.application.domain.question.persistence.QuestionRepository;
import org.npathai.kata.application.domain.question.persistence.ReopenVoteRepository;
import org.npathai.kata.application.domain.question.usecase.*;
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
    public PostAnswerUseCase postAnswerUseCase(QuestionRepository questionRepository,
                                               AnswerRepository answerRepository,
                                               IdGenerator answerIdGenerator) {
        return new PostAnswerUseCase(questionRepository, answerRepository, answerIdGenerator);
    }

    @Bean
    public GetQuestionUseCase createGetQuestionUseCase(QuestionRepository questionRepository,
                                                       AnswerRepository answerRepository) {
        return new GetQuestionUseCase(questionRepository, answerRepository);
    }

    @Bean
    public QuestionVotingUseCase createQuestionVotingUseCase(QuestionRepository questionRepository, VoteRepository voteRepository, UserService userService, IdGenerator voteIdGenerator) {
        return new QuestionVotingUseCase(questionRepository, voteRepository, userService, voteIdGenerator);
    }

    @Bean
    public QuestionCancelVotingUseCase createQuestionCancelVotingUseCase(QuestionRepository questionRepository,
                                                                         VoteRepository voteRepository,
                                                                         UserService userService) {
        return new QuestionCancelVotingUseCase(questionRepository,
                voteRepository,
                userService
        );
    }

    @Bean
    public AnswerVotingUseCase createAnswerVotingUseCase(AnswerRepository answerRepository,
                                                         VoteRepository voteRepository,
                                                         IdGenerator voteIdGenerator,
                                                         UserService userService) {
        return new AnswerVotingUseCase(answerRepository, voteRepository, voteIdGenerator, userService);
    }

    @Bean
    public AnswerCancelVotingUseCase createAnswerCancelVotingUseCase(AnswerRepository answerRepository,
                                                                     VoteRepository voteRepository,
                                                                     UserService userService) {
        return new AnswerCancelVotingUseCase(answerRepository, voteRepository, userService);
    }

    @Bean
    public QuestionCloseVotingUseCase createQuestionCloseVotingUseCase(QuestionRepository questionRepository,
                                                                       IdGenerator closeVoteIdGenerator,
                                                                       CloseVoteRepository closeVoteRepository,
                                                                       UserService userService,
                                                                       Clock clock) {

        return new QuestionCloseVotingUseCase(questionRepository, closeVoteIdGenerator, closeVoteRepository, clock, userService);
    }

    @Bean
    public QuestionReopenVotingUseCase createQuestionReopenVotingUseCase(QuestionRepository questionRepository,
                                                                         IdGenerator reopenVoteIdGenerator,
                                                                         ReopenVoteRepository reopenVoteRepository,
                                                                         UserService userService,
                                                                         CloseVoteRepository closeVoteRepository) {
        return new QuestionReopenVotingUseCase(questionRepository, reopenVoteIdGenerator,
                reopenVoteRepository, userService, closeVoteRepository);
    }

    @Bean
    public QuestionService createQuestionService(PostQuestionUseCase postQuestionUseCase,
                                                 GetRecentQuestionsUseCase getRecentQuestionsUseCase,
                                                 PostAnswerUseCase postAnswerUseCase,
                                                 GetQuestionUseCase getQuestionUseCase,
                                                 QuestionVotingUseCase questionVotingUseCase,
                                                 QuestionCancelVotingUseCase questionCancelVotingUseCase,
                                                 AnswerVotingUseCase answerVotingUseCase,
                                                 AnswerCancelVotingUseCase answerCancelVotingUseCase,
                                                 QuestionCloseVotingUseCase questionCloseVotingUseCase,
                                                 QuestionReopenVotingUseCase questionReopenVotingUseCase) {

        return new QuestionService(postQuestionUseCase,
                getRecentQuestionsUseCase,
                postAnswerUseCase,
                getQuestionUseCase,
                questionVotingUseCase,
                questionCancelVotingUseCase,
                answerVotingUseCase,
                answerCancelVotingUseCase,
                questionCloseVotingUseCase, questionReopenVotingUseCase);
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

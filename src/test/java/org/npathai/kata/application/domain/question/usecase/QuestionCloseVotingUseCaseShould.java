package org.npathai.kata.application.domain.question.usecase;

import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.npathai.kata.application.domain.question.QuestionBuilder;
import org.npathai.kata.application.domain.question.QuestionId;
import org.npathai.kata.application.domain.question.dto.CloseVote;
import org.npathai.kata.application.domain.question.dto.CloseVoteSummary;
import org.npathai.kata.application.domain.question.dto.Question;
import org.npathai.kata.application.domain.question.persistence.CloseVoteRepository;
import org.npathai.kata.application.domain.question.persistence.QuestionRepository;
import org.npathai.kata.application.domain.services.IdGenerator;
import org.npathai.kata.application.domain.user.InsufficientReputationException;
import org.npathai.kata.application.domain.user.UserBuilder;
import org.npathai.kata.application.domain.user.UserId;
import org.npathai.kata.application.domain.user.UserService;
import org.npathai.kata.application.domain.user.dto.User;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class QuestionCloseVotingUseCaseShould {

    private static final String VOTER_ID_1 = "U1";
    private static final String VOTER_ID_2 = "U2";
    private static final String VOTER_ID_3 = "U3";
    private static final String VOTER_ID_4 = "U4";

    private static final String QUESTION_ID = "Q1";

    @Mock
    CloseVoteRepository closeVoteRepository;

    @Mock
    QuestionRepository questionRepository;
    
    @Mock
    UserService userService;

    @Mock
    IdGenerator closeVoteIdGenerator;

    Clock clock;

    QuestionCloseVotingUseCase useCase;
    Question question;

    @BeforeEach
    @SneakyThrows
    public void setUp() {
        clock = fixedClock();
        useCase = new QuestionCloseVotingUseCase(questionRepository, closeVoteIdGenerator, closeVoteRepository, clock,
                userService);
        question = QuestionBuilder.aQuestion().withId(QUESTION_ID).build();
    }

    private Clock fixedClock() {
        return Clock.fixed(Instant.now(), ZoneId.systemDefault());
    }

    @Test
    @SneakyThrows
    public void recordCloseVote() {
        given(questionRepository.findById(QUESTION_ID)).willReturn(Optional.of(question));
        given(closeVoteIdGenerator.get()).willReturn(UUID.randomUUID().toString());
        given(userService.getUserById(UserId.validated(VOTER_ID_1)))
                .willReturn(UserBuilder.anUser().withReputation(3000).withId(VOTER_ID_1).build());

        CloseVote closeVote = aCloseVote(VOTER_ID_1);

        useCase.closeVote(UserId.validated(VOTER_ID_1), QuestionId.validated(QUESTION_ID));

        verify(closeVoteRepository).save(closeVote);
    }

    @Test
    @SneakyThrows
    public void returnCloseVoteSummary() {
        given(questionRepository.findById(QUESTION_ID)).willReturn(Optional.of(question));
        given(userService.getUserById(UserId.validated(VOTER_ID_2)))
                .willReturn(UserBuilder.anUser().withReputation(3000).build());
        given(userService.getUserById(UserId.validated(VOTER_ID_1)))
                .willReturn(UserBuilder.anUser().withReputation(3000).withId(VOTER_ID_1).build());

        CloseVoteSummary summary1 = useCase.closeVote(UserId.validated(VOTER_ID_1), QuestionId.validated(QUESTION_ID));
        assertThat(summary1.getCastVotes()).isEqualTo(1);
        assertThat(summary1.getRemainingVotes()).isEqualTo(3);

        CloseVote closeVote = aCloseVote(VOTER_ID_1);
        given(closeVoteRepository.findByQuestionId(QUESTION_ID)).willReturn(new ArrayList<>(List.of((closeVote))));

        CloseVoteSummary summary2 = useCase.closeVote(UserId.validated(VOTER_ID_2), QuestionId.validated(QUESTION_ID));
        assertThat(summary2.getCastVotes()).isEqualTo(2);
        assertThat(summary2.getRemainingVotes()).isEqualTo(2);
    }

    @Test
    @SneakyThrows
    public void closesQuestionAfterFourCloseVotes() {
        given(questionRepository.findById(QUESTION_ID)).willReturn(Optional.of(question));
        given(userService.getUserById(UserId.validated(VOTER_ID_4)))
                .willReturn(UserBuilder.anUser().withReputation(3000).build());

        List<CloseVote> pastVotes = new ArrayList<>(List.of((aCloseVote(VOTER_ID_1)),
                aCloseVote(VOTER_ID_2),
                aCloseVote(VOTER_ID_3)));

        given(closeVoteRepository.findByQuestionId(QUESTION_ID)).willReturn(pastVotes);

        CloseVoteSummary closeVoteSummary =
                useCase.closeVote(UserId.validated(VOTER_ID_4), QuestionId.validated(QUESTION_ID));

        assertThat(closeVoteSummary.getCastVotes()).isEqualTo(4);
        assertThat(closeVoteSummary.getRemainingVotes()).isEqualTo(0);
        assertThat(question.getClosedAt()).isEqualTo(clock.millis());
        verify(questionRepository).save(question);
    }
    
    @ParameterizedTest
    @ValueSource(ints = {
            2998,
            2999
    })
    @SneakyThrows
    public void throwsExceptionWhenVoterDoesNotHaveSufficientReputationToVote(int reputation) {
        User insufficientRepVoter = UserBuilder.anUser().withReputation(reputation).build();

        given(userService.getUserById(UserId.validated(insufficientRepVoter.getId())))
                .willReturn(insufficientRepVoter);


        assertThatThrownBy(() -> useCase.closeVote(UserId.validated(insufficientRepVoter.getId()), QuestionId.validated(QUESTION_ID)))
            .isInstanceOf(InsufficientReputationException.class);
    }
    
    private CloseVote aCloseVote(String voterId) {
        CloseVote closeVote = new CloseVote();
        closeVote.setId(closeVoteIdGenerator.get());
        closeVote.setVoterId(voterId);
        closeVote.setQuestionId(QUESTION_ID);
        return closeVote;
    }
}

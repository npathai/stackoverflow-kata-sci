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
import org.npathai.kata.application.domain.question.dto.VoteSummary;
import org.npathai.kata.application.domain.question.dto.Question;
import org.npathai.kata.application.domain.question.dto.ReopenVote;
import org.npathai.kata.application.domain.question.persistence.CloseVoteRepository;
import org.npathai.kata.application.domain.question.persistence.QuestionRepository;
import org.npathai.kata.application.domain.question.persistence.ReopenVoteRepository;
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
public class QuestionReopenVotingUseCaseShould {
    private static final String VOTER_ID_1 = "U1";
    private static final String VOTER_ID_2 = "U2";
    private static final String VOTER_ID_3 = "U3";
    private static final String VOTER_ID_4 = "U4";

    private static final String QUESTION_ID = "Q1";
    public static final int CLOSED_AT_TIMESTAMP = 11234556;

    @Mock
    CloseVoteRepository closeVoteRepository;

    @Mock
    ReopenVoteRepository reopenVoteRepository;

    @Mock
    QuestionRepository questionRepository;

    @Mock
    UserService userService;

    @Mock
    IdGenerator reopenVoteIdGenerator;

    Clock clock;

    QuestionReopenVotingUseCase useCase;
    Question closedQuestion;

    @BeforeEach
    @SneakyThrows
    public void setUp() {
        clock = fixedClock();
        useCase = new QuestionReopenVotingUseCase(questionRepository, reopenVoteIdGenerator, reopenVoteRepository,
                userService, closeVoteRepository);
        closedQuestion = QuestionBuilder.aQuestion().withId(QUESTION_ID).withClosedAt(CLOSED_AT_TIMESTAMP).build();
    }

    private Clock fixedClock() {
        return Clock.fixed(Instant.now(), ZoneId.systemDefault());
    }

    @Test
    @SneakyThrows
    public void recordReopenVote() {
        given(reopenVoteIdGenerator.get()).willReturn(UUID.randomUUID().toString());
        given(questionRepository.findById(QUESTION_ID)).willReturn(Optional.of(closedQuestion));
        given(reopenVoteIdGenerator.get()).willReturn(UUID.randomUUID().toString());
        given(userService.getUserById(UserId.validated(VOTER_ID_1)))
                .willReturn(UserBuilder.anUser().withReputation(3000).withId(VOTER_ID_1).build());

        ReopenVote reopenVote = aReopenVote(VOTER_ID_1);

        useCase.reopenVote(UserId.validated(VOTER_ID_1), QuestionId.validated(QUESTION_ID));

        verify(reopenVoteRepository).save(reopenVote);
    }

    @Test
    @SneakyThrows
    public void returnCloseVoteSummary() {
        given(reopenVoteIdGenerator.get()).willReturn(UUID.randomUUID().toString());
        given(questionRepository.findById(QUESTION_ID)).willReturn(Optional.of(closedQuestion));
        given(reopenVoteIdGenerator.get()).willReturn(UUID.randomUUID().toString());
        given(userService.getUserById(UserId.validated(VOTER_ID_1)))
                .willReturn(UserBuilder.anUser().withReputation(3000).withId(VOTER_ID_1).build());
        given(userService.getUserById(UserId.validated(VOTER_ID_2)))
                .willReturn(UserBuilder.anUser().withReputation(3000).withId(VOTER_ID_2).build());

        VoteSummary voteSummary = useCase.reopenVote(UserId.validated(VOTER_ID_1), QuestionId.validated(QUESTION_ID));
        assertThat(voteSummary.getCastVotes()).isEqualTo(1);
        assertThat(voteSummary.getRemainingVotes()).isEqualTo(3);

        ReopenVote reopenVote = aReopenVote(VOTER_ID_1);
        given(reopenVoteRepository.findByQuestionId(QUESTION_ID)).willReturn(new ArrayList<>(List.of(reopenVote)));

        VoteSummary voteSummary2 = useCase.reopenVote(UserId.validated(VOTER_ID_2), QuestionId.validated(QUESTION_ID));
        assertThat(voteSummary2.getCastVotes()).isEqualTo(2);
        assertThat(voteSummary2.getRemainingVotes()).isEqualTo(2);
    }

    @Test
    @SneakyThrows
    public void reopenQuestionAfterFourReopenVotes() {
        given(reopenVoteIdGenerator.get()).willReturn(UUID.randomUUID().toString());
        given(questionRepository.findById(QUESTION_ID)).willReturn(Optional.of(closedQuestion));
        given(userService.getUserById(UserId.validated(VOTER_ID_4)))
                .willReturn(UserBuilder.anUser().withReputation(3000).build());

        List<ReopenVote> pastVotes = new ArrayList<>(List.of((aReopenVote(VOTER_ID_1)),
                aReopenVote(VOTER_ID_2),
                aReopenVote(VOTER_ID_3)));

        given(reopenVoteRepository.findByQuestionId(QUESTION_ID)).willReturn(pastVotes);

        VoteSummary voteSummary =
                useCase.reopenVote(UserId.validated(VOTER_ID_4), QuestionId.validated(QUESTION_ID));

        assertThat(voteSummary.getCastVotes()).isEqualTo(4);
        assertThat(voteSummary.getRemainingVotes()).isEqualTo(0);
        assertThat(closedQuestion.getClosedAt()).isNull();
        verify(questionRepository).save(closedQuestion);
    }

//    @Test
//    @SneakyThrows
//    public void deleteCloseAndReopenVotesAfterQuestionIsReopened() {
//        given(reopenVoteIdGenerator.get()).willReturn(UUID.randomUUID().toString());
//        given(questionRepository.findById(QUESTION_ID)).willReturn(Optional.of(closedQuestion));
//        given(userService.getUserById(UserId.validated(VOTER_ID_4)))
//                .willReturn(UserBuilder.anUser().withId(VOTER_ID_4).withReputation(3000).build());
//
//        List<ReopenVote> pastVotes = new ArrayList<>(List.of((aReopenVote(VOTER_ID_1)),
//                aReopenVote(VOTER_ID_2),
//                aReopenVote(VOTER_ID_3)));
//
//        List<ReopenVote> expectedDeletedVotes = new ArrayList<>(List.of((aReopenVote(VOTER_ID_1)),
//                aReopenVote(VOTER_ID_2),
//                aReopenVote(VOTER_ID_3)));
//
//        given(reopenVoteRepository.findByQuestionId(QUESTION_ID)).willReturn(pastVotes);
//
//        useCase.reopenVote(UserId.validated(VOTER_ID_4), QuestionId.validated(QUESTION_ID));
//
//        verify(closeVoteRepository).deleteByQuestionId(QUESTION_ID);
//        verify(reopenVoteRepository).deleteAll(expectedDeletedVotes);
//    }

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


        assertThatThrownBy(() -> useCase.reopenVote(UserId.validated(insufficientRepVoter.getId()), QuestionId.validated(QUESTION_ID)))
                .isInstanceOf(InsufficientReputationException.class);
    }

    private ReopenVote aReopenVote(String voterId) {
        ReopenVote reopenVote = new ReopenVote();
        reopenVote.setId(reopenVoteIdGenerator.get());
        reopenVote.setVoterId(voterId);
        reopenVote.setQuestionId(QUESTION_ID);
        return reopenVote;
    }
}

package org.npathai.kata.application.domain.question.usecase;

import lombok.SneakyThrows;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.npathai.kata.application.domain.ImpermissibleOperationException;
import org.npathai.kata.application.domain.question.QuestionBuilder;
import org.npathai.kata.application.domain.question.QuestionId;
import org.npathai.kata.application.domain.question.dto.Question;
import org.npathai.kata.application.domain.question.persistence.QuestionRepository;
import org.npathai.kata.application.domain.services.IdGenerator;
import org.npathai.kata.application.domain.services.UnknownEntityException;
import org.npathai.kata.application.domain.user.InsufficientReputationException;
import org.npathai.kata.application.domain.user.UserId;
import org.npathai.kata.application.domain.user.UserService;
import org.npathai.kata.application.domain.user.dto.User;
import org.npathai.kata.application.domain.vote.VoteRepository;
import org.npathai.kata.application.domain.vote.VoteRequest;
import org.npathai.kata.application.domain.vote.VoteType;
import org.npathai.kata.application.domain.vote.dto.Score;
import org.npathai.kata.application.domain.vote.dto.Vote;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.npathai.kata.application.domain.user.UserBuilder.anUser;

@ExtendWith(MockitoExtension.class)
public class QuestionVotingUseCaseShould {
    private static final long AUTHOR_REPUTATION = 1000;
    private static final String USER_ID = "U1";
    private static final String QUESTION_ID = "Q1";

    Question question;
    User author;
    User voter;

    @Mock
    UserService userService;

    @Mock
    VoteRepository voteRepository;

    @Mock
    QuestionRepository questionRepository;

    @Mock
    IdGenerator voteIdGenerator;

    @InjectMocks
    QuestionVotingUseCase useCase;

    @BeforeEach
    @SneakyThrows
    public void setUp() {
        author = anUser()
                .withReputation(AUTHOR_REPUTATION)
                .build();

        voter = anUser()
                .withReputation(1000)
                .withCastDownVotes(10)
                .withCastUpVotes(10)
                .build();

        question = QuestionBuilder.aQuestion().withId(QUESTION_ID)
                .withAuthorId(author.getId())
                .withScore(10).build();
    }

    @Nested
    public class OnUpVote {

        private Score score;
        private VoteRequest voteRequest;
        private final String voteId = "v1";

        @BeforeEach
        @SneakyThrows
        public void setUp() {
            given(userService.getUserById(UserId.validated(voter.getId()))).willReturn(voter);
            given(userService.getUserById(UserId.validated(author.getId()))).willReturn(author);
            given(questionRepository.findById(question.getId())).willReturn(Optional.of(question));
            given(voteIdGenerator.get()).willReturn(voteId);
            voteRequest = VoteRequest.valid(VoteType.UP);

            score = castUpVote(voter, QUESTION_ID);
        }

        @Test
        @SneakyThrows
        public void returnIncrementedScore() {
            assertThat(score.getScore()).isEqualTo(11);
        }

        @Test
        @SneakyThrows
        public void incrementUpVoteCastCountOfVoter() {
            assertThat(voter.getCastUpVotes()).isEqualTo(11);
            verify(userService).update(voter);
        }

        @Test
        @SneakyThrows
        public void incrementsScoreOfQuestion() {
            assertThat(question.getScore()).isEqualTo(11);
            verify(questionRepository).save(question);
        }

        @Test
        @SneakyThrows
        public void incrementsAuthorReputation() {
            assertThat(author.getReputation()).isEqualTo(AUTHOR_REPUTATION + 10);
            verify(userService).update(author);
        }

        @Test
        public void savesVoteInRepository() {
            Vote expectedVote = new Vote();
            expectedVote.setId(voteId);
            expectedVote.setQuestionId(question.getId());
            expectedVote.setVoterId(voter.getId());
            expectedVote.setType("up");

            verify(voteRepository).save(expectedVote);
        }

        @Test
        @SneakyThrows
        public void doesNotAllowAuthorToUpVoteOnOwnQuestion() {
            assertThatThrownBy(() -> castUpVote(author, question.getId())).isInstanceOf(ImpermissibleOperationException.class);
        }

        @SneakyThrows
        private Score castUpVote(User voter, String id) {
            return useCase.voteQuestion(UserId.validated(voter.getId()),
                    QuestionId.validated(id), voteRequest);
        }

        @ParameterizedTest
        @SneakyThrows
        @ValueSource(ints = {
                15,
                16,
                100
        })
        public void allowsUserToVoteAfterSufficientReputation(int reputation) {
            voter.setReputation(reputation);

            Assertions.assertThat(useCase.voteQuestion(UserId.validated(voter.getId()),
                    QuestionId.validated(question.getId()), voteRequest).getScore()).isEqualTo(12);
        }

        @ParameterizedTest
        @SneakyThrows
        @ValueSource(ints = {
                1,
                13,
                14,
        })
        public void doesNotAllowUserWithInsufficientReputationToVote(int reputation) {
            voter.setReputation(reputation);

            assertThatThrownBy(() -> Assertions.assertThat(useCase.voteQuestion(UserId.validated(voter.getId()),
                    QuestionId.validated(question.getId()), voteRequest)).isInstanceOf(InsufficientReputationException.class));
        }
    }

    @Nested
    public class CancelUpVote {

        private Score cancelledScore;
        private Vote vote;

        @BeforeEach
        @SneakyThrows
        public void setUp() {
            vote = new Vote();
            vote.setId("1");
            vote.setVoterId(voter.getId());
            vote.setQuestionId(question.getId());
            vote.setType("up");
            given(voteRepository.findByQuestionIdAndVoterId(question.getId(), voter.getId())).willReturn(vote);

            given(userService.getUserById(UserId.validated(voter.getId()))).willReturn(voter);
            given(userService.getUserById(UserId.validated(author.getId()))).willReturn(author);

            given(questionRepository.findById(question.getId())).willReturn(Optional.of(question));

            cancelledScore = useCase.cancelVote(UserId.validated(voter.getId()), QuestionId.validated(question.getId()));
        }

        @Test
        @SneakyThrows
        public void returnDecreasedScoreWhenVoteIsCancelled() {
            assertThat(cancelledScore.getScore()).isEqualTo(9);
        }

        @Test
        public void decrementQuestionScoreAndUpdate() {
            assertThat(question.getScore()).isEqualTo(9);
            verify(questionRepository).save(question);
        }

        @Test
        public void decrementVoterCastUpVotesAndUpdate() {
            assertThat(voter.getCastUpVotes()).isEqualTo(9);
            verify(userService).update(voter);
        }

        @Test
        public void decrementAuthorReputationAndUpdate() {
            assertThat(author.getReputation()).isEqualTo(AUTHOR_REPUTATION - 10);
            verify(userService).update(author);
        }

        @Test
        @SneakyThrows
        public void deleteVote() {
            verify(voteRepository).delete(vote);
        }
    }

    @Nested
    public class OnDownVote {

        private Score score;
        private VoteRequest voteRequest;
        private final String voteId = "v1";

        @BeforeEach
        @SneakyThrows
        public void setUp() {
            given(userService.getUserById(UserId.validated(voter.getId()))).willReturn(voter);
            given(userService.getUserById(UserId.validated(author.getId()))).willReturn(author);
            given(questionRepository.findById(question.getId())).willReturn(Optional.of(question));
            given(voteIdGenerator.get()).willReturn(voteId);

            voteRequest = VoteRequest.valid(VoteType.DOWN);

            score = useCase.voteQuestion(UserId.validated(voter.getId()), QuestionId.validated(QUESTION_ID),
                    voteRequest);
        }

        @Test
        @SneakyThrows
        public void returnsDecrementedScoreOfQuestion() {
            assertThat(score.getScore()).isEqualTo(9);
        }

        @Test
        @SneakyThrows
        public void incrementsCountOfDownVotesCastOfVoter() {
            assertThat(voter.getCastDownVotes()).isEqualTo(11);
            verify(userService).update(voter);
        }

        @Test
        @SneakyThrows
        public void saveQuestionWithUpdatedScore() {
            assertThat(question.getScore()).isEqualTo(9);
            verify(questionRepository).save(question);
        }

        @Test
        @SneakyThrows
        public void decrementsAuthorReputation() {
            assertThat(author.getReputation()).isEqualTo(AUTHOR_REPUTATION - 5);
            verify(userService).update(author);
        }

        @Test
        public void savesVoteInRepository() {
            Vote expectedVote = new Vote();
            expectedVote.setId(voteId);
            expectedVote.setQuestionId(question.getId());
            expectedVote.setVoterId(voter.getId());
            expectedVote.setType("down");

            verify(voteRepository).save(expectedVote);
        }

        @Test
        @SneakyThrows
        public void doesNotAllowAuthorToDownVoteOnOwnQuestion() {
            assertThatThrownBy(() -> useCase.voteQuestion(UserId.validated(author.getId()),
                    QuestionId.validated(question.getId()), voteRequest)).isInstanceOf(ImpermissibleOperationException.class);
        }

        @ParameterizedTest
        @SneakyThrows
        @ValueSource(ints = {
                125,
                126,
                1000
        })
        public void allowsUserToVoteAfterSufficientReputation(int reputation) {
            voter.setReputation(reputation);

            Assertions.assertThat(useCase.voteQuestion(UserId.validated(voter.getId()),
                    QuestionId.validated(question.getId()), voteRequest).getScore()).isEqualTo(8);
        }

        @ParameterizedTest
        @SneakyThrows
        @ValueSource(ints = {
                1,
                100,
                124
        })
        public void doesNotAllowUserWithInsufficientReputationToVote(int reputation) {
            voter.setReputation(reputation);

            assertThatThrownBy(() -> Assertions.assertThat(useCase.voteQuestion(UserId.validated(voter.getId()),
                    QuestionId.validated(question.getId()), voteRequest)).isInstanceOf(InsufficientReputationException.class));
        }

    }


    @Nested
    public class CancelDownVote {

        private Score cancelledScore;
        private Vote vote;

        @BeforeEach
        @SneakyThrows
        public void setUp() {
            vote = new Vote();
            vote.setId("1");
            vote.setVoterId(voter.getId());
            vote.setQuestionId(question.getId());
            vote.setType("down");
            given(voteRepository.findByQuestionIdAndVoterId(question.getId(), voter.getId())).willReturn(vote);

            given(userService.getUserById(UserId.validated(voter.getId()))).willReturn(voter);
            given(userService.getUserById(UserId.validated(author.getId()))).willReturn(author);

            given(questionRepository.findById(question.getId())).willReturn(Optional.of(question));

            cancelledScore = useCase.cancelVote(UserId.validated(voter.getId()), QuestionId.validated(question.getId()));
        }

        @Test
        @SneakyThrows
        public void returnIncrementedQuestionScore() {
            assertThat(cancelledScore.getScore()).isEqualTo(11);
        }

        @Test
        public void incrementQuestionScoreAndUpdate() {
            assertThat(question.getScore()).isEqualTo(11);
            verify(questionRepository).save(question);
        }

        @Test
        public void decrementVoterCastDownVotesAndUpdate() {
            assertThat(voter.getCastDownVotes()).isEqualTo(9);
            verify(userService).update(voter);
        }

        @Test
        public void incrementAuthorReputationAndUpdate() {
            assertThat(author.getReputation()).isEqualTo(AUTHOR_REPUTATION + 5);
            verify(userService).update(author);
        }

        @Test
        @SneakyThrows
        public void deleteVote() {
            verify(voteRepository).delete(vote);
        }
    }

    @Test
    @SneakyThrows
    public void throwsUnknownEntityExceptionWhenQuestionWithIdNotFound() {
        VoteRequest voteRequest = VoteRequest.valid(VoteType.DOWN);

        assertThatThrownBy(() -> useCase.voteQuestion(UserId.validated(USER_ID), QuestionId.validated("unknown"), voteRequest))
                .isInstanceOf(UnknownEntityException.class);
    }
}

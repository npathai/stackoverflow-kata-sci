package org.npathai.kata.application.domain.question.usecase;

import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.npathai.kata.application.domain.ImpermissibleOperationException;
import org.npathai.kata.application.domain.question.QuestionBuilder;
import org.npathai.kata.application.domain.question.answer.AnswerBuilder;
import org.npathai.kata.application.domain.question.answer.dto.Answer;
import org.npathai.kata.application.domain.question.answer.dto.AnswerId;
import org.npathai.kata.application.domain.question.answer.persistence.AnswerRepository;
import org.npathai.kata.application.domain.question.dto.Question;
import org.npathai.kata.application.domain.services.IdGenerator;
import org.npathai.kata.application.domain.user.InsufficientReputationException;
import org.npathai.kata.application.domain.user.UserBuilder;
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

@ExtendWith(MockitoExtension.class)
public class AnswerVotingUseCaseShould {

    private static final long AUTHOR_INITIAL_REPUTATION = 10000;
    private static final long VOTER_INITIAL_REPUTATION = 5000;
    private static final long ANSWER_AUTHOR_INITIAL_REPUTATION = 2000;
    private static final int ANSWER_INITIAL_SCORE = 2;
    private static final int VOTER_INITIAL_CAST_UP_VOTES = 10;
    private static final int VOTER_INITIAL_CAST_DOWN_VOTES = 20;

    @Mock
    AnswerRepository answerRepository;

    @Mock
    UserService userService;

    @Mock
    VoteRepository voteRepository;

    @Mock
    IdGenerator voteIdGenerator;

    AnswerVotingUseCase useCase;

    private User answerAuthor;
    private User voter;
    private Answer answer;

    @BeforeEach
    public void setUp() {
        useCase = new AnswerVotingUseCase(answerRepository, voteRepository, voteIdGenerator, userService);

        User questionAuthor = UserBuilder.anUser().withReputation(AUTHOR_INITIAL_REPUTATION).build();
        voter = UserBuilder.anUser()
                .withReputation(VOTER_INITIAL_REPUTATION)
                .withCastUpVotes(VOTER_INITIAL_CAST_UP_VOTES)
                .withCastDownVotes(VOTER_INITIAL_CAST_DOWN_VOTES)
                .build();
        answerAuthor = UserBuilder.anUser().withReputation(ANSWER_AUTHOR_INITIAL_REPUTATION).build();
        Question question = QuestionBuilder.aQuestion().withAuthorId(questionAuthor.getId()).build();
        answer = AnswerBuilder.anAnswer()
                .withAuthorId(answerAuthor.getId())
                .withQuestionId(question.getId())
                .withScore(ANSWER_INITIAL_SCORE)
                .build();
    }

    @Nested
    public class OnUpVote {
        private final String voteId = "v1";
        private VoteRequest upVoteRequest;

        @BeforeEach
        @SneakyThrows
        public void setUp() {
            upVoteRequest = VoteRequest.valid(VoteType.UP);

        }

        @Nested
        public class VoteRecorded {

            @BeforeEach
            @SneakyThrows
            public void setUp() {
                given(voteIdGenerator.get()).willReturn(voteId);
                given(answerRepository.findById(answer.getId())).willReturn(Optional.of(answer));
                given(userService.getUserById(UserId.validated(answerAuthor.getId()))).willReturn(answerAuthor);
                given(userService.getUserById(UserId.validated(voter.getId()))).willReturn(voter);
            }

            @Test
            @SneakyThrows
            public void returnIncrementedAnswerScore() {
                Score score = useCase.voteAnswer(UserId.validated(voter.getId()), AnswerId.validated(answer.getId()), upVoteRequest);
                assertThat(score.getScore()).isEqualTo(ANSWER_INITIAL_SCORE + 1);
            }

            @Test
            @SneakyThrows
            public void incrementScoreOfAnswer() {
                useCase.voteAnswer(UserId.validated(voter.getId()), AnswerId.validated(answer.getId()), upVoteRequest);
                assertThat(answer.getScore()).isEqualTo(ANSWER_INITIAL_SCORE + 1);
                verify(answerRepository).save(answer);
            }

            @Test
            @SneakyThrows
            public void incrementAnswerAuthorReputation() {
                useCase.voteAnswer(UserId.validated(voter.getId()), AnswerId.validated(answer.getId()), upVoteRequest);
                assertThat(answerAuthor.getReputation()).isEqualTo(ANSWER_AUTHOR_INITIAL_REPUTATION + 10);
                verify(userService).update(answerAuthor);
            }

            @Test
            @SneakyThrows
            public void incrementVoterCastUpVotesCount() {
                useCase.voteAnswer(UserId.validated(voter.getId()), AnswerId.validated(answer.getId()), upVoteRequest);
                assertThat(voter.getCastUpVotes()).isEqualTo(VOTER_INITIAL_CAST_UP_VOTES + 1);
                verify(userService).update(voter);
            }

            @Test
            @SneakyThrows
            public void savesVoteInRepository() {
                useCase.voteAnswer(UserId.validated(voter.getId()), AnswerId.validated(answer.getId()), upVoteRequest);

                Vote expectedVote = new Vote();
                expectedVote.setId(voteId);
                expectedVote.setVotableId(answer.getId());
                expectedVote.setVoterId(voter.getId());
                expectedVote.setType("up");

                verify(voteRepository).save(expectedVote);
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

                useCase.voteAnswer(UserId.validated(voter.getId()), AnswerId.validated(answer.getId()), upVoteRequest);

                assertThat(answer.getScore()).isEqualTo(ANSWER_INITIAL_SCORE + 1);
            }
        }

        @Nested
        public class VoteRejected {

            @ParameterizedTest
            @SneakyThrows
            @ValueSource(ints = {
                    1,
                    13,
                    14,
            })
            public void doesNotAllowUserWithInsufficientReputationToVote(int reputation) {
                given(answerRepository.findById(answer.getId())).willReturn(Optional.of(answer));
                given(userService.getUserById(UserId.validated(answerAuthor.getId()))).willReturn(answerAuthor);
                given(userService.getUserById(UserId.validated(voter.getId()))).willReturn(voter);

                voter.setReputation(reputation);

                assertThatThrownBy(() -> useCase.voteAnswer(UserId.validated(voter.getId()), AnswerId.validated(answer.getId()), upVoteRequest)).isInstanceOf(InsufficientReputationException.class);
            }

            @Test
            @SneakyThrows
            public void doesNotAllowAuthorToVoteOnOwnAnswer() {
                given(answerRepository.findById(answer.getId())).willReturn(Optional.of(answer));
                given(userService.getUserById(UserId.validated(answerAuthor.getId()))).willReturn(answerAuthor);

                assertThatThrownBy(() -> useCase.voteAnswer(UserId.validated(answerAuthor.getId()), AnswerId.validated(answer.getId()), upVoteRequest))
                        .isInstanceOf(ImpermissibleOperationException.class);
            }
        }
    }

    @Nested
    public class OnDownVote {
        private final String voteId = "v1";
        private VoteRequest downVoteRequest;

        @BeforeEach
        @SneakyThrows
        public void setUp() {
            downVoteRequest = VoteRequest.valid(VoteType.DOWN);
        }

        @Nested
        public class VoteRecorded {

            @BeforeEach
            @SneakyThrows
            public void setUp() {
                given(voteIdGenerator.get()).willReturn(voteId);
                given(answerRepository.findById(answer.getId())).willReturn(Optional.of(answer));
                given(userService.getUserById(UserId.validated(answerAuthor.getId()))).willReturn(answerAuthor);
                given(userService.getUserById(UserId.validated(voter.getId()))).willReturn(voter);
            }

            @Test
            @SneakyThrows
            public void returnIncrementedAnswerScore() {
                Score score = useCase.voteAnswer(UserId.validated(voter.getId()), AnswerId.validated(answer.getId()), downVoteRequest);
                assertThat(score.getScore()).isEqualTo(ANSWER_INITIAL_SCORE - 1);
            }

            @Test
            @SneakyThrows
            public void incrementScoreOfAnswer() {
                useCase.voteAnswer(UserId.validated(voter.getId()), AnswerId.validated(answer.getId()), downVoteRequest);
                assertThat(answer.getScore()).isEqualTo(ANSWER_INITIAL_SCORE - 1);
                verify(answerRepository).save(answer);
            }

            @Test
            @SneakyThrows
            public void decrementAnswerAuthorReputation() {
                useCase.voteAnswer(UserId.validated(voter.getId()), AnswerId.validated(answer.getId()), downVoteRequest);
                assertThat(answerAuthor.getReputation()).isEqualTo(ANSWER_AUTHOR_INITIAL_REPUTATION - 5);
                verify(userService).update(answerAuthor);
            }

            @Test
            @SneakyThrows
            public void incrementVoterCastDownVotesCount() {
                useCase.voteAnswer(UserId.validated(voter.getId()), AnswerId.validated(answer.getId()), downVoteRequest);
                assertThat(voter.getCastDownVotes()).isEqualTo(VOTER_INITIAL_CAST_DOWN_VOTES + 1);
                verify(userService).update(voter);
            }

            @Test
            @SneakyThrows
            public void decrementVoterReputationByOne() {
                useCase.voteAnswer(UserId.validated(voter.getId()), AnswerId.validated(answer.getId()), downVoteRequest);
                assertThat(voter.getReputation()).isEqualTo(VOTER_INITIAL_REPUTATION - 1);
                verify(userService).update(voter);
            }

            @Test
            @SneakyThrows
            public void savesVoteInRepository() {
                useCase.voteAnswer(UserId.validated(voter.getId()), AnswerId.validated(answer.getId()), downVoteRequest);

                Vote expectedVote = new Vote();
                expectedVote.setId(voteId);
                expectedVote.setVotableId(answer.getId());
                expectedVote.setVoterId(voter.getId());
                expectedVote.setType("down");

                verify(voteRepository).save(expectedVote);
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

                useCase.voteAnswer(UserId.validated(voter.getId()), AnswerId.validated(answer.getId()), downVoteRequest);

                assertThat(answer.getScore()).isEqualTo(ANSWER_INITIAL_SCORE - 1);
            }
        }

        @Nested
        public class VoteRejected {

            @ParameterizedTest
            @SneakyThrows
            @ValueSource(ints = {
                    1,
                    100,
                    124,
            })
            public void doesNotAllowUserWithInsufficientReputationToVote(int reputation) {
                given(answerRepository.findById(answer.getId())).willReturn(Optional.of(answer));
                given(userService.getUserById(UserId.validated(answerAuthor.getId()))).willReturn(answerAuthor);
                given(userService.getUserById(UserId.validated(voter.getId()))).willReturn(voter);

                voter.setReputation(reputation);

                assertThatThrownBy(() -> useCase.voteAnswer(UserId.validated(voter.getId()), AnswerId.validated(answer.getId()), downVoteRequest)).isInstanceOf(InsufficientReputationException.class);
            }

            @Test
            @SneakyThrows
            public void doesNotAllowAuthorToVoteOnOwnAnswer() {
                given(answerRepository.findById(answer.getId())).willReturn(Optional.of(answer));
                given(userService.getUserById(UserId.validated(answerAuthor.getId()))).willReturn(answerAuthor);

                assertThatThrownBy(() -> useCase.voteAnswer(UserId.validated(answerAuthor.getId()), AnswerId.validated(answer.getId()), downVoteRequest))
                        .isInstanceOf(ImpermissibleOperationException.class);
            }
        }
    }
}
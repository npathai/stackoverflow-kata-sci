package org.npathai.kata.application.domain.question.usecase;

import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.npathai.kata.application.domain.question.QuestionBuilder;
import org.npathai.kata.application.domain.question.answer.AnswerBuilder;
import org.npathai.kata.application.domain.question.answer.dto.Answer;
import org.npathai.kata.application.domain.question.answer.dto.AnswerId;
import org.npathai.kata.application.domain.question.answer.persistence.AnswerRepository;
import org.npathai.kata.application.domain.question.dto.Question;
import org.npathai.kata.application.domain.user.UserBuilder;
import org.npathai.kata.application.domain.user.UserId;
import org.npathai.kata.application.domain.user.UserService;
import org.npathai.kata.application.domain.user.dto.User;
import org.npathai.kata.application.domain.vote.VoteRepository;
import org.npathai.kata.application.domain.vote.dto.Score;
import org.npathai.kata.application.domain.vote.dto.Vote;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class AnswerCancelVotingUseCaseShould {

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

    AnswerCancelVotingUseCase useCase;

    private User answerAuthor;
    private User voter;
    private Answer answer;

    @BeforeEach
    public void setUp() {
        useCase = new AnswerCancelVotingUseCase(answerRepository, voteRepository, userService);

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
    public class CancelUpVote {
        private final String voteId = "v1";
        private Score cancelledScore;

        @BeforeEach
        @SneakyThrows
        public void setUp() {
            Vote vote = new Vote();
            vote.setId(voteId);
            vote.setVotableId(answer.getId());
            vote.setVoterId(voter.getId());
            vote.setType("up");

            given(voteRepository.findByVotableIdAndVoterId(answer.getId(), voter.getId())).willReturn(vote);

            given(answerRepository.findById(answer.getId())).willReturn(Optional.of(answer));
            given(userService.getUserById(UserId.validated(answerAuthor.getId()))).willReturn(answerAuthor);
            given(userService.getUserById(UserId.validated(voter.getId()))).willReturn(voter);

            cancelledScore = useCase.cancelVote(UserId.validated(voter.getId()), AnswerId.validated(answer.getId()));
        }

        @Test
        @SneakyThrows
        public void returnDecreasedScoreWhenVoteIsCancelled() {
            assertThat(cancelledScore.getScore()).isEqualTo(ANSWER_INITIAL_SCORE - 1);
        }

        @Test
        public void decrementAnswerScoreAndUpdate() {
            assertThat(answer.getScore()).isEqualTo(ANSWER_INITIAL_SCORE - 1);
            verify(answerRepository).save(answer);
        }

        @Test
        public void decrementVoterCastUpVotesAndUpdate() {
            assertThat(voter.getCastUpVotes()).isEqualTo(VOTER_INITIAL_CAST_UP_VOTES - 1);
            verify(userService).update(voter);
        }

        @Test
        public void decrementAuthorReputationAndUpdate() {
            assertThat(answerAuthor.getReputation()).isEqualTo(ANSWER_AUTHOR_INITIAL_REPUTATION - 10);
            verify(userService).update(answerAuthor);
        }

        @Test
        @SneakyThrows
        public void deleteVote() {
            Vote vote = new Vote();
            vote.setId(voteId);
            vote.setVotableId(answer.getId());
            vote.setVoterId(voter.getId());
            vote.setType("up");

            verify(voteRepository).delete(vote);
        }
    }

    @Nested
    public class CancelDownVote {
        private final String voteId = "v1";
        private Score cancelledScore;

        @BeforeEach
        @SneakyThrows
        public void setUp() {
            Vote vote = new Vote();
            vote.setId(voteId);
            vote.setVotableId(answer.getId());
            vote.setVoterId(voter.getId());
            vote.setType("down");

            given(voteRepository.findByVotableIdAndVoterId(answer.getId(), voter.getId())).willReturn(vote);

            given(answerRepository.findById(answer.getId())).willReturn(Optional.of(answer));
            given(userService.getUserById(UserId.validated(answerAuthor.getId()))).willReturn(answerAuthor);
            given(userService.getUserById(UserId.validated(voter.getId()))).willReturn(voter);

            cancelledScore = useCase.cancelVote(UserId.validated(voter.getId()), AnswerId.validated(answer.getId()));
        }

        @Test
        @SneakyThrows
        public void returnIncreasedScoreWhenVoteIsCancelled() {
            assertThat(cancelledScore.getScore()).isEqualTo(ANSWER_INITIAL_SCORE + 1);
        }

        @Test
        public void incrementAnswerScoreAndUpdate() {
            assertThat(answer.getScore()).isEqualTo(ANSWER_INITIAL_SCORE + 1);
            verify(answerRepository).save(answer);
        }

        @Test
        public void decrementVoterCastDownVotesAndUpdate() {
            assertThat(voter.getCastDownVotes()).isEqualTo(VOTER_INITIAL_CAST_DOWN_VOTES - 1);
            verify(userService).update(voter);
        }

        @Test
        public void incrementAuthorReputationAndUpdate() {
            assertThat(answerAuthor.getReputation()).isEqualTo(ANSWER_AUTHOR_INITIAL_REPUTATION + 5);
            verify(userService).update(answerAuthor);
        }

        @Test
        public void incrementVoterReputationAndUpdate() {
            assertThat(voter.getReputation()).isEqualTo(VOTER_INITIAL_REPUTATION + 1);
            verify(userService).update(voter);
        }

        @Test
        @SneakyThrows
        public void deleteVote() {
            Vote vote = new Vote();
            vote.setId(voteId);
            vote.setVotableId(answer.getId());
            vote.setVoterId(voter.getId());
            vote.setType("down");

            verify(voteRepository).delete(vote);
        }
    }
}

package org.npathai.kata.application.domain.question.usecase;

import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.npathai.kata.application.domain.question.QuestionBuilder;
import org.npathai.kata.application.domain.question.QuestionId;
import org.npathai.kata.application.domain.question.dto.Question;
import org.npathai.kata.application.domain.question.persistence.QuestionRepository;
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
import static org.npathai.kata.application.domain.user.UserBuilder.anUser;

@ExtendWith(MockitoExtension.class)
class QuestionCancelVotingUseCaseShould {
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

    @InjectMocks
    QuestionCancelVotingUseCase useCase;

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

}
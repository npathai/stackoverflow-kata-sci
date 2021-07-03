package org.npathai.kata.application.domain.question;

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
import org.npathai.kata.application.domain.question.answer.dto.Answer;
import org.npathai.kata.application.domain.question.answer.persistence.AnswerRepository;
import org.npathai.kata.application.domain.question.dto.Question;
import org.npathai.kata.application.domain.question.dto.QuestionWithAnswers;
import org.npathai.kata.application.domain.question.persistence.QuestionRepository;
import org.npathai.kata.application.domain.question.request.PostQuestionRequest;
import org.npathai.kata.application.domain.question.usecase.GetQuestionUseCase;
import org.npathai.kata.application.domain.question.usecase.GetRecentQuestionsUseCase;
import org.npathai.kata.application.domain.question.usecase.PostAnswerUseCase;
import org.npathai.kata.application.domain.question.usecase.PostQuestionUseCase;
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

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.npathai.kata.application.domain.tag.TagBuilder.aTag;
import static org.npathai.kata.application.domain.user.UserBuilder.anUser;

@ExtendWith(MockitoExtension.class)
public class QuestionServiceShould {
    private static final String USER_ID = "U1";
    private static final String QUESTION_ID = "Q1";
    private static final String QUESTION_TITLE = "First Question";
    private static final String QUESTION_BODY = "First question body";
    private static final List<String> QUESTION_TAGS = List.of("java", "kata");
    private static final String ANSWER_ID = "A1";
    private static final String ANSWERER_ID = "U2";

    @Mock
    QuestionRepository questionRepository;
    @Mock
    AnswerRepository answerRepository;

    @Mock
    UserService userService;

    @Mock
    VoteRepository voteRepository;

    @Mock
    PostQuestionUseCase postQuestionUseCase;

    @Mock
    GetRecentQuestionsUseCase getRecentQuestionsUseCase;

    @Mock
    PostAnswerUseCase postAnswerUseCase;

    @Mock
    GetQuestionUseCase getQuestionUseCase;

    @Mock
    IdGenerator voteIdGenerator;

    Clock clock;

    QuestionService questionService;

    PostQuestionRequest request;


    @BeforeEach
    public void setUp() {
        clock = fixedClock();
        questionService = new QuestionService(postQuestionUseCase,
                getRecentQuestionsUseCase,
                postAnswerUseCase,
                getQuestionUseCase,
                questionRepository,
                answerRepository, userService, voteRepository,
                voteIdGenerator);
        request = PostQuestionRequest.valid(QUESTION_TITLE, QUESTION_BODY, QUESTION_TAGS);
    }

    private Clock fixedClock() {
        return Clock.fixed(Instant.now(), ZoneId.systemDefault());
    }

    @Nested
    public class QuestionVotingShould {
        private static final long AUTHOR_REPUTATION = 1000;

        private Question question;
        private User author;
        private User voter;

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

            question = aQuestion(QUESTION_ID)
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
                return questionService.voteQuestion(UserId.validated(voter.getId()),
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

                assertThat(questionService.voteQuestion(UserId.validated(voter.getId()),
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

                assertThatThrownBy(() -> assertThat(questionService.voteQuestion(UserId.validated(voter.getId()),
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

                cancelledScore = questionService.cancelVote(UserId.validated(voter.getId()), QuestionId.validated(question.getId()));
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

                score = questionService.voteQuestion(UserId.validated(voter.getId()), QuestionId.validated(QUESTION_ID),
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
                assertThatThrownBy(() -> questionService.voteQuestion(UserId.validated(author.getId()),
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

                assertThat(questionService.voteQuestion(UserId.validated(voter.getId()),
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

                assertThatThrownBy(() -> assertThat(questionService.voteQuestion(UserId.validated(voter.getId()),
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

                cancelledScore = questionService.cancelVote(UserId.validated(voter.getId()), QuestionId.validated(question.getId()));
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

            assertThatThrownBy(() -> questionService.voteQuestion(UserId.validated(USER_ID), QuestionId.validated("unknown"), voteRequest))
                    .isInstanceOf(UnknownEntityException.class);
        }
    }

    private QuestionBuilder aQuestion(String id) {
        return QuestionBuilder.aQuestion()
                .withId(id)
                .withTitle(QUESTION_TITLE)
                .withBody(QUESTION_BODY)
                .withTags(List.of(
                        aTag().withId("1").withName("java").build(),
                        aTag().withId("2").withName("kata").build())
                )
                .withAuthorId(USER_ID);
    }

}
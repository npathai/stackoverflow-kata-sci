package org.npathai.kata.application.domain.question;

import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.npathai.kata.application.domain.question.answer.dto.Answer;
import org.npathai.kata.application.domain.question.answer.persistence.AnswerRepository;
import org.npathai.kata.application.domain.question.answer.request.PostAnswerRequest;
import org.npathai.kata.application.domain.question.dto.Question;
import org.npathai.kata.application.domain.question.dto.QuestionWithAnswers;
import org.npathai.kata.application.domain.question.persistence.QuestionRepository;
import org.npathai.kata.application.domain.question.request.PostQuestionRequest;
import org.npathai.kata.application.domain.services.IdGenerator;
import org.npathai.kata.application.domain.services.UnknownEntityException;
import org.npathai.kata.application.domain.tag.dto.Tag;
import org.npathai.kata.application.domain.tag.persistence.TagRepository;
import org.npathai.kata.application.domain.user.UserId;
import org.npathai.kata.application.domain.user.UserService;
import org.npathai.kata.application.domain.user.dto.User;
import org.npathai.kata.application.domain.vote.VoteRequest;
import org.npathai.kata.application.domain.vote.VoteType;
import org.npathai.kata.application.domain.vote.dto.Score;
import org.springframework.data.domain.*;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

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
    TagRepository tagRepository;

    @Mock
    AnswerRepository answerRepository;

    @Mock
    UserService userService;

    @Mock
    IdGenerator questionIdGenerator;

    @Mock
    IdGenerator tagIdGenerator;

    @Mock
    IdGenerator answerIdGenerator;

    Clock clock;

    QuestionService questionService;

    PostQuestionRequest request;

    @BeforeEach
    public void setUp() {
        clock = fixedClock();
        questionService = new QuestionService(tagRepository, questionRepository,
                answerRepository, userService, questionIdGenerator, tagIdGenerator, answerIdGenerator, clock);
        request = PostQuestionRequest.valid(QUESTION_TITLE, QUESTION_BODY, QUESTION_TAGS);
    }

    private Clock fixedClock() {
        return Clock.fixed(Instant.now(), ZoneId.systemDefault());
    }

    @Nested
    public class PostQuestionShould {

        private Question question;
        private List<Tag> tags;

        @BeforeEach
        public void setUp() {
            given(questionIdGenerator.get()).willReturn(QUESTION_ID);

            Tag javaTag = aTag("1", "java");
            Tag kataTag = aTag("2", "kata");
            tags = List.of(javaTag, kataTag);
            given(tagRepository.findAllByName(QUESTION_TAGS)).willReturn(tags);
        }

        @Test
        public void createQuestionWithGivenDetails() {
            postQuestion();

            assertThat(question.getTitle()).isEqualTo(QUESTION_TITLE);
            assertThat(question.getBody()).isEqualTo(QUESTION_BODY);
        }

        @Test
        public void usesExistingTags() {
            postQuestion();

            assertThat(question.getTags()).isEqualTo(tags);
            verify(tagRepository, times(0)).save(any());
        }

        @Test
        public void createMissingTags() {
            given(tagRepository.findAllByName(QUESTION_TAGS)).willReturn(List.of(aTag("1", "java")));
            given(tagIdGenerator.get()).willReturn("2");

            postQuestion();
            assertThat(question.getTags()).isEqualTo(tags);
            verify(tagRepository).save(aTag("2", "kata"));
        }

        @Test
        public void assignIdToQuestion() {
            postQuestion();

            assertThat(question.getId()).isEqualTo(QUESTION_ID);
        }

        @Test
        public void assignCreatedAtAsCurrentTime() {
            postQuestion();

            assertThat(question.getCreatedAt()).isEqualTo(clock.millis());
        }

        @Test
        public void savesQuestionToRepository() {
            postQuestion();

            verify(questionRepository).save(question);
        }

        @Test
        public void assignAuthorIdToBeUserId() {
            postQuestion();

            assertThat(question.getAuthorId()).isEqualTo(USER_ID);
        }

        @SneakyThrows
        private void postQuestion() {
            question = questionService.post(UserId.validated(USER_ID), request);
        }

        private Tag aTag(String id, String name) {
            Tag javaTag = new Tag();
            javaTag.setId(id);
            javaTag.setName(name);
            return javaTag;
        }
    }

    @Nested
    public class RecentQuestionsShould {

        @Captor
        ArgumentCaptor<PageRequest> captor;
        private List<Question> questions;
        private PageImpl<Question> questionPage;

        @BeforeEach
        public void setUp() {
            questions = List.of(aQuestion("1"),
                    aQuestion("2")
            );

            questionPage = new PageImpl<>(this.questions);
        }

        @Test
        public void returnsPageContainingQuestions() {
            given(questionRepository.findAll(any(Pageable.class))).willReturn(questionPage);

            Page<Question> recentQuestionsPage = questionService.getRecentQuestions();

            assertThat(recentQuestionsPage.getContent()).isEqualTo(questions);
        }

        @Test
        public void returnTenQuestionsSortedInDescendingOrderOfCreationDate() {
            given(questionRepository.findAll(any(Pageable.class))).willReturn(questionPage);

            questionService.getRecentQuestions();

            verify(questionRepository).findAll(captor.capture());
            assertThat(captor.getValue().getPageNumber()).isEqualTo(0);
            assertThat(captor.getValue().getPageSize()).isEqualTo(10);
            assertThat(captor.getValue().getSort()).isEqualTo(Sort.by(Sort.Direction.DESC, "createdAt"));
        }
    }

    @Nested
    public class PostAnswerShould {

        private Question question;
        private Answer answer;
        private PostAnswerRequest postAnswerRequest;

        @BeforeEach
        public void setUp() {
            postAnswerRequest = PostAnswerRequest.valid("Body");
            given(answerIdGenerator.get()).willReturn(ANSWER_ID);

            question = aQuestion(QUESTION_ID);
            given(questionRepository.findById(QUESTION_ID)).willReturn(Optional.of(question));

            answer = new Answer();
            answer.setId(ANSWER_ID);
            answer.setAuthorId(ANSWERER_ID);
            answer.setQuestionId(QUESTION_ID);
            answer.setBody("Body");
        }

        @Test
        @SneakyThrows
        public void returnCreatedAnswer() {
            Answer postedAnswer = questionService.postAnswer(UserId.validated(ANSWERER_ID), QuestionId.validated(QUESTION_ID), postAnswerRequest);
            assertThat(postedAnswer).isEqualTo(answer);
        }

        @Test
        @SneakyThrows
        public void saveAnswerToRepository() {
            questionService.postAnswer(UserId.validated(ANSWERER_ID), QuestionId.validated(QUESTION_ID), postAnswerRequest);
            verify(answerRepository).save(answer);
        }

        @Test
        @SneakyThrows
        public void incrementAnswerCount() {
            questionService.postAnswer(UserId.validated(ANSWERER_ID), QuestionId.validated(QUESTION_ID), postAnswerRequest);
            questionService.postAnswer(UserId.validated(ANSWERER_ID), QuestionId.validated(QUESTION_ID), postAnswerRequest);

            assertThat(question.getAnswerCount()).isEqualTo(2);
            verify(questionRepository, times(2)).save(question);
        }
    }

    @Nested
    public class GetQuestionShould {

        @Test
        @SneakyThrows
        public void returnQuestionWithAnswers() {
            Question question = aQuestion(QUESTION_ID);

            Answer answer = new Answer();
            answer.setId(ANSWER_ID);
            answer.setAuthorId(ANSWERER_ID);
            answer.setQuestionId(QUESTION_ID);
            answer.setBody("Body");

            QuestionWithAnswers expected = new QuestionWithAnswers();
            expected.setQuestion(question);
            expected.setAnswers(List.of(answer));

            given(questionRepository.findById(QUESTION_ID)).willReturn(Optional.of(question));
            given(answerRepository.findByQuestionId(QUESTION_ID)).willReturn(List.of(answer));

            QuestionWithAnswers questionWithAnswers = questionService.getQuestion(QuestionId.validated(QUESTION_ID));

            assertThat(questionWithAnswers).isEqualTo(expected);
        }

        @Test
        public void throwExceptionWhenQuestionWithIdNotFound() {
            assertThatThrownBy(() -> questionService.getQuestion(QuestionId.validated("unknown")))
                    .isInstanceOf(UnknownEntityException.class);
        }
    }

    @Nested
    public class QuestionVotingShould {

        private Question question;
        private User user;

        @BeforeEach
        @SneakyThrows
        public void setUp() {
            question = aQuestion(QUESTION_ID);
            question.setScore(10);
            user = aUser();
            user.setCastUpVotes(10);
            user.setCastDownVotes(10);

        }

        @Nested
        public class OnUpVote {

            private VoteRequest voteRequest;

            @BeforeEach
            @SneakyThrows
            public void setUp() {
                given(userService.getUserById(UserId.validated(USER_ID))).willReturn(user);
                given(questionRepository.findById(QUESTION_ID)).willReturn(Optional.of(question));
                voteRequest = VoteRequest.valid(VoteType.UP);
            }

            @Test
            @SneakyThrows
            public void returnIncrementedScore() {
                Score score = questionService.voteQuestion(UserId.validated(USER_ID), QuestionId.validated(QUESTION_ID), voteRequest);
                assertThat(score.getScore()).isEqualTo(11);
            }

            @Test
            @SneakyThrows
            public void incrementUpVoteCastCountOfVoter() {
                questionService.voteQuestion(UserId.validated(USER_ID), QuestionId.validated(QUESTION_ID), voteRequest);

                assertThat(user.getCastUpVotes()).isEqualTo(11);
                verify(userService).update(user);
            }

            @Test
            @SneakyThrows
            public void incrementsScoreOfQuestion() {
                questionService.voteQuestion(UserId.validated(USER_ID), QuestionId.validated(QUESTION_ID), voteRequest);

                assertThat(question.getScore()).isEqualTo(11);
                verify(questionRepository).save(question);
            }

        }

        @Nested
        public class OnDownVote {

            private VoteRequest voteRequest;

            @BeforeEach
            @SneakyThrows
            public void setUp() {
                given(userService.getUserById(UserId.validated(USER_ID))).willReturn(user);
                given(questionRepository.findById(QUESTION_ID)).willReturn(Optional.of(question));
                voteRequest = VoteRequest.valid(VoteType.DOWN);
            }

            @Test
            @SneakyThrows
            public void returnsDecrementedScoreOfQuestion() {
                Score score = questionService.voteQuestion(UserId.validated(USER_ID), QuestionId.validated(QUESTION_ID),
                        voteRequest);

                assertThat(score.getScore()).isEqualTo(9);
            }

            @Test
            @SneakyThrows
            public void incrementsCountOfDownVotesCastOfVoter() {
                questionService.voteQuestion(UserId.validated(USER_ID), QuestionId.validated(QUESTION_ID), voteRequest);

                assertThat(user.getCastDownVotes()).isEqualTo(11);
                verify(userService).update(user);
            }

            @Test
            @SneakyThrows
            public void saveQuestionWithUpdatedScore() {
                questionService.voteQuestion(UserId.validated(USER_ID), QuestionId.validated(QUESTION_ID), voteRequest);
                assertThat(question.getScore()).isEqualTo(9);
                verify(questionRepository).save(question);
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

    private User aUser() {
        User user = new User();
        user.setId(USER_ID);
        return user;
    }

    private Question aQuestion(String id) {
        Question question = new Question();
        question.setId(id);
        question.setTitle(QUESTION_TITLE);
        question.setBody(QUESTION_BODY);
        question.setCreatedAt(System.currentTimeMillis());
        question.setTags(List.of(
                aTag("1", "java"),
                aTag("2", "kata")
        ));
        question.setAuthorId(USER_ID);
        return question;
    }

    private Tag aTag(String id, String name) {
        Tag tag = new Tag();
        tag.setId(id);
        tag.setName(name);
        return tag;
    }
}
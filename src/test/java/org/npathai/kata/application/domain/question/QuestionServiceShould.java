package org.npathai.kata.application.domain.question;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.npathai.kata.application.domain.question.dto.Question;
import org.npathai.kata.application.domain.question.persistence.QuestionRepository;
import org.npathai.kata.application.domain.question.request.PostQuestionRequest;
import org.npathai.kata.application.domain.services.IdGenerator;
import org.npathai.kata.application.domain.tag.dto.Tag;
import org.npathai.kata.application.domain.tag.persistence.TagRepository;
import org.npathai.kata.application.domain.user.UserId;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class QuestionServiceShould {
    private static final String USER_ID = "1";
    private static final String QUESTION_ID = "1";
    private static final String QUESTION_TITLE = "First Question";
    private static final String QUESTION_BODY = "First question body";
    private static final List<String> QUESTION_TAGS = List.of("java", "kata");

    @Mock
    QuestionRepository questionRepository;

    @Mock
    TagRepository tagRepository;

    @Mock
    IdGenerator questionIdGenerator;

    @Mock
    IdGenerator tagIdGenerator;

    Clock clock;
    QuestionService questionService;

    PostQuestionRequest request;

    @BeforeEach
    public void setUp() {
        clock = fixedClock();
        questionService = new QuestionService(tagRepository, questionRepository, questionIdGenerator, tagIdGenerator, clock);
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
}
package org.npathai.kata.application.domain.question;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.npathai.kata.application.domain.question.dto.Question;
import org.npathai.kata.application.domain.question.persistence.QuestionRepository;
import org.npathai.kata.application.domain.question.request.PostQuestionRequest;
import org.npathai.kata.application.domain.services.IdGenerator;
import org.npathai.kata.application.domain.tag.TagService;
import org.npathai.kata.application.domain.tag.dto.Tag;
import org.npathai.kata.application.domain.user.UserId;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
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
    TagService tagService;

    @Mock
    IdGenerator idGenerator;

    Clock clock;
    QuestionService questionService;

    PostQuestionRequest request;

    @BeforeEach
    public void setUp() {
        clock = fixedClock();
        questionService = new QuestionService(tagService, questionRepository, idGenerator, clock);
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
            given(idGenerator.get()).willReturn(QUESTION_ID);

            Tag javaTag = aTag("1", "java");
            Tag kataTag = aTag("2", "kata");
            tags = List.of(javaTag, kataTag);
            given(tagService.getOrCreateTags(QUESTION_TAGS)).willReturn(tags);

            question = questionService.post(UserId.validated(USER_ID), request);
            assertThat(question).isNotNull();
        }

        @Test
        public void createQuestionWithGivenDetails() {
            assertThat(question.getTitle()).isEqualTo(QUESTION_TITLE);
            assertThat(question.getBody()).isEqualTo(QUESTION_BODY);
        }

        @Test
        public void createTagsIfMissing() {
            assertThat(question.getTags()).isEqualTo(tags);
            verify(tagService).getOrCreateTags(request.getTags());
        }

        @Test
        public void assignIdToQuestion() {
            assertThat(question.getId()).isEqualTo(QUESTION_ID);
        }

        @Test
        public void assignCreatedAtAsCurrentTime() {
            assertThat(question.getCreatedAt()).isEqualTo(clock.millis());
        }
        
        @Test
        public void savesQuestionToRepository() {
            verify(questionRepository).save(question);
        }

        @Test
        public void assignAuthorIdToBeUserId() {
            assertThat(question.getAuthorId()).isEqualTo(USER_ID);
        }

        private Tag aTag(String id, String name) {
            Tag javaTag = new Tag();
            javaTag.setId(id);
            javaTag.setName(name);
            return javaTag;
        }
    }
}
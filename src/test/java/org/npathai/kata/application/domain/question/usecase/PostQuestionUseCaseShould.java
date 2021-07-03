package org.npathai.kata.application.domain.question.usecase;

import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
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

import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PostQuestionUseCaseShould {
    private static final String USER_ID = "U1";
    private static final String QUESTION_ID = "Q1";
    private static final List<String> QUESTION_TAGS = List.of("java", "kata");
    private static final String QUESTION_TITLE = "First Question";
    private static final String QUESTION_BODY = "First question body";

    private Question question;
    private List<Tag> tags;

    @Mock
    IdGenerator questionIdGenerator;

    @Mock
    IdGenerator tagIdGenerator;

    @Mock
    TagRepository tagRepository;

    @Mock
    QuestionRepository questionRepository;

    Clock clock;

    PostQuestionUseCase useCase;
    private PostQuestionRequest request;

    @BeforeEach
    public void setUp() {
        clock = fixedClock();
        given(questionIdGenerator.get()).willReturn(QUESTION_ID);

        Tag javaTag = aTag("1", "java");
        Tag kataTag = aTag("2", "kata");
        tags = List.of(javaTag, kataTag);
        given(tagRepository.findAllByName(QUESTION_TAGS)).willReturn(tags);

        request = PostQuestionRequest.valid(QUESTION_TITLE, QUESTION_BODY, QUESTION_TAGS);

        useCase = new PostQuestionUseCase(questionRepository, tagRepository, questionIdGenerator, tagIdGenerator, clock);
    }

    private Clock fixedClock() {
        return Clock.fixed(Instant.now(), ZoneId.systemDefault());
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
        question = useCase.post(UserId.validated(USER_ID), request);
    }

    private Tag aTag(String id, String name) {
        Tag javaTag = new Tag();
        javaTag.setId(id);
        javaTag.setName(name);
        return javaTag;
    }
}
package org.npathai.kata.application.domain.question.usecase;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.npathai.kata.application.domain.question.QuestionBuilder;
import org.npathai.kata.application.domain.question.dto.Question;
import org.npathai.kata.application.domain.question.persistence.QuestionRepository;
import org.springframework.data.domain.*;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.npathai.kata.application.domain.tag.TagBuilder.aTag;

import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class RecentQuestionsShould {
    private static final String USER_ID = "U1";
    private static final String QUESTION_TITLE = "First Question";
    private static final String QUESTION_BODY = "First question body";

    @Mock
    QuestionRepository questionRepository;

    @Captor
    ArgumentCaptor<PageRequest> captor;
    List<Question> questions;
    PageImpl<Question> questionPage;

    GetRecentQuestionsUseCase useCase;

    @BeforeEach
    public void setUp() {
        questions = List.of(
                aQuestion("1").build(),
                aQuestion("2").build()
        );

        questionPage = new PageImpl<>(this.questions);

        useCase = new GetRecentQuestionsUseCase(questionRepository);
    }

    @Test
    public void returnsPageContainingQuestions() {
        given(questionRepository.findAll(any(Pageable.class))).willReturn(questionPage);

        Page<Question> recentQuestionsPage = useCase.getRecentQuestions();

        assertThat(recentQuestionsPage.getContent()).isEqualTo(questions);
    }

    @Test
    public void returnTenQuestionsSortedInDescendingOrderOfCreationDate() {
        given(questionRepository.findAll(any(Pageable.class))).willReturn(questionPage);

        useCase.getRecentQuestions();

        verify(questionRepository).findAll(captor.capture());
        assertThat(captor.getValue().getPageNumber()).isEqualTo(0);
        assertThat(captor.getValue().getPageSize()).isEqualTo(10);
        assertThat(captor.getValue().getSort()).isEqualTo(Sort.by(Sort.Direction.DESC, "createdAt"));
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

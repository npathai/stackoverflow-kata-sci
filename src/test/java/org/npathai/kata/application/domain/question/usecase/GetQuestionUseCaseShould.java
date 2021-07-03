package org.npathai.kata.application.domain.question.usecase;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.npathai.kata.application.domain.question.QuestionBuilder;
import org.npathai.kata.application.domain.question.QuestionId;
import org.npathai.kata.application.domain.question.answer.dto.Answer;
import org.npathai.kata.application.domain.question.answer.persistence.AnswerRepository;
import org.npathai.kata.application.domain.question.dto.Question;
import org.npathai.kata.application.domain.question.dto.QuestionWithAnswers;
import org.npathai.kata.application.domain.question.persistence.QuestionRepository;
import org.npathai.kata.application.domain.services.UnknownEntityException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class GetQuestionUseCaseShould {
    private static final String QUESTION_ID = "Q1";
    private static final String ANSWER_ID = "A1";
    private static final String ANSWERER_ID = "U2";

    @Mock
    QuestionRepository questionRepository;

    @Mock
    AnswerRepository answerRepository;

    @InjectMocks
    GetQuestionUseCase questionService;

    @Test
    @SneakyThrows
    public void returnQuestionWithAnswers() {
        Question question = QuestionBuilder.aQuestion().withId(QUESTION_ID).build();

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

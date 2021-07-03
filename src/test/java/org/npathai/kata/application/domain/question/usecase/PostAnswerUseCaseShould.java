package org.npathai.kata.application.domain.question.usecase;

import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.npathai.kata.application.domain.question.QuestionBuilder;
import org.npathai.kata.application.domain.question.QuestionId;
import org.npathai.kata.application.domain.question.answer.dto.Answer;
import org.npathai.kata.application.domain.question.answer.persistence.AnswerRepository;
import org.npathai.kata.application.domain.question.answer.request.PostAnswerRequest;
import org.npathai.kata.application.domain.question.dto.Question;
import org.npathai.kata.application.domain.question.persistence.QuestionRepository;
import org.npathai.kata.application.domain.services.IdGenerator;
import org.npathai.kata.application.domain.user.UserId;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class PostAnswerUseCaseShould {
    private static final String QUESTION_ID = "Q1";
    private static final String ANSWER_ID = "A1";
    private static final String ANSWERER_ID = "U2";

    Question question;
    Answer answer;
    PostAnswerRequest postAnswerRequest;

    @Mock
    QuestionRepository questionRepository;

    @Mock
    AnswerRepository answerRepository;

    @Mock
    IdGenerator answerIdGenerator;

    @InjectMocks
    PostAnswerUseCase questionService;

    @BeforeEach
    public void setUp() {
        postAnswerRequest = PostAnswerRequest.valid("Body");
        given(answerIdGenerator.get()).willReturn(ANSWER_ID);

        question = QuestionBuilder.aQuestion().withId(QUESTION_ID).build();
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
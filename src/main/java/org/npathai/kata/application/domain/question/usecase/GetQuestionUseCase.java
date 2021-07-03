package org.npathai.kata.application.domain.question.usecase;

import org.npathai.kata.application.domain.question.QuestionId;
import org.npathai.kata.application.domain.question.answer.dto.Answer;
import org.npathai.kata.application.domain.question.answer.persistence.AnswerRepository;
import org.npathai.kata.application.domain.question.dto.Question;
import org.npathai.kata.application.domain.question.dto.QuestionWithAnswers;
import org.npathai.kata.application.domain.question.persistence.QuestionRepository;
import org.npathai.kata.application.domain.services.UnknownEntityException;

import java.util.List;

public class GetQuestionUseCase {
    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;

    public GetQuestionUseCase(QuestionRepository questionRepository,
                              AnswerRepository answerRepository) {
        this.questionRepository = questionRepository;
        this.answerRepository = answerRepository;
    }

    public QuestionWithAnswers getQuestion(QuestionId questionId) {
        Question question = getQuestionExplosively(questionId);
        List<Answer> answers = answerRepository.findByQuestionId(question.getId());
        QuestionWithAnswers questionWithAnswers = new QuestionWithAnswers();
        questionWithAnswers.setQuestion(question);
        questionWithAnswers.setAnswers(answers);
        return questionWithAnswers;
    }

    private Question getQuestionExplosively(QuestionId questionId) {
        return questionRepository.findById(questionId.getId())
                .orElseThrow(UnknownEntityException::new);
    }
}

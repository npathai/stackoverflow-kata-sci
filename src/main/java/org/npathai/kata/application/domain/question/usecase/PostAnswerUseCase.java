package org.npathai.kata.application.domain.question.usecase;

import org.npathai.kata.application.domain.question.QuestionId;
import org.npathai.kata.application.domain.question.answer.dto.Answer;
import org.npathai.kata.application.domain.question.answer.persistence.AnswerRepository;
import org.npathai.kata.application.domain.question.answer.request.PostAnswerRequest;
import org.npathai.kata.application.domain.question.dto.Question;
import org.npathai.kata.application.domain.question.persistence.QuestionRepository;
import org.npathai.kata.application.domain.services.IdGenerator;
import org.npathai.kata.application.domain.services.UnknownEntityException;
import org.npathai.kata.application.domain.user.UserId;

public class PostAnswerUseCase {

    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;
    private final IdGenerator answerIdGenerator;

    public PostAnswerUseCase(QuestionRepository questionRepository,
                             AnswerRepository answerRepository,
                             IdGenerator answerIdGenerator) {
        this.questionRepository = questionRepository;
        this.answerRepository = answerRepository;
        this.answerIdGenerator = answerIdGenerator;
    }

    public Answer postAnswer(UserId authorId, QuestionId questionId, PostAnswerRequest request) {
        Question question = getQuestionExplosively(questionId);
        Answer answer = new Answer();
        answer.setId(answerIdGenerator.get());
        answer.setAuthorId(authorId.getId());
        answer.setQuestionId(questionId.getId());
        answer.setBody(request.getBody());

        answerRepository.save(answer);

        question.setAnswerCount(question.getAnswerCount() + 1);
        questionRepository.save(question);

        return answer;
    }

    private Question getQuestionExplosively(QuestionId questionId) {
        return questionRepository.findById(questionId.getId())
                .orElseThrow(UnknownEntityException::new);
    }
}

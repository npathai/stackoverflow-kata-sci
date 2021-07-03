package org.npathai.kata.application.domain.question.usecase;

import org.npathai.kata.application.domain.question.dto.Question;
import org.npathai.kata.application.domain.question.persistence.QuestionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

public class GetRecentQuestionsUseCase {

    private final QuestionRepository questionRepository;

    public GetRecentQuestionsUseCase(QuestionRepository questionRepository) {
        this.questionRepository = questionRepository;
    }

    public Page<Question> getRecentQuestions() {
        return questionRepository.findAll(PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt")));
    }
}

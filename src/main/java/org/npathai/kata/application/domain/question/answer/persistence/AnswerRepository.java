package org.npathai.kata.application.domain.question.answer.persistence;

import org.npathai.kata.application.domain.question.answer.dto.Answer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnswerRepository extends JpaRepository<Answer, String> {

    List<Answer> findByQuestionId(String questionId);
}

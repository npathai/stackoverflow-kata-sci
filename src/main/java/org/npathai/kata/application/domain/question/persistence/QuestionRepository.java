package org.npathai.kata.application.domain.question.persistence;

import org.npathai.kata.application.domain.question.dto.Question;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionRepository extends JpaRepository<Question, String> {

}

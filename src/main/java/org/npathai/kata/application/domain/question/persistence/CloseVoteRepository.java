package org.npathai.kata.application.domain.question.persistence;

import org.npathai.kata.application.domain.question.dto.CloseVote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CloseVoteRepository extends JpaRepository<CloseVote, String> {

    List<CloseVote> findByQuestionId(String questionId);

    void deleteByQuestionId(String questionId);
}

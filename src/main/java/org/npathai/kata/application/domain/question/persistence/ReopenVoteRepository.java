package org.npathai.kata.application.domain.question.persistence;

import org.npathai.kata.application.domain.question.dto.ReopenVote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReopenVoteRepository extends JpaRepository<ReopenVote, String> {
    List<ReopenVote> findByQuestionId(String questionId);
}

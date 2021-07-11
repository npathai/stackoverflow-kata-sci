package org.npathai.kata.application.domain.vote;

import org.npathai.kata.application.domain.vote.dto.Vote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VoteRepository extends JpaRepository<Vote, String> {

    Vote findByVotableIdAndVoterId(String votableId, String voterId);
}

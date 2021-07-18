package org.npathai.kata.application.domain.question.usecase;

import org.npathai.kata.application.domain.question.QuestionId;
import org.npathai.kata.application.domain.question.dto.CloseVote;
import org.npathai.kata.application.domain.question.dto.CloseVoteSummary;
import org.npathai.kata.application.domain.question.persistence.CloseVoteRepository;
import org.npathai.kata.application.domain.services.IdGenerator;
import org.npathai.kata.application.domain.user.UserId;

import java.util.List;

public class QuestionCloseVotingUseCase {

    private final IdGenerator closeVoteIdGenerator;
    private final CloseVoteRepository closeVoteRepository;

    public QuestionCloseVotingUseCase(IdGenerator closeVoteIdGenerator, CloseVoteRepository closeVoteRepository) {
        this.closeVoteIdGenerator = closeVoteIdGenerator;
        this.closeVoteRepository = closeVoteRepository;
    }

    public CloseVoteSummary closeVote(UserId voterId, QuestionId questionId) {
        List<CloseVote> closeVotes = closeVoteRepository.findByQuestionId(questionId.getId());

        CloseVote closeVote = new CloseVote();
        closeVote.setQuestionId(questionId.getId());
        closeVote.setVoterId(voterId.getId());
        closeVote.setId(closeVoteIdGenerator.get());
        closeVoteRepository.save(closeVote);

        CloseVoteSummary closeVoteSummary = new CloseVoteSummary();
        closeVoteSummary.setCastVotes(closeVotes.size() + 1);
        closeVoteSummary.setRemainingVotes(4 - closeVoteSummary.getCastVotes());

        return closeVoteSummary;
    }
}

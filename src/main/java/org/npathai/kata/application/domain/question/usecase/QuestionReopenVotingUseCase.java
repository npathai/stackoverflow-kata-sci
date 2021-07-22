package org.npathai.kata.application.domain.question.usecase;

import org.npathai.kata.application.domain.question.QuestionId;
import org.npathai.kata.application.domain.question.dto.Question;
import org.npathai.kata.application.domain.question.dto.ReopenVote;
import org.npathai.kata.application.domain.question.dto.VoteSummary;
import org.npathai.kata.application.domain.question.persistence.CloseVoteRepository;
import org.npathai.kata.application.domain.question.persistence.QuestionRepository;
import org.npathai.kata.application.domain.question.persistence.ReopenVoteRepository;
import org.npathai.kata.application.domain.services.IdGenerator;
import org.npathai.kata.application.domain.user.InsufficientReputationException;
import org.npathai.kata.application.domain.user.UserId;
import org.npathai.kata.application.domain.user.UserService;
import org.npathai.kata.application.domain.user.dto.User;

import java.time.Clock;
import java.util.List;

public class QuestionReopenVotingUseCase {
    private final QuestionRepository questionRepository;
    private final IdGenerator reopenVoteIdGenerator;
    private final ReopenVoteRepository reopenVoteRepository;
    private final UserService userService;
    private final CloseVoteRepository closeVoteRepository;

    public QuestionReopenVotingUseCase(QuestionRepository questionRepository,
                                       IdGenerator reopenVoteIdGenerator,
                                       ReopenVoteRepository reopenVoteRepository,
                                       UserService userService,
                                       CloseVoteRepository closeVoteRepository) {
        this.questionRepository = questionRepository;
        this.reopenVoteIdGenerator = reopenVoteIdGenerator;
        this.reopenVoteRepository = reopenVoteRepository;
        this.closeVoteRepository = closeVoteRepository;
        this.userService = userService;
    }

    // FIXME we should not be deleting votes
    // FIXME how to handle scenario where a question is reopened and then again voted to be closed again
    public VoteSummary reopenVote(UserId voterId, QuestionId questionId) throws InsufficientReputationException {
        User voter = userService.getUserById(voterId);
        if (!voter.hasReputationToCloseVote()) {
            throw new InsufficientReputationException();
        }

        Question question = questionRepository.findById(questionId.getId()).get();
        List<ReopenVote> reopenVotes = reopenVoteRepository.findByQuestionId(questionId.getId());

        ReopenVote reopenVote = question.reopenVote(voter, reopenVotes);
        reopenVote.setId(reopenVoteIdGenerator.get());
        reopenVoteRepository.save(reopenVote);

        if (question.isOpen()) {
            questionRepository.save(question);
        }

        return new VoteSummary(reopenVotes.size(), 4 - reopenVotes.size());
    }
}

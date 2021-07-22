package org.npathai.kata.application.domain.question.usecase;

import org.npathai.kata.application.domain.question.QuestionId;
import org.npathai.kata.application.domain.question.dto.CloseVote;
import org.npathai.kata.application.domain.question.dto.VoteSummary;
import org.npathai.kata.application.domain.question.dto.Question;
import org.npathai.kata.application.domain.question.persistence.CloseVoteRepository;
import org.npathai.kata.application.domain.question.persistence.QuestionRepository;
import org.npathai.kata.application.domain.services.IdGenerator;
import org.npathai.kata.application.domain.user.InsufficientReputationException;
import org.npathai.kata.application.domain.user.UserId;
import org.npathai.kata.application.domain.user.UserService;
import org.npathai.kata.application.domain.user.dto.User;

import java.time.Clock;
import java.util.List;

public class QuestionCloseVotingUseCase {

    private final QuestionRepository questionRepository;
    private final IdGenerator closeVoteIdGenerator;
    private final CloseVoteRepository closeVoteRepository;
    private final Clock clock;
    private final UserService userService;

    public QuestionCloseVotingUseCase(QuestionRepository questionRepository,
                                      IdGenerator closeVoteIdGenerator,
                                      CloseVoteRepository closeVoteRepository,
                                      Clock clock, UserService userService) {
        this.questionRepository = questionRepository;
        this.closeVoteIdGenerator = closeVoteIdGenerator;
        this.closeVoteRepository = closeVoteRepository;
        this.clock = clock;
        this.userService = userService;
    }

    public VoteSummary closeVote(UserId voterId, QuestionId questionId) throws InsufficientReputationException {
        User voter = userService.getUserById(voterId);
        if (!voter.hasReputationToCloseVote()) {
            throw new InsufficientReputationException();
        }

        Question question = questionRepository.findById(questionId.getId()).get();
        List<CloseVote> closeVotes = closeVoteRepository.findByQuestionId(questionId.getId());

        CloseVote closeVote = question.closeVote(voterId, closeVotes, clock);
        closeVote.setId(closeVoteIdGenerator.get());
        closeVoteRepository.save(closeVote);

        if (question.isClosed()) {
            questionRepository.save(question);
        }

        return question.getCloseVoteSummary(closeVotes);
    }
}

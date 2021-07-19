package org.npathai.kata.application.domain.question.usecase;

import org.npathai.kata.application.domain.question.QuestionId;
import org.npathai.kata.application.domain.question.dto.CloseVote;
import org.npathai.kata.application.domain.question.dto.CloseVoteSummary;
import org.npathai.kata.application.domain.question.dto.Question;
import org.npathai.kata.application.domain.question.persistence.CloseVoteRepository;
import org.npathai.kata.application.domain.question.persistence.QuestionRepository;
import org.npathai.kata.application.domain.services.IdGenerator;
import org.npathai.kata.application.domain.user.UserId;

import java.time.Clock;
import java.util.List;

public class QuestionCloseVotingUseCase {

    private final QuestionRepository questionRepository;
    private final IdGenerator closeVoteIdGenerator;
    private final CloseVoteRepository closeVoteRepository;
    private final Clock clock;

    public QuestionCloseVotingUseCase(QuestionRepository questionRepository,
                                      IdGenerator closeVoteIdGenerator,
                                      CloseVoteRepository closeVoteRepository,
                                      Clock clock) {
        this.questionRepository = questionRepository;
        this.closeVoteIdGenerator = closeVoteIdGenerator;
        this.closeVoteRepository = closeVoteRepository;
        this.clock = clock;
    }

    public CloseVoteSummary closeVote(UserId voterId, QuestionId questionId) {
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

package org.npathai.kata.application.domain.question.usecase;

import org.npathai.kata.application.api.validation.BadRequestParametersException;
import org.npathai.kata.application.domain.ImpermissibleOperationException;
import org.npathai.kata.application.domain.question.QuestionId;
import org.npathai.kata.application.domain.question.dto.Question;
import org.npathai.kata.application.domain.question.persistence.QuestionRepository;
import org.npathai.kata.application.domain.services.IdGenerator;
import org.npathai.kata.application.domain.services.UnknownEntityException;
import org.npathai.kata.application.domain.user.InsufficientReputationException;
import org.npathai.kata.application.domain.user.UserId;
import org.npathai.kata.application.domain.user.UserService;
import org.npathai.kata.application.domain.user.dto.User;
import org.npathai.kata.application.domain.vote.VoteRepository;
import org.npathai.kata.application.domain.vote.VoteRequest;
import org.npathai.kata.application.domain.vote.VoteType;
import org.npathai.kata.application.domain.vote.dto.Score;
import org.npathai.kata.application.domain.vote.dto.Vote;

public class QuestionVotingUseCase {
    private final QuestionRepository questionRepository;
    private final VoteRepository voteRepository;
    private final UserService userService;
    private final IdGenerator voteIdGenerator;

    public QuestionVotingUseCase(QuestionRepository questionRepository, VoteRepository voteRepository,
                                 UserService userService, IdGenerator voteIdGenerator) {
        this.questionRepository = questionRepository;
        this.voteRepository = voteRepository;
        this.userService = userService;
        this.voteIdGenerator = voteIdGenerator;
    }

    public Score voteQuestion(UserId userId, QuestionId questionId, VoteRequest voteRequest) throws
            BadRequestParametersException, ImpermissibleOperationException, InsufficientReputationException {
        Question question = getQuestionExplosively(questionId);
        User voter = userService.getUserById(userId);
        User author = userService.getUserById(UserId.validated(question.getAuthorId()));

        if (voter.equals(author)) {
            throw new ImpermissibleOperationException("Can't cast vote on own question");
        }

        Score score = new Score();
        if (voteRequest.getType() == VoteType.UP) {
            if (voter.getReputation() < 15) {
                throw new InsufficientReputationException();
            }

            score.setScore(question.getScore() + 1);
            voter.setCastUpVotes(voter.getCastUpVotes() + 1);
            author.setReputation(author.getReputation() + 10);
        } else {
            if (voter.getReputation() < 125) {
                throw new InsufficientReputationException();
            }
            voter.setCastDownVotes(voter.getCastDownVotes() + 1);
            score.setScore(question.getScore() - 1);
            author.setReputation(author.getReputation() - 5);
        }

        userService.update(voter);
        userService.update(author);

        question.setScore(score.getScore());
        questionRepository.save(question);

        Vote vote = new Vote();
        vote.setId(voteIdGenerator.get());
        vote.setQuestionId(question.getId());
        vote.setVoterId(voter.getId());
        vote.setType(voteRequest.getType().val);

        voteRepository.save(vote);

        return score;
    }

    private Question getQuestionExplosively(QuestionId questionId) {
        return questionRepository.findById(questionId.getId())
                .orElseThrow(UnknownEntityException::new);
    }

}
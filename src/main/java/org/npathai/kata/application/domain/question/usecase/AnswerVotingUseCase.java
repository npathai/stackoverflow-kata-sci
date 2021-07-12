package org.npathai.kata.application.domain.question.usecase;

import lombok.SneakyThrows;
import org.npathai.kata.application.api.validation.BadRequestParametersException;
import org.npathai.kata.application.domain.ImpermissibleOperationException;
import org.npathai.kata.application.domain.question.answer.dto.Answer;
import org.npathai.kata.application.domain.question.answer.dto.AnswerId;
import org.npathai.kata.application.domain.question.answer.persistence.AnswerRepository;
import org.npathai.kata.application.domain.services.IdGenerator;
import org.npathai.kata.application.domain.user.InsufficientReputationException;
import org.npathai.kata.application.domain.user.UserId;
import org.npathai.kata.application.domain.user.UserService;
import org.npathai.kata.application.domain.user.dto.User;
import org.npathai.kata.application.domain.vote.VoteRepository;
import org.npathai.kata.application.domain.vote.VoteRequest;
import org.npathai.kata.application.domain.vote.VoteType;
import org.npathai.kata.application.domain.vote.dto.Score;
import org.npathai.kata.application.domain.vote.dto.Vote;

import java.util.Optional;

public class AnswerVotingUseCase {
    private final AnswerRepository answerRepository;
    private final VoteRepository voteRepository;
    private final IdGenerator voteIdGenerator;
    private final UserService userService;

    public AnswerVotingUseCase(AnswerRepository answerRepository,
                               VoteRepository voteRepository,
                               IdGenerator voteIdGenerator,
                               UserService userService) {
        this.answerRepository = answerRepository;
        this.voteRepository = voteRepository;
        this.voteIdGenerator = voteIdGenerator;
        this.userService = userService;
    }

    public Score voteAnswer(UserId voterId, AnswerId answerId, VoteRequest request) throws InsufficientReputationException, ImpermissibleOperationException, BadRequestParametersException {
        Optional<Answer> maybeAnswer = answerRepository.findById(answerId.getId());
        Answer answer = maybeAnswer.get();
        User author = userService.getUserById(UserId.validated(answer.getAuthorId()));
        User voter = userService.getUserById(voterId);

        if (voter.equals(author)) {
            throw new ImpermissibleOperationException("Can't vote on own answer");
        }

        if (request.getType() == VoteType.UP) {
            if (voter.getReputation() < 15) {
                throw new InsufficientReputationException();
            }
            answer.setScore(answer.getScore() + 1);
            author.setReputation(author.getReputation() + 10);
            voter.setCastUpVotes(voter.getCastUpVotes() + 1);
        } else {
            if (voter.getReputation() < 125) {
                throw new InsufficientReputationException();
            }
            answer.setScore(answer.getScore() - 1);
            author.setReputation(author.getReputation() - 5);
            voter.setCastDownVotes(voter.getCastDownVotes() + 1);
            voter.setReputation(voter.getReputation() - 1);
        }

        userService.update(voter);
        userService.update(author);
        answerRepository.save(answer);

        Vote vote = new Vote();
        vote.setId(voteIdGenerator.get());
        vote.setVotableId(answer.getId());
        vote.setVoterId(voter.getId());
        vote.setType(request.getType().val);
        voteRepository.save(vote);

        Score score = new Score();
        score.setScore(answer.getScore());
        return score;
    }
}

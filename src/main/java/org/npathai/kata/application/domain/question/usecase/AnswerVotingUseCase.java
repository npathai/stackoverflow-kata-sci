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

        Vote vote = answer.vote(author, voter, request.getType());
        vote.setId(voteIdGenerator.get());

        userService.update(voter);
        userService.update(author);
        answerRepository.save(answer);

        voteRepository.save(vote);

        Score score = new Score();
        score.setScore(answer.getScore());
        return score;
    }
}

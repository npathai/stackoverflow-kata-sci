package org.npathai.kata.application.domain.question.usecase;

import lombok.val;
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

        Vote vote = question.vote(voteRequest.getType(), author, voter);
        vote.setId(voteIdGenerator.get());

        userService.update(voter);
        userService.update(author);
        questionRepository.save(question);
        voteRepository.save(vote);

        Score score = new Score();
        score.setScore(question.getScore());
        return score;
    }

    private Question getQuestionExplosively(QuestionId questionId) {
        return questionRepository.findById(questionId.getId())
                .orElseThrow(UnknownEntityException::new);
    }

}

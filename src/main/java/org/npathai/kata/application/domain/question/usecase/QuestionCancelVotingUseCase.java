package org.npathai.kata.application.domain.question.usecase;

import org.npathai.kata.application.api.validation.BadRequestParametersException;
import org.npathai.kata.application.domain.question.QuestionId;
import org.npathai.kata.application.domain.question.dto.Question;
import org.npathai.kata.application.domain.question.persistence.QuestionRepository;
import org.npathai.kata.application.domain.services.UnknownEntityException;
import org.npathai.kata.application.domain.user.UserId;
import org.npathai.kata.application.domain.user.UserService;
import org.npathai.kata.application.domain.user.dto.User;
import org.npathai.kata.application.domain.vote.VoteRepository;
import org.npathai.kata.application.domain.vote.dto.Score;
import org.npathai.kata.application.domain.vote.dto.Vote;

public class QuestionCancelVotingUseCase {

    private final QuestionRepository questionRepository;
    private final VoteRepository voteRepository;
    private final UserService userService;

    public QuestionCancelVotingUseCase(QuestionRepository questionRepository, VoteRepository voteRepository,
                                       UserService userService) {
        this.questionRepository = questionRepository;
        this.voteRepository = voteRepository;
        this.userService = userService;
    }

    public Score cancelVote(UserId voterId, QuestionId questionId) throws BadRequestParametersException {
        User voter = userService.getUserById(voterId);
        Question question = getQuestionBy(questionId);
        Vote vote = voteRepository.findByVotableIdAndVoterId(question.getId(), voterId.getId());
        User author = userService.getUserById(UserId.validated(question.getAuthorId()));

        question.cancelVote(vote, author, voter);

        userService.update(voter);
        userService.update(author);
        questionRepository.save(question);
        voteRepository.delete(vote);

        Score score = new Score();
        score.setScore(question.getScore());
        return score;
    }

    private Question getQuestionBy(QuestionId questionId) {
        return questionRepository.findById(questionId.getId())
                .orElseThrow(UnknownEntityException::new);
    }
}

package org.npathai.kata.application.domain.question.usecase;

import lombok.SneakyThrows;
import org.npathai.kata.application.domain.question.answer.dto.Answer;
import org.npathai.kata.application.domain.question.answer.dto.AnswerId;
import org.npathai.kata.application.domain.question.answer.persistence.AnswerRepository;
import org.npathai.kata.application.domain.user.UserId;
import org.npathai.kata.application.domain.user.UserService;
import org.npathai.kata.application.domain.user.dto.User;
import org.npathai.kata.application.domain.vote.VoteRepository;
import org.npathai.kata.application.domain.vote.VoteType;
import org.npathai.kata.application.domain.vote.dto.Score;
import org.npathai.kata.application.domain.vote.dto.Vote;

public class AnswerCancelVotingUseCase {

    private final AnswerRepository answerRepository;
    private final VoteRepository voteRepository;
    private final UserService userService;

    public AnswerCancelVotingUseCase(AnswerRepository answerRepository,
                               VoteRepository voteRepository,
                               UserService userService) {
        this.answerRepository = answerRepository;
        this.voteRepository = voteRepository;
        this.userService = userService;
    }

    @SneakyThrows
    public Score cancelVote(UserId voterId, AnswerId answerId) {
        Answer answer = answerRepository.findById(answerId.getId()).get();
        User voter = userService.getUserById(voterId);
        User author = userService.getUserById(UserId.validated(answer.getAuthorId()));
        Vote vote = voteRepository.findByVotableIdAndVoterId(answer.getId(), voter.getId());

        answer.cancelVote(vote, author, voter);

        userService.update(author);
        userService.update(voter);
        answerRepository.save(answer);
        voteRepository.delete(vote);

        Score score = new Score();
        score.setScore(answer.getScore());
        return score;
    }
}

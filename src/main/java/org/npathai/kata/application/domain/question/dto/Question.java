package org.npathai.kata.application.domain.question.dto;

import lombok.Data;
import org.npathai.kata.application.domain.ImpermissibleOperationException;
import org.npathai.kata.application.domain.services.IdGenerator;
import org.npathai.kata.application.domain.tag.dto.Tag;
import org.npathai.kata.application.domain.user.InsufficientReputationException;
import org.npathai.kata.application.domain.user.dto.User;
import org.npathai.kata.application.domain.vote.VoteRequest;
import org.npathai.kata.application.domain.vote.VoteType;
import org.npathai.kata.application.domain.vote.dto.Vote;

import javax.persistence.*;
import java.util.List;

@Data
@Entity
@Table(name = "questions")
public class Question {
    @Id
    private String id;
    private String title;
    private String body;
    @ManyToMany
    @JoinColumn(name = "id")
    private List<Tag> tags;
    private long createdAt;
    private String authorId;
    private int answerCount;
    private int score;

    public Vote upVote(User author, User voter) throws ImpermissibleOperationException, InsufficientReputationException {
        if (voter.equals(author)) {
            throw new ImpermissibleOperationException("Can't cast vote on own question");
        }

        if (voter.getReputation() < 15) {
            throw new InsufficientReputationException();
        }

        setScore(getScore() + 1);

        return createVote(VoteType.UP, this, voter);
    }

    public Vote downVote(User author, User voter) throws ImpermissibleOperationException, InsufficientReputationException {
        if (voter.equals(author)) {
            throw new ImpermissibleOperationException("Can't cast vote on own question");
        }

        if (voter.getReputation() < 125) {
            throw new InsufficientReputationException();
        }

        setScore(getScore() - 1);

        return createVote(VoteType.DOWN, this, voter);
    }

    private Vote createVote(VoteType voteType, Question question, User voter) {
        Vote vote = new Vote();
        vote.setQuestionId(question.getId());
        vote.setVoterId(voter.getId());
        vote.setType(voteType.val);
        return vote;
    }
}

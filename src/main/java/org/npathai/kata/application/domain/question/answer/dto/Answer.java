package org.npathai.kata.application.domain.question.answer.dto;

import lombok.Data;
import org.npathai.kata.application.domain.ImpermissibleOperationException;
import org.npathai.kata.application.domain.services.PersistedEntity;
import org.npathai.kata.application.domain.user.InsufficientReputationException;
import org.npathai.kata.application.domain.user.dto.User;
import org.npathai.kata.application.domain.vote.VoteType;
import org.npathai.kata.application.domain.vote.dto.Vote;

import javax.persistence.*;

@Data
@Entity
@PersistedEntity
@Table(name = "answers")
public class Answer {
    @Id
    private String id;
    private String body;
    @JoinColumn(table = "users", name = "id")
    private String authorId;
    @JoinColumn(table = "questions", name = "id")
    private String questionId;
    private int score;

    public Vote vote(User author, User voter, VoteType type) throws ImpermissibleOperationException, InsufficientReputationException {
        if (voter.equals(author)) {
            throw new ImpermissibleOperationException("Can't vote on own answer");
        }

        if (type == VoteType.UP) {
            return upVote(author, voter);
        } else {
            return downVote(author, voter);
        }
    }

    private Vote downVote(User author, User voter) throws InsufficientReputationException {
        if (voter.getReputation() < 125) {
            throw new InsufficientReputationException();
        }
        setScore(getScore() - 1);
        author.setReputation(author.getReputation() - 5);
        voter.setCastDownVotes(voter.getCastDownVotes() + 1);
        voter.setReputation(voter.getReputation() - 1);

        return aVote(VoteType.DOWN, voter);
    }

    private Vote upVote(User author, User voter) throws InsufficientReputationException {
        if (voter.getReputation() < 15) {
            throw new InsufficientReputationException();
        }
        setScore(getScore() + 1);
        author.setReputation(author.getReputation() + 10);
        voter.setCastUpVotes(voter.getCastUpVotes() + 1);

        return aVote(VoteType.UP, voter);
    }

    private Vote aVote(VoteType type, User voter) {
        Vote vote = new Vote();
        vote.setVotableId(getId());
        vote.setVoterId(voter.getId());
        vote.setType(type.val);
        return vote;
    }
}

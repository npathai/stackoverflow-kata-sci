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
    public static final int UP_VOTE_AUTHOR_REP_GAIN = 10;
    public static final int DOWN_VOTE_AUTHOR_REP_LOSS = 5;
    public static final int DOWN_VOTE_VOTER_REP_LOSS = 1;
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
        if (!voter.hasReputationToDownVote()) {
            throw new InsufficientReputationException();
        }
        decrementScore();
        voter.incrementCastDownVotes();
        author.decrementReputationBy(DOWN_VOTE_AUTHOR_REP_LOSS);
        voter.decrementReputationBy(DOWN_VOTE_VOTER_REP_LOSS);

        return aVote(VoteType.DOWN, voter);
    }

    private void decrementScore() {
        setScore(getScore() - 1);
    }

    private Vote upVote(User author, User voter) throws InsufficientReputationException {
        if (!voter.hasReputationToUpVote()) {
            throw new InsufficientReputationException();
        }
        incrementScore();
        voter.incrementCastUpVotes();
        author.incrementReputationBy(UP_VOTE_AUTHOR_REP_GAIN);

        return aVote(VoteType.UP, voter);
    }

    private void incrementScore() {
        setScore(getScore() + 1);
    }

    private Vote aVote(VoteType type, User voter) {
        Vote vote = new Vote();
        vote.setVotableId(getId());
        vote.setVoterId(voter.getId());
        vote.setType(type.val);
        return vote;
    }

    public void cancelVote(Vote vote, User author, User voter) {
        if (VoteType.from(vote.getType()) == VoteType.UP) {
            decrementScore();
            voter.decrementCastUpVotes();
            author.decrementReputationBy(UP_VOTE_AUTHOR_REP_GAIN);
        } else {
            incrementScore();
            voter.decrementCastDownVotes();
            author.incrementReputationBy(DOWN_VOTE_AUTHOR_REP_LOSS);
            voter.incrementReputationBy(DOWN_VOTE_VOTER_REP_LOSS);
        }
    }
}

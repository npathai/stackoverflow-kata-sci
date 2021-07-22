package org.npathai.kata.application.domain.question.dto;

import lombok.Data;
import org.npathai.kata.application.domain.ImpermissibleOperationException;
import org.npathai.kata.application.domain.tag.dto.Tag;
import org.npathai.kata.application.domain.user.InsufficientReputationException;
import org.npathai.kata.application.domain.user.UserId;
import org.npathai.kata.application.domain.user.dto.User;
import org.npathai.kata.application.domain.vote.VoteType;
import org.npathai.kata.application.domain.vote.dto.Vote;

import javax.persistence.*;
import java.time.Clock;
import java.util.List;

@Data
@Entity
@Table(name = "questions")
public class Question {
    private static final int UP_VOTE_REP_GAIN = 10;
    private static final int DOWN_VOTE_REP_LOSS = 5;

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
    private Long closedAt;

    public Vote vote(VoteType type, User author, User voter) throws ImpermissibleOperationException, InsufficientReputationException {
        if (voter.equals(author)) {
            throw new ImpermissibleOperationException("Can't cast vote on own question");
        }

        if (type == VoteType.UP) {
            return upVote(author, voter);
        } else {
            return downVote(author, voter);
        }
    }

    private Vote upVote(User author, User voter) throws InsufficientReputationException {
        if (!voter.hasReputationToUpVote()) {
            throw new InsufficientReputationException();
        }

        incrementScore();
        voter.incrementCastUpVotes();
        author.incrementReputationBy(UP_VOTE_REP_GAIN);

        return aVote(VoteType.UP, voter);
    }

    private Vote downVote(User author, User voter) throws InsufficientReputationException {
        if (!voter.hasReputationToDownVote()) {
            throw new InsufficientReputationException();
        }

        decrementScore();
        voter.incrementCastDownVotes();
        author.decrementReputationBy(DOWN_VOTE_REP_LOSS);

        return aVote(VoteType.DOWN, voter);
    }

    public void cancelVote(Vote vote, User author, User voter) {
        if (VoteType.from(vote.getType()) == VoteType.UP) {
            decrementScore();
            voter.decrementCastUpVotes();
            author.decrementReputationBy(UP_VOTE_REP_GAIN);
        } else {
            incrementScore();
            voter.decrementCastDownVotes();
            author.incrementReputationBy(DOWN_VOTE_REP_LOSS);
        }
    }

    private void incrementScore() {
        score += 1;
    }

    private void decrementScore() {
        score -= 1;
    }

    private Vote aVote(VoteType voteType, User voter) {
        Vote vote = new Vote();
        vote.setVotableId(getId());
        vote.setVoterId(voter.getId());
        vote.setType(voteType.val);
        return vote;
    }

    public CloseVote closeVote(UserId voterId, List<CloseVote> closeVotes, Clock clock) {
        CloseVote closeVote = new CloseVote();
        closeVote.setQuestionId(getId());
        closeVote.setVoterId(voterId.getId());
        if (closeVotes.size() + 1 == 4) {
            setClosedAt(clock.millis());
        }
        // TODO check if there is a better approach for this
        closeVotes.add(closeVote);
        return closeVote;
    }

    public boolean isClosed() {
        return closedAt != null;
    }

    public VoteSummary getCloseVoteSummary(List<CloseVote> closeVotes) {
        VoteSummary voteSummary = new VoteSummary();
        voteSummary.setCastVotes(closeVotes.size());
        voteSummary.setRemainingVotes(4 - voteSummary.getCastVotes());
        return voteSummary;
    }

    public ReopenVote reopenVote(User voter, List<ReopenVote> reopenVotes) {
        if (reopenVotes.size() == 3) {
            setClosedAt(null);
        }

        ReopenVote reopenVote = new ReopenVote();
        reopenVote.setVoterId(voter.getId());
        reopenVote.setQuestionId(getId());
        reopenVotes.add(reopenVote);
        return reopenVote;
    }

    public boolean isOpen() {
        return !isClosed();
    }
}

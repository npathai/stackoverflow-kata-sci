package org.npathai.kata.application.domain.user.dto;

import lombok.Data;
import org.npathai.kata.application.domain.services.PersistedEntity;
import org.npathai.kata.application.domain.user.InsufficientReputationException;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@PersistedEntity
@Entity
@Table(name = "users")
public class User {
    @Id
    private String id;
    private String username;
    private String email;
    private long reputation;
    private int castUpVotes;
    private int castDownVotes;

    public boolean hasReputationToUpVote() {
        return getReputation() >= 15;
    }

    public boolean hasReputationToDownVote() {
        return getReputation() >= 125;
    }

    public void incrementCastUpVotes() {
        castUpVotes += 1;
    }

    public void incrementReputationBy(int delta) {
        setReputation(getReputation() + delta);
    }

    public void incrementCastDownVotes() {
        setCastDownVotes(getCastDownVotes() + 1);
    }

    public void decrementReputationBy(int delta) {
        setReputation(Math.max(1, getReputation() - delta));
    }

    public void decrementCastUpVotes() {
        setCastUpVotes(getCastUpVotes() - 1);
    }

    public void decrementCastDownVotes() {
        setCastDownVotes(getCastDownVotes() - 1);
    }
}

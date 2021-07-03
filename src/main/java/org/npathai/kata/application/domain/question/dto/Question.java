package org.npathai.kata.application.domain.question.dto;

import lombok.Data;
import org.npathai.kata.application.domain.services.IdGenerator;
import org.npathai.kata.application.domain.tag.dto.Tag;
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

    public Vote upVote(User author, User voter) {
        return createVote(VoteType.UP, this, voter);
    }

    public Vote downVote(User author, User voter) {
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

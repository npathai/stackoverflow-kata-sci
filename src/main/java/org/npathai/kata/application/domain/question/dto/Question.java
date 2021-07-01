package org.npathai.kata.application.domain.question.dto;

import lombok.Data;
import org.npathai.kata.application.domain.tag.dto.Tag;

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
}

package org.npathai.kata.application.domain.question.dto;

import lombok.Data;
import org.npathai.kata.application.domain.tag.dto.Tag;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
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
    private List<Tag> tags;
    private long createdAt;
    private String authorId;
}

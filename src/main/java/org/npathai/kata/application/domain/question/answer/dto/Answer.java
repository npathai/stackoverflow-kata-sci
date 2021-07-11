package org.npathai.kata.application.domain.question.answer.dto;

import lombok.Data;
import org.npathai.kata.application.domain.services.PersistedEntity;

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
}

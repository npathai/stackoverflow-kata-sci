package org.npathai.kata.application.domain.vote.dto;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Table;

@Data
@Entity
@Table(name = "votes")
public class Vote {
    @Id
    private String id;

    @JoinColumn(table = "questions", name = "id")
    private String questionId;

    @JoinColumn(table = "users", name = "id")
    private String voterId;

    private String type;
}

package org.npathai.kata.application.domain.question.dto;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@Entity
@Table(name = "close_votes")
public class CloseVote {
    @Id
    private String id;
    private String voterId;
    private String questionId;
}

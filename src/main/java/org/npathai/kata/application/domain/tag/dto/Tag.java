package org.npathai.kata.application.domain.tag.dto;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@Entity
@Table(name = "tags")
public class Tag {
    @Id
    private String id;
    private String name;
}

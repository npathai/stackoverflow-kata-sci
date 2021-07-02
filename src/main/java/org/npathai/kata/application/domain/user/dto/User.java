package org.npathai.kata.application.domain.user.dto;

import lombok.Data;
import org.npathai.kata.application.domain.services.PersistedEntity;

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
}

package org.npathai.kata.application.domain.user.dto;

import lombok.Data;

@Data
public class User {
    private String id;
    private String username;
    private String email;
    private long reputation;
}

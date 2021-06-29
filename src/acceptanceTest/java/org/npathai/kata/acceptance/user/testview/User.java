package org.npathai.kata.acceptance.user.testview;

import lombok.Data;

@Data
public class User {
    private String id;
    private String username;
    private String email;
    private int reputation;
}

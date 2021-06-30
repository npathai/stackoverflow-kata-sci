package org.npathai.kata.application.domain.user;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.npathai.kata.application.domain.services.ValueObject;

@ValueObject
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode
public class UserId {
    String id;

    public static UserId validated(String id) {
        return new UserId(id);
    }
}

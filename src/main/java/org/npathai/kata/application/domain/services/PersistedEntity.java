package org.npathai.kata.application.domain.services;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface PersistedEntity {

}

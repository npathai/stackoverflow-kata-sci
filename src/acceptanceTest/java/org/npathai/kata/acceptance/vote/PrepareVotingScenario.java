package org.npathai.kata.acceptance.vote;

import org.springframework.test.context.jdbc.Sql;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Sql("/data/init_users_voting_feature.sql")
public @interface PrepareVotingScenario {

}

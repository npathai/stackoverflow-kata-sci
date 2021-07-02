package org.npathai.kata.acceptance.vote;

import org.junit.jupiter.api.Test;
import org.npathai.kata.acceptance.base.AcceptanceTest;
import org.npathai.kata.acceptance.base.ClearTables;
import org.springframework.test.context.jdbc.Sql;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Sql(value = {"/data/init_users_voting_feature.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@AcceptanceTest
public @interface VotingScenarioAcceptanceTest {

}

package org.npathai.kata.acceptance.base;

import org.springframework.test.context.jdbc.Sql;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Sql(value = {"/data/clearAll.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public @interface ClearTables {

}

package org.npathai.kata.application.util;

import org.junit.jupiter.params.provider.ValueSource;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@ValueSource(strings = {
        "",
        " ",
        "\n",
        "\t"
})
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface WhitespaceSource {
}

package org.npathai.kata.application.api.validation;

@FunctionalInterface
public interface Validator<T> {
    void validate(T val) throws BadRequestParametersException;
}

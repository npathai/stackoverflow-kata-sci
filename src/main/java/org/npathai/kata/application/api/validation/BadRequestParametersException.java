package org.npathai.kata.application.api.validation;

public class BadRequestParametersException extends Exception {

    public BadRequestParametersException() {
    }

    public BadRequestParametersException(Throwable cause) {
        super(cause);
    }
}

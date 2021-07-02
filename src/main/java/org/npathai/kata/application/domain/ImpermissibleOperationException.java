package org.npathai.kata.application.domain;

public class ImpermissibleOperationException extends Exception {

    public ImpermissibleOperationException(String message) {
        super(message);
    }
}

package com.doodle.scheduler.exception;

public class EmailAlreadyInUseException extends RuntimeException {

    public EmailAlreadyInUseException(final String message) {
        super(message);
    }
}

package com.doodle.scheduler.exception;

public class UsernameAlreadyInUseException extends RuntimeException {

    public UsernameAlreadyInUseException(final String message) {
        super(message);
    }
}

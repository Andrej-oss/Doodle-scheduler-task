package com.doodle.scheduler.exception;

public class ConflictException extends RuntimeException {

    public ConflictException(final String message) {
        super(message);
    }
}

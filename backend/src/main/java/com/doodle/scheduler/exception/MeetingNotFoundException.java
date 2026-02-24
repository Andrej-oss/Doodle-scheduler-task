package com.doodle.scheduler.exception;

public class MeetingNotFoundException extends RuntimeException {

    public MeetingNotFoundException(final String message) {
        super(message);
    }
}

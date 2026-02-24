package com.doodle.scheduler.exception;

public class CalendarNotFoundException extends RuntimeException {

    public CalendarNotFoundException(final String message) {
        super(message);
    }
}

package com.doodle.scheduler.exception;

public class SlotNotFoundException extends RuntimeException {

    public SlotNotFoundException(final String message) {
        super(message);
    }
}

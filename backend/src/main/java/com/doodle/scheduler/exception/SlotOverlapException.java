package com.doodle.scheduler.exception;

public class SlotOverlapException extends RuntimeException {

    public SlotOverlapException(final String message) {
        super(message);
    }
}

package com.doodle.scheduler.exception;

public class SlotAlreadyBusyException extends RuntimeException {

    public SlotAlreadyBusyException(final String message) {
        super(message);
    }
}

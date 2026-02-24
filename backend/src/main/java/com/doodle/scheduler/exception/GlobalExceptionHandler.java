package com.doodle.scheduler.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ServerWebExchange;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({
            UserNotFoundException.class,
            CalendarNotFoundException.class,
            SlotNotFoundException.class,
            MeetingNotFoundException.class
    })
    public ProblemDetail handleNotFound(final RuntimeException ex, final ServerWebExchange exchange) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler({
            EmailAlreadyInUseException.class,
            UsernameAlreadyInUseException.class,
            SlotOverlapException.class,
            SlotAlreadyBusyException.class,
            SlotLinkedToMeetingException.class
    })
    public ProblemDetail handleConflict(final RuntimeException ex, final ServerWebExchange exchange) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleBadRequest(final IllegalArgumentException ex, final ServerWebExchange exchange) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
    }
}

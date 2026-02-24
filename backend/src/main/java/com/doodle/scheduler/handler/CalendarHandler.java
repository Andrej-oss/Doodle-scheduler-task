package com.doodle.scheduler.handler;

import com.doodle.scheduler.domain.Calendar;
import com.doodle.scheduler.dto.CreateCalendarRequest;
import com.doodle.scheduler.service.CalendarService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CalendarHandler {

    private static final String PATH_CALENDAR_ID = "calendarId";
    private static final String PATH_USER_ID = "userId";

    private final CalendarService calendarService;

    public Mono<ServerResponse> create(@NonNull final ServerRequest request) {
        return request.bodyToMono(CreateCalendarRequest.class)
                .flatMap(calendarService::create)
                .flatMap(calendar -> ServerResponse.status(HttpStatus.CREATED).bodyValue(calendar));
    }

    public Mono<ServerResponse> findById(@NonNull final ServerRequest request) {
        final var id = UUID.fromString(request.pathVariable(PATH_CALENDAR_ID));
        return calendarService.findById(id)
                .flatMap(calendar -> ServerResponse.ok().bodyValue(calendar));
    }

    public Mono<ServerResponse> findByUserId(@NonNull final ServerRequest request) {
        final var userId = UUID.fromString(request.pathVariable(PATH_USER_ID));
        return ServerResponse.ok().body(calendarService.findByUserId(userId), Calendar.class);
    }
}

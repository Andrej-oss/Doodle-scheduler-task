package com.doodle.scheduler.handler;

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

    private final CalendarService calendarService;

    public Mono<ServerResponse> create(@NonNull final ServerRequest request) {
        return request.bodyToMono(CreateCalendarRequest.class)
                .flatMap(calendarService::create)
                .flatMap(calendar -> ServerResponse.status(HttpStatus.CREATED).bodyValue(calendar));
    }

    public Mono<ServerResponse> findById(@NonNull final ServerRequest request) {
        final UUID id = UUID.fromString(request.pathVariable("calendarId"));
        return calendarService.findById(id)
                .flatMap(calendar -> ServerResponse.ok().bodyValue(calendar));
    }

    public Mono<ServerResponse> findByUserId(@NonNull final ServerRequest request) {
        final UUID userId = UUID.fromString(request.pathVariable("userId"));
        return ServerResponse.ok().body(calendarService.findByUserId(userId), com.doodle.scheduler.domain.Calendar.class);
    }
}

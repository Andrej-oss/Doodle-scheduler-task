package com.doodle.scheduler.handler;

import com.doodle.scheduler.domain.SlotStatus;
import com.doodle.scheduler.domain.TimeSlot;
import com.doodle.scheduler.dto.AvailabilityResponse;
import com.doodle.scheduler.dto.CreateSlotRequest;
import com.doodle.scheduler.dto.UpdateSlotRequest;
import com.doodle.scheduler.service.TimeSlotService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TimeSlotHandler {

    private static final String PATH_CALENDAR_ID = "calendarId";
    private static final String PATH_SLOT_ID = "slotId";
    private static final String PATH_USER_ID = "userId";
    private static final String QUERY_STATUS = "status";
    private static final String QUERY_FROM = "from";
    private static final String QUERY_TO = "to";

    private final TimeSlotService timeSlotService;

    public Mono<ServerResponse> create(@NonNull final ServerRequest request) {
        final var calendarId = UUID.fromString(request.pathVariable(PATH_CALENDAR_ID));
        return request.bodyToMono(CreateSlotRequest.class)
                .flatMap(req -> timeSlotService.create(calendarId, req))
                .flatMap(slot -> ServerResponse.status(HttpStatus.CREATED).bodyValue(slot));
    }

    public Mono<ServerResponse> update(@NonNull final ServerRequest request) {
        final var slotId = UUID.fromString(request.pathVariable(PATH_SLOT_ID));
        return request.bodyToMono(UpdateSlotRequest.class)
                .flatMap(req -> timeSlotService.update(slotId, req))
                .flatMap(slot -> ServerResponse.ok().bodyValue(slot));
    }

    public Mono<ServerResponse> delete(@NonNull final ServerRequest request) {
        final var slotId = UUID.fromString(request.pathVariable(PATH_SLOT_ID));
        return timeSlotService.delete(slotId)
                .then(ServerResponse.noContent().build());
    }

    public Mono<ServerResponse> findByCalendar(@NonNull final ServerRequest request) {
        final var calendarId = UUID.fromString(request.pathVariable(PATH_CALENDAR_ID));
        final var status = request.queryParam(QUERY_STATUS)
                .map(SlotStatus::valueOf)
                .orElse(null);
        final var from = request.queryParam(QUERY_FROM)
                .map(LocalDateTime::parse)
                .orElse(null);
        final var to = request.queryParam(QUERY_TO)
                .map(LocalDateTime::parse)
                .orElse(null);
        return ServerResponse.ok().body(timeSlotService.findByCalendar(calendarId, status, from, to), TimeSlot.class);
    }

    public Mono<ServerResponse> getAvailability(@NonNull final ServerRequest request) {
        final var userId = UUID.fromString(request.pathVariable(PATH_USER_ID));
        final var from = request.queryParam(QUERY_FROM)
                .map(LocalDateTime::parse)
                .orElse(LocalDateTime.now());
        final var to = request.queryParam(QUERY_TO)
                .map(LocalDateTime::parse)
                .orElse(LocalDateTime.now().plusDays(7));
        return ServerResponse.ok().body(
                timeSlotService.getAvailability(userId, from, to),
                AvailabilityResponse.class
        );
    }
}

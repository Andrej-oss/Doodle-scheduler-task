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

    private final TimeSlotService timeSlotService;

    public Mono<ServerResponse> create(@NonNull final ServerRequest request) {
        final UUID calendarId = UUID.fromString(request.pathVariable("calendarId"));
        return request.bodyToMono(CreateSlotRequest.class)
                .flatMap(req -> timeSlotService.create(calendarId, req))
                .flatMap(slot -> ServerResponse.status(HttpStatus.CREATED).bodyValue(slot));
    }

    public Mono<ServerResponse> update(@NonNull final ServerRequest request) {
        final UUID slotId = UUID.fromString(request.pathVariable("slotId"));
        return request.bodyToMono(UpdateSlotRequest.class)
                .flatMap(req -> timeSlotService.update(slotId, req))
                .flatMap(slot -> ServerResponse.ok().bodyValue(slot));
    }

    public Mono<ServerResponse> delete(@NonNull final ServerRequest request) {
        final UUID slotId = UUID.fromString(request.pathVariable("slotId"));
        return timeSlotService.delete(slotId)
                .then(ServerResponse.noContent().build());
    }

    public Mono<ServerResponse> findByCalendar(@NonNull final ServerRequest request) {
        final UUID calendarId = UUID.fromString(request.pathVariable("calendarId"));
        final SlotStatus status = request.queryParam("status")
                .map(SlotStatus::valueOf)
                .orElse(null);
        final LocalDateTime from = request.queryParam("from")
                .map(LocalDateTime::parse)
                .orElse(null);
        final LocalDateTime to = request.queryParam("to")
                .map(LocalDateTime::parse)
                .orElse(null);
        return ServerResponse.ok().body(timeSlotService.findByCalendar(calendarId, status, from, to), TimeSlot.class);
    }

    public Mono<ServerResponse> getAvailability(@NonNull final ServerRequest request) {
        final UUID userId = UUID.fromString(request.pathVariable("userId"));
        final LocalDateTime from = request.queryParam("from")
                .map(LocalDateTime::parse)
                .orElse(LocalDateTime.now());
        final LocalDateTime to = request.queryParam("to")
                .map(LocalDateTime::parse)
                .orElse(LocalDateTime.now().plusDays(7));
        return ServerResponse.ok().body(
                timeSlotService.getAvailability(userId, from, to),
                AvailabilityResponse.class
        );
    }
}

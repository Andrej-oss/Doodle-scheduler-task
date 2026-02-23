package com.doodle.scheduler.handler;

import com.doodle.scheduler.dto.CreateMeetingRequest;
import com.doodle.scheduler.dto.MeetingResponse;
import com.doodle.scheduler.service.MeetingService;
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
public class MeetingHandler {

    private final MeetingService meetingService;

    public Mono<ServerResponse> schedule(@NonNull final ServerRequest request) {
        return request.bodyToMono(CreateMeetingRequest.class)
                .flatMap(meetingService::schedule)
                .flatMap(meeting -> ServerResponse.status(HttpStatus.CREATED).bodyValue(meeting));
    }

    public Mono<ServerResponse> findById(@NonNull final ServerRequest request) {
        final UUID meetingId = UUID.fromString(request.pathVariable("meetingId"));
        return meetingService.findById(meetingId)
                .flatMap(meeting -> ServerResponse.ok().bodyValue(meeting));
    }

    public Mono<ServerResponse> findByUser(@NonNull final ServerRequest request) {
        final UUID userId = UUID.fromString(request.pathVariable("userId"));
        return ServerResponse.ok().body(meetingService.findByUser(userId), MeetingResponse.class);
    }
}

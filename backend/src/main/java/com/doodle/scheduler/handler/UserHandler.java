package com.doodle.scheduler.handler;

import com.doodle.scheduler.dto.CreateUserRequest;
import com.doodle.scheduler.service.UserService;
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
public class UserHandler {

    private final UserService userService;

    public Mono<ServerResponse> create(@NonNull final ServerRequest request) {
        return request.bodyToMono(CreateUserRequest.class)
                .flatMap(userService::create)
                .flatMap(user -> ServerResponse.status(HttpStatus.CREATED).bodyValue(user));
    }

    public Mono<ServerResponse> findById(@NonNull final ServerRequest request) {
        final UUID id = UUID.fromString(request.pathVariable("userId"));
        return userService.findById(id)
                .flatMap(user -> ServerResponse.ok().bodyValue(user));
    }
}

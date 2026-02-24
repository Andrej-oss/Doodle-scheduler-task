package com.doodle.scheduler.handler;

import com.doodle.scheduler.domain.User;
import com.doodle.scheduler.dto.CreateUserRequest;
import com.doodle.scheduler.service.UserService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UserHandler {

    private static final String PATH_USER_ID = "userId";
    private static final String QUERY_PARAM_Q = "q";
    private static final int MIN_QUERY_LENGTH = 2;

    private final UserService userService;

    public Mono<ServerResponse> create(@NonNull final ServerRequest request) {
        return request.bodyToMono(CreateUserRequest.class)
                .flatMap(userService::create)
                .flatMap(user -> ServerResponse.status(HttpStatus.CREATED).bodyValue(user));
    }

    public Mono<ServerResponse> findById(@NonNull final ServerRequest request) {
        final var id = UUID.fromString(request.pathVariable(PATH_USER_ID));
        return userService.findById(id)
                .flatMap(user -> ServerResponse.ok().bodyValue(user));
    }

    public Mono<ServerResponse> search(@NonNull final ServerRequest request) {
        final var q = request.queryParam(QUERY_PARAM_Q).orElse("").trim();
        if (q.length() < MIN_QUERY_LENGTH) {
            return ServerResponse.ok().bodyValue(List.of());
        }
        return ServerResponse.ok().body(userService.search(q), User.class);
    }
}

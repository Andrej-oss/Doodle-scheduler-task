package com.doodle.scheduler.service;

import com.doodle.scheduler.domain.User;
import com.doodle.scheduler.dto.CreateUserRequest;
import com.doodle.scheduler.exception.EmailAlreadyInUseException;
import com.doodle.scheduler.exception.UserNotFoundException;
import com.doodle.scheduler.exception.UsernameAlreadyInUseException;
import com.doodle.scheduler.repository.UserRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private static final String ERR_EMAIL_IN_USE = "Email already in use: ";
    private static final String ERR_USERNAME_IN_USE = "Username already in use: ";
    private static final String ERR_USER_NOT_FOUND = "User not found: ";

    private final UserRepository userRepository;

    public Mono<User> create(@NonNull final CreateUserRequest request) {
        log.info("Creating user: username={}, email={}", request.username(), request.email());
        return userRepository.existsByEmail(request.email())
                .flatMap(emailExists -> {
                    if (emailExists) {
                        log.warn("Email already in use: {}", request.email());
                        return Mono.error(new EmailAlreadyInUseException(ERR_EMAIL_IN_USE + request.email()));
                    }
                    return userRepository.existsByUsername(request.username());
                })
                .flatMap(usernameExists -> {
                    if (usernameExists) {
                        log.warn("Username already in use: {}", request.username());
                        return Mono.error(new UsernameAlreadyInUseException(ERR_USERNAME_IN_USE + request.username()));
                    }
                    final var user = User.builder()
                            .username(request.username())
                            .email(request.email())
                            .build();
                    return userRepository.save(user);
                })
                .doOnSuccess(u -> log.info("User created: id={}, username={}", u.id(), u.username()));
    }

    public Mono<User> findById(@NonNull final UUID id) {
        log.debug("Finding user by id={}", id);
        return userRepository.findById(id)
                .switchIfEmpty(Mono.error(new UserNotFoundException(ERR_USER_NOT_FOUND + id)));
    }

    public Flux<User> search(@NonNull final String query) {
        log.debug("Searching users: query={}", query);
        return userRepository.searchByUsernameOrEmail(query);
    }
}

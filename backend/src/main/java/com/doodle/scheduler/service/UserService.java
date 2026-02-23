package com.doodle.scheduler.service;

import com.doodle.scheduler.domain.User;
import com.doodle.scheduler.dto.CreateUserRequest;
import com.doodle.scheduler.exception.ConflictException;
import com.doodle.scheduler.exception.NotFoundException;
import com.doodle.scheduler.repository.UserRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public Mono<User> create(@NonNull final CreateUserRequest request) {
        return userRepository.existsByEmail(request.email())
                .flatMap(emailExists -> {
                    if (emailExists) {
                        return Mono.error(new ConflictException("Email already in use: " + request.email()));
                    }
                    return userRepository.existsByUsername(request.username());
                })
                .flatMap(usernameExists -> {
                    if (usernameExists) {
                        return Mono.error(new ConflictException("Username already in use: " + request.username()));
                    }
                    final User user = User.builder()
                            .username(request.username())
                            .email(request.email())
                            .build();
                    return userRepository.save(user);
                });
    }

    public Mono<User> findById(@NonNull final UUID id) {
        return userRepository.findById(id)
                .switchIfEmpty(Mono.error(new NotFoundException("User not found: " + id)));
    }
}

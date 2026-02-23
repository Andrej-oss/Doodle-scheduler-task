package com.doodle.scheduler.repository;

import com.doodle.scheduler.domain.User;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface UserRepository extends ReactiveCrudRepository<User, UUID> {

    Mono<Boolean> existsByEmail(String email);

    Mono<Boolean> existsByUsername(String username);
}

package com.doodle.scheduler.repository;

import com.doodle.scheduler.domain.User;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface UserRepository extends ReactiveCrudRepository<User, UUID> {

    Mono<Boolean> existsByEmail(String email);

    Mono<Boolean> existsByUsername(String username);

    @Query("SELECT * FROM users WHERE username ILIKE '%' || :q || '%' OR email ILIKE '%' || :q || '%' LIMIT 10")
    Flux<User> searchByUsernameOrEmail(String q);
}

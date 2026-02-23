package com.doodle.scheduler.repository;

import com.doodle.scheduler.domain.Calendar;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.util.UUID;

@Repository
public interface CalendarRepository extends ReactiveCrudRepository<Calendar, UUID> {

    Flux<Calendar> findAllByUserId(UUID userId);
}

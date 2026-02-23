package com.doodle.scheduler.repository;

import com.doodle.scheduler.domain.Meeting;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.util.UUID;

@Repository
public interface MeetingRepository extends ReactiveCrudRepository<Meeting, UUID> {

    Flux<Meeting> findAllByOrganizerId(UUID organizerId);
}

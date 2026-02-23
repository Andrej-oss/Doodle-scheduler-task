package com.doodle.scheduler.repository;

import com.doodle.scheduler.domain.MeetingParticipant;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.util.UUID;

@Repository
public interface MeetingParticipantRepository extends ReactiveCrudRepository<MeetingParticipant, UUID> {

    Flux<MeetingParticipant> findAllByMeetingId(UUID meetingId);
}

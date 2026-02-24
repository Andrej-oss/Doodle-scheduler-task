package com.doodle.scheduler.repository;

import com.doodle.scheduler.domain.SlotStatus;
import com.doodle.scheduler.domain.TimeSlot;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public interface TimeSlotRepository extends ReactiveCrudRepository<TimeSlot, UUID> {

    @Query("""
            SELECT * FROM time_slots
            WHERE calendar_id = :calendarId
              AND (:status IS NULL OR status = :status)
              AND (:from IS NULL OR start_time >= :from)
              AND (:to IS NULL OR end_time <= :to)
            ORDER BY start_time
            """)
    Flux<TimeSlot> findByCalendarIdWithFilters(UUID calendarId,
                                               String status,
                                               LocalDateTime from,
                                               LocalDateTime to);

    @Query("""
            SELECT COUNT(*) FROM time_slots
            WHERE calendar_id = :calendarId
              AND start_time < :endTime
              AND end_time > :startTime
            """)
    Mono<Long> countOverlapping(UUID calendarId, LocalDateTime startTime, LocalDateTime endTime);

    @Query("""
            SELECT ts.* FROM time_slots ts
            JOIN calendars c ON c.id = ts.calendar_id
            WHERE c.user_id = :userId
              AND start_time >= :from
              AND end_time <= :to
            ORDER BY start_time
            """)
    Flux<TimeSlot> findByUserIdAndTimeRange(UUID userId, LocalDateTime from, LocalDateTime to);
}

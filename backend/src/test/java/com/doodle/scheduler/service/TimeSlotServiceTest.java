package com.doodle.scheduler.service;

import com.doodle.scheduler.domain.SlotStatus;
import com.doodle.scheduler.domain.TimeSlot;
import com.doodle.scheduler.dto.CreateSlotRequest;
import com.doodle.scheduler.exception.CalendarNotFoundException;
import com.doodle.scheduler.exception.SlotLinkedToMeetingException;
import com.doodle.scheduler.exception.SlotNotFoundException;
import com.doodle.scheduler.exception.SlotOverlapException;
import com.doodle.scheduler.repository.CalendarRepository;
import com.doodle.scheduler.repository.TimeSlotRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TimeSlotServiceTest {

    private static final Clock FIXED_CLOCK = Clock.fixed(Instant.parse("2026-02-24T10:00:00Z"), ZoneOffset.UTC);

    @Mock private TimeSlotRepository timeSlotRepository;
    @Mock private CalendarRepository calendarRepository;
    @Mock private MeterRegistry meterRegistry;
    @Mock private Counter counter;

    @InjectMocks
    private TimeSlotService timeSlotService;

    private final UUID calendarId = UUID.randomUUID();
    private final LocalDateTime start = LocalDateTime.now(FIXED_CLOCK).plusDays(1);
    private final LocalDateTime end = start.plusHours(1);

    @BeforeEach
    void setUp() {
        lenient().when(meterRegistry.counter(anyString())).thenReturn(counter);
        lenient().doNothing().when(counter).increment();
    }

    @Test
    void shouldCreateSlotSuccessfully() {
        final var saved = TimeSlot.builder()
                .id(UUID.randomUUID()).calendarId(calendarId)
                .startTime(start).endTime(end).status(SlotStatus.FREE).build();

        when(calendarRepository.existsById(calendarId)).thenReturn(Mono.just(true));
        when(timeSlotRepository.countOverlapping(calendarId, start, end)).thenReturn(Mono.just(0L));
        when(timeSlotRepository.save(any())).thenReturn(Mono.just(saved));

        StepVerifier.create(timeSlotService.create(calendarId, new CreateSlotRequest(start, end)))
                .expectNextMatches(s -> s.status() == SlotStatus.FREE && s.calendarId().equals(calendarId))
                .verifyComplete();
    }

    @Test
    void shouldFailWhenCalendarNotFound() {
        when(calendarRepository.existsById(calendarId)).thenReturn(Mono.just(false));

        StepVerifier.create(timeSlotService.create(calendarId, new CreateSlotRequest(start, end)))
                .expectError(CalendarNotFoundException.class)
                .verify();
    }

    @Test
    void shouldFailWhenSlotOverlaps() {
        when(calendarRepository.existsById(calendarId)).thenReturn(Mono.just(true));
        when(timeSlotRepository.countOverlapping(calendarId, start, end)).thenReturn(Mono.just(1L));

        StepVerifier.create(timeSlotService.create(calendarId, new CreateSlotRequest(start, end)))
                .expectError(SlotOverlapException.class)
                .verify();
    }

    @Test
    void shouldFailWhenEndTimeBeforeStartTime() {
        StepVerifier.create(timeSlotService.create(calendarId, new CreateSlotRequest(end, start)))
                .expectError(IllegalArgumentException.class)
                .verify();
    }

    @Test
    void shouldDeleteFreeSlot() {
        final var slotId = UUID.randomUUID();
        final var slot = TimeSlot.builder()
                .id(slotId).calendarId(calendarId)
                .startTime(start).endTime(end).status(SlotStatus.FREE).build();

        when(timeSlotRepository.findById(slotId)).thenReturn(Mono.just(slot));
        when(timeSlotRepository.deleteById(slotId)).thenReturn(Mono.empty());

        StepVerifier.create(timeSlotService.delete(slotId))
                .verifyComplete();
    }

    @Test
    void shouldFailDeleteWhenSlotLinkedToMeeting() {
        final var slotId = UUID.randomUUID();
        final var slot = TimeSlot.builder()
                .id(slotId).calendarId(calendarId)
                .startTime(start).endTime(end).status(SlotStatus.BUSY)
                .meetingId(UUID.randomUUID()).build();

        when(timeSlotRepository.findById(slotId)).thenReturn(Mono.just(slot));

        StepVerifier.create(timeSlotService.delete(slotId))
                .expectError(SlotLinkedToMeetingException.class)
                .verify();
    }
}

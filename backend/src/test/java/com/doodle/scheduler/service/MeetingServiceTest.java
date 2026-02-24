package com.doodle.scheduler.service;

import com.doodle.scheduler.domain.Meeting;
import com.doodle.scheduler.domain.MeetingParticipant;
import com.doodle.scheduler.domain.SlotStatus;
import com.doodle.scheduler.domain.TimeSlot;
import com.doodle.scheduler.dto.CreateMeetingRequest;
import com.doodle.scheduler.exception.SlotAlreadyBusyException;
import com.doodle.scheduler.exception.SlotNotFoundException;
import com.doodle.scheduler.repository.MeetingParticipantRepository;
import com.doodle.scheduler.repository.MeetingRepository;
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
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MeetingServiceTest {

    private static final Clock FIXED_CLOCK = Clock.fixed(Instant.parse("2026-02-24T10:00:00Z"), ZoneOffset.UTC);

    @Mock private MeetingRepository meetingRepository;
    @Mock private MeetingParticipantRepository participantRepository;
    @Mock private TimeSlotRepository timeSlotRepository;
    @Mock private MeterRegistry meterRegistry;
    @Mock private Counter counter;

    @InjectMocks
    private MeetingService meetingService;

    private final UUID slotId = UUID.randomUUID();
    private final UUID organizerId = UUID.randomUUID();
    private final LocalDateTime start = LocalDateTime.now(FIXED_CLOCK).plusDays(1);
    private final LocalDateTime end = start.plusHours(1);

    @BeforeEach
    void setUp() {
        lenient().when(meterRegistry.counter(anyString())).thenReturn(counter);
        lenient().doNothing().when(counter).increment();
    }

    @Test
    void shouldScheduleMeetingSuccessfully() {
        final var meetingId = UUID.randomUUID();
        final var freeSlot = TimeSlot.builder()
                .id(slotId).calendarId(UUID.randomUUID())
                .startTime(start).endTime(end).status(SlotStatus.FREE).build();
        final var savedMeeting = Meeting.builder()
                .id(meetingId).title("Team Sync").organizerId(organizerId).slotId(slotId).build();
        final var busySlot = freeSlot.withStatus(SlotStatus.BUSY).withMeetingId(meetingId);

        when(timeSlotRepository.findById(slotId)).thenReturn(Mono.just(freeSlot));
        when(meetingRepository.save(any())).thenReturn(Mono.just(savedMeeting));
        when(timeSlotRepository.save(any())).thenReturn(Mono.just(busySlot));

        StepVerifier.create(meetingService.schedule(
                        new CreateMeetingRequest(slotId, organizerId, "Team Sync", null, List.of())))
                .expectNextMatches(r -> r.title().equals("Team Sync") && r.slotId().equals(slotId))
                .verifyComplete();
    }

    @Test
    void shouldScheduleMeetingWithParticipants() {
        final var meetingId = UUID.randomUUID();
        final var participantId = UUID.randomUUID();
        final var freeSlot = TimeSlot.builder()
                .id(slotId).calendarId(UUID.randomUUID())
                .startTime(start).endTime(end).status(SlotStatus.FREE).build();
        final var savedMeeting = Meeting.builder()
                .id(meetingId).title("Sync").organizerId(organizerId).slotId(slotId).build();
        final var participant = MeetingParticipant.builder()
                .id(UUID.randomUUID()).meetingId(meetingId).userId(participantId).build();

        when(timeSlotRepository.findById(slotId)).thenReturn(Mono.just(freeSlot));
        when(meetingRepository.save(any())).thenReturn(Mono.just(savedMeeting));
        when(timeSlotRepository.save(any())).thenReturn(Mono.just(freeSlot.withStatus(SlotStatus.BUSY).withMeetingId(meetingId)));
        when(participantRepository.save(any())).thenReturn(Mono.just(participant));

        StepVerifier.create(meetingService.schedule(
                        new CreateMeetingRequest(slotId, organizerId, "Sync", null, List.of(participantId))))
                .expectNextMatches(r -> r.participantIds().contains(participantId))
                .verifyComplete();
    }

    @Test
    void shouldFailWhenSlotNotFound() {
        when(timeSlotRepository.findById(slotId)).thenReturn(Mono.empty());

        StepVerifier.create(meetingService.schedule(
                        new CreateMeetingRequest(slotId, organizerId, "Sync", null, List.of())))
                .expectError(SlotNotFoundException.class)
                .verify();
    }

    @Test
    void shouldFailWhenSlotAlreadyBusy() {
        final var busySlot = TimeSlot.builder()
                .id(slotId).calendarId(UUID.randomUUID())
                .startTime(start).endTime(end).status(SlotStatus.BUSY)
                .meetingId(UUID.randomUUID()).build();

        when(timeSlotRepository.findById(slotId)).thenReturn(Mono.just(busySlot));

        StepVerifier.create(meetingService.schedule(
                        new CreateMeetingRequest(slotId, organizerId, "Sync", null, List.of())))
                .expectError(SlotAlreadyBusyException.class)
                .verify();
    }
}

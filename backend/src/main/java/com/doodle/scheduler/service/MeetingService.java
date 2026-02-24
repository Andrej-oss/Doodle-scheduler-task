package com.doodle.scheduler.service;

import com.doodle.scheduler.domain.Meeting;
import com.doodle.scheduler.domain.MeetingParticipant;
import com.doodle.scheduler.domain.SlotStatus;
import com.doodle.scheduler.dto.CreateMeetingRequest;
import com.doodle.scheduler.dto.MeetingResponse;
import com.doodle.scheduler.exception.MeetingNotFoundException;
import com.doodle.scheduler.exception.SlotAlreadyBusyException;
import com.doodle.scheduler.exception.SlotNotFoundException;
import com.doodle.scheduler.repository.MeetingParticipantRepository;
import com.doodle.scheduler.repository.MeetingRepository;
import com.doodle.scheduler.repository.TimeSlotRepository;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class MeetingService {

    private static final String ERR_SLOT_NOT_FOUND = "Slot not found: ";
    private static final String ERR_SLOT_ALREADY_BUSY = "Slot is already busy: ";
    private static final String ERR_MEETING_NOT_FOUND = "Meeting not found: ";
    private static final String METRIC_MEETINGS_SCHEDULED = "meetings_scheduled_total";

    private final MeetingRepository meetingRepository;
    private final MeetingParticipantRepository participantRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final MeterRegistry meterRegistry;

    @Transactional
    public Mono<MeetingResponse> schedule(@NonNull final CreateMeetingRequest request) {
        log.info("Scheduling meeting: title='{}', slotId={}, organizerId={}", request.title(), request.slotId(), request.organizerId());
        return timeSlotRepository.findById(request.slotId())
                .switchIfEmpty(Mono.error(new SlotNotFoundException(ERR_SLOT_NOT_FOUND + request.slotId())))
                .flatMap(slot -> {
                    if (slot.status() == SlotStatus.BUSY) {
                        log.warn("Slot already busy: slotId={}", request.slotId());
                        return Mono.error(new SlotAlreadyBusyException(ERR_SLOT_ALREADY_BUSY + request.slotId()));
                    }
                    final var meeting = Meeting.builder()
                            .title(request.title())
                            .description(request.description())
                            .organizerId(request.organizerId())
                            .slotId(request.slotId())
                            .build();
                    return meetingRepository.save(meeting)
                            .flatMap(saved -> {
                                final var updatedSlot = slot
                                        .withStatus(SlotStatus.BUSY)
                                        .withMeetingId(saved.id());
                                return timeSlotRepository.save(updatedSlot).thenReturn(saved);
                            })
                            .flatMap(saved -> saveParticipants(saved, request.participantIds())
                                    .collectList()
                                    .map(participants -> toResponse(saved, slot.startTime(), slot.endTime(),
                                            participants.stream().map(MeetingParticipant::userId).toList())));
                })
                .doOnSuccess(m -> {
                    log.info("Meeting scheduled: id={}, title='{}', participants={}", m.id(), m.title(), m.participantIds().size());
                    meterRegistry.counter(METRIC_MEETINGS_SCHEDULED).increment();
                });
    }

    public Mono<MeetingResponse> findById(@NonNull final UUID meetingId) {
        log.debug("Finding meeting by id={}", meetingId);
        return meetingRepository.findById(meetingId)
                .switchIfEmpty(Mono.error(new MeetingNotFoundException(ERR_MEETING_NOT_FOUND + meetingId)))
                .flatMap(meeting -> timeSlotRepository.findById(meeting.slotId())
                        .flatMap(slot -> participantRepository.findAllByMeetingId(meetingId)
                                .map(MeetingParticipant::userId)
                                .collectList()
                                .map(ids -> toResponse(meeting, slot.startTime(), slot.endTime(), ids))));
    }

    public Flux<MeetingResponse> findByUser(@NonNull final UUID userId) {
        log.debug("Finding meetings for userId={}", userId);
        return meetingRepository.findAllByOrganizerId(userId)
                .flatMap(meeting -> timeSlotRepository.findById(meeting.slotId())
                        .flatMap(slot -> participantRepository.findAllByMeetingId(meeting.id())
                                .map(MeetingParticipant::userId)
                                .collectList()
                                .map(ids -> toResponse(meeting, slot.startTime(), slot.endTime(), ids))));
    }

    private Flux<MeetingParticipant> saveParticipants(@NonNull final Meeting meeting,
                                                      final List<UUID> participantIds) {
        if (participantIds == null || participantIds.isEmpty()) {
            return Flux.empty();
        }
        return Flux.fromIterable(participantIds)
                .map(userId -> MeetingParticipant.builder()
                        .meetingId(meeting.id())
                        .userId(userId)
                        .build())
                .flatMap(participantRepository::save);
    }

    private MeetingResponse toResponse(@NonNull final Meeting meeting,
                                       @NonNull final LocalDateTime startTime,
                                       @NonNull final LocalDateTime endTime,
                                       @NonNull final List<UUID> participantIds) {
        return new MeetingResponse(
                meeting.id(),
                meeting.title(),
                meeting.description(),
                meeting.organizerId(),
                meeting.slotId(),
                startTime,
                endTime,
                participantIds,
                meeting.createdAt()
        );
    }
}

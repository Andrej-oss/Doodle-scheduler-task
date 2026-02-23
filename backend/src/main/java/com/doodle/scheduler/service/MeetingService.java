package com.doodle.scheduler.service;

import com.doodle.scheduler.domain.Meeting;
import com.doodle.scheduler.domain.MeetingParticipant;
import com.doodle.scheduler.domain.SlotStatus;
import com.doodle.scheduler.dto.CreateMeetingRequest;
import com.doodle.scheduler.dto.MeetingResponse;
import com.doodle.scheduler.exception.ConflictException;
import com.doodle.scheduler.exception.NotFoundException;
import com.doodle.scheduler.repository.MeetingParticipantRepository;
import com.doodle.scheduler.repository.MeetingRepository;
import com.doodle.scheduler.repository.TimeSlotRepository;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MeetingService {

    private final MeetingRepository meetingRepository;
    private final MeetingParticipantRepository participantRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final MeterRegistry meterRegistry;

    @Transactional
    public Mono<MeetingResponse> schedule(@NonNull final CreateMeetingRequest request) {
        return timeSlotRepository.findById(request.slotId())
                .switchIfEmpty(Mono.error(new NotFoundException("Slot not found: " + request.slotId())))
                .flatMap(slot -> {
                    if (slot.status() == SlotStatus.BUSY) {
                        return Mono.error(new ConflictException("Slot is already busy: " + request.slotId()));
                    }
                    final Meeting meeting = Meeting.builder()
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
                                    .map(participants -> toResponse(saved, slot.startTime(), slot.endTime(), participants)));
                })
                .doOnSuccess(m -> meterRegistry.counter("meetings_scheduled_total").increment());
    }

    public Mono<MeetingResponse> findById(@NonNull final UUID meetingId) {
        return meetingRepository.findById(meetingId)
                .switchIfEmpty(Mono.error(new NotFoundException("Meeting not found: " + meetingId)))
                .flatMap(meeting -> timeSlotRepository.findById(meeting.slotId())
                        .flatMap(slot -> participantRepository.findAllByMeetingId(meetingId)
                                .map(MeetingParticipant::userId)
                                .collectList()
                                .map(ids -> toResponse(meeting, slot.startTime(), slot.endTime(), ids))));
    }

    public Flux<MeetingResponse> findByUser(@NonNull final UUID userId) {
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
                                       @NonNull final java.time.LocalDateTime startTime,
                                       @NonNull final java.time.LocalDateTime endTime,
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

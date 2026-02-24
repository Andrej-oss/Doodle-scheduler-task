package com.doodle.scheduler.service;

import com.doodle.scheduler.domain.SlotStatus;
import com.doodle.scheduler.domain.TimeSlot;
import com.doodle.scheduler.dto.AvailabilityResponse;
import com.doodle.scheduler.dto.CreateSlotRequest;
import com.doodle.scheduler.dto.UpdateSlotRequest;
import com.doodle.scheduler.exception.CalendarNotFoundException;
import com.doodle.scheduler.exception.SlotLinkedToMeetingException;
import com.doodle.scheduler.exception.SlotNotFoundException;
import com.doodle.scheduler.exception.SlotOverlapException;
import com.doodle.scheduler.repository.CalendarRepository;
import com.doodle.scheduler.repository.TimeSlotRepository;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TimeSlotService {

    private static final String ERR_CALENDAR_NOT_FOUND = "Calendar not found: ";
    private static final String ERR_SLOT_NOT_FOUND = "Slot not found: ";
    private static final String ERR_END_BEFORE_START = "endTime must be after startTime";
    private static final String ERR_SLOT_OVERLAPS = "Slot overlaps with an existing slot in this calendar";
    private static final String ERR_CANNOT_FREE_MEETING_SLOT = "Cannot free a slot that is linked to a meeting";
    private static final String ERR_CANNOT_DELETE_MEETING_SLOT = "Cannot delete a slot linked to a meeting";
    private static final String METRIC_SLOTS_CREATED = "slots_created_total";

    private final TimeSlotRepository timeSlotRepository;
    private final CalendarRepository calendarRepository;
    private final MeterRegistry meterRegistry;

    public Mono<TimeSlot> create(@NonNull final UUID calendarId,
                                 @NonNull final CreateSlotRequest request) {
        log.info("Creating slot: calendarId={}, start={}, end={}", calendarId, request.startTime(), request.endTime());
        if (!request.endTime().isAfter(request.startTime())) {
            return Mono.error(new IllegalArgumentException(ERR_END_BEFORE_START));
        }
        return calendarRepository.existsById(calendarId)
                .flatMap(exists -> {
                    if (!exists) {
                        log.warn("Calendar not found: {}", calendarId);
                        return Mono.error(new CalendarNotFoundException(ERR_CALENDAR_NOT_FOUND + calendarId));
                    }
                    return timeSlotRepository.countOverlapping(calendarId, request.startTime(), request.endTime());
                })
                .flatMap(count -> {
                    if (count > 0) {
                        log.warn("Slot overlap detected: calendarId={}, start={}, end={}", calendarId, request.startTime(), request.endTime());
                        return Mono.error(new SlotOverlapException(ERR_SLOT_OVERLAPS));
                    }
                    final var slot = TimeSlot.builder()
                            .calendarId(calendarId)
                            .startTime(request.startTime())
                            .endTime(request.endTime())
                            .status(SlotStatus.FREE)
                            .build();
                    return timeSlotRepository.save(slot);
                })
                .doOnSuccess(s -> {
                    log.info("Slot created: id={}, calendarId={}", s.id(), s.calendarId());
                    meterRegistry.counter(METRIC_SLOTS_CREATED).increment();
                });
    }

    public Mono<TimeSlot> update(@NonNull final UUID slotId,
                                 @NonNull final UpdateSlotRequest request) {
        log.info("Updating slot: id={}", slotId);
        return timeSlotRepository.findById(slotId)
                .switchIfEmpty(Mono.error(new SlotNotFoundException(ERR_SLOT_NOT_FOUND + slotId)))
                .flatMap(existing -> {
                    if (existing.meetingId() != null && request.status() == SlotStatus.FREE) {
                        log.warn("Attempt to free a meeting-linked slot: id={}, meetingId={}", slotId, existing.meetingId());
                        return Mono.error(new SlotLinkedToMeetingException(ERR_CANNOT_FREE_MEETING_SLOT));
                    }
                    final var updated = existing
                            .withStartTime(request.startTime() != null ? request.startTime() : existing.startTime())
                            .withEndTime(request.endTime() != null ? request.endTime() : existing.endTime())
                            .withStatus(request.status() != null ? request.status() : existing.status());
                    return timeSlotRepository.save(updated);
                })
                .doOnSuccess(s -> log.info("Slot updated: id={}, status={}", s.id(), s.status()));
    }

    public Mono<Void> delete(@NonNull final UUID slotId) {
        log.info("Deleting slot: id={}", slotId);
        return timeSlotRepository.findById(slotId)
                .switchIfEmpty(Mono.error(new SlotNotFoundException(ERR_SLOT_NOT_FOUND + slotId)))
                .flatMap(slot -> {
                    if (slot.meetingId() != null) {
                        log.warn("Attempt to delete a meeting-linked slot: id={}, meetingId={}", slotId, slot.meetingId());
                        return Mono.error(new SlotLinkedToMeetingException(ERR_CANNOT_DELETE_MEETING_SLOT));
                    }
                    return timeSlotRepository.deleteById(slotId);
                })
                .doOnSuccess(v -> log.info("Slot deleted: id={}", slotId));
    }

    public Flux<TimeSlot> findByCalendar(@NonNull final UUID calendarId,
                                         final SlotStatus status,
                                         final LocalDateTime from,
                                         final LocalDateTime to) {
        log.debug("Finding slots: calendarId={}, status={}, from={}, to={}", calendarId, status, from, to);
        final var statusStr = status != null ? status.name() : null;
        return timeSlotRepository.findByCalendarIdWithFilters(calendarId, statusStr, from, to);
    }

    public Flux<AvailabilityResponse> getAvailability(@NonNull final UUID userId,
                                                      @NonNull final LocalDateTime from,
                                                      @NonNull final LocalDateTime to) {
        log.debug("Getting availability: userId={}, from={}, to={}", userId, from, to);
        return timeSlotRepository.findByUserIdAndTimeRange(userId, from, to)
                .map(slot -> new AvailabilityResponse(slot.id(), slot.startTime(), slot.endTime(), slot.status()));
    }
}

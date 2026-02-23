package com.doodle.scheduler.service;

import com.doodle.scheduler.domain.SlotStatus;
import com.doodle.scheduler.domain.TimeSlot;
import com.doodle.scheduler.dto.AvailabilityResponse;
import com.doodle.scheduler.dto.CreateSlotRequest;
import com.doodle.scheduler.dto.UpdateSlotRequest;
import com.doodle.scheduler.exception.ConflictException;
import com.doodle.scheduler.exception.NotFoundException;
import com.doodle.scheduler.repository.CalendarRepository;
import com.doodle.scheduler.repository.TimeSlotRepository;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TimeSlotService {

    private final TimeSlotRepository timeSlotRepository;
    private final CalendarRepository calendarRepository;
    private final MeterRegistry meterRegistry;

    public Mono<TimeSlot> create(@NonNull final UUID calendarId,
                                 @NonNull final CreateSlotRequest request) {
        if (!request.endTime().isAfter(request.startTime())) {
            return Mono.error(new IllegalArgumentException("endTime must be after startTime"));
        }
        return calendarRepository.existsById(calendarId)
                .flatMap(exists -> {
                    if (!exists) {
                        return Mono.error(new NotFoundException("Calendar not found: " + calendarId));
                    }
                    final TimeSlot slot = TimeSlot.builder()
                            .calendarId(calendarId)
                            .startTime(request.startTime())
                            .endTime(request.endTime())
                            .status(SlotStatus.FREE)
                            .build();
                    return timeSlotRepository.save(slot);
                })
                .doOnSuccess(s -> meterRegistry.counter("slots_created_total").increment());
    }

    public Mono<TimeSlot> update(@NonNull final UUID slotId,
                                 @NonNull final UpdateSlotRequest request) {
        return timeSlotRepository.findById(slotId)
                .switchIfEmpty(Mono.error(new NotFoundException("Slot not found: " + slotId)))
                .flatMap(existing -> {
                    if (existing.meetingId() != null && request.status() == SlotStatus.FREE) {
                        return Mono.error(new ConflictException("Cannot free a slot that is linked to a meeting"));
                    }
                    final TimeSlot updated = existing
                            .withStartTime(request.startTime() != null ? request.startTime() : existing.startTime())
                            .withEndTime(request.endTime() != null ? request.endTime() : existing.endTime())
                            .withStatus(request.status() != null ? request.status() : existing.status());
                    return timeSlotRepository.save(updated);
                });
    }

    public Mono<Void> delete(@NonNull final UUID slotId) {
        return timeSlotRepository.findById(slotId)
                .switchIfEmpty(Mono.error(new NotFoundException("Slot not found: " + slotId)))
                .flatMap(slot -> {
                    if (slot.meetingId() != null) {
                        return Mono.error(new ConflictException("Cannot delete a slot linked to a meeting"));
                    }
                    return timeSlotRepository.deleteById(slotId);
                });
    }

    public Flux<TimeSlot> findByCalendar(@NonNull final UUID calendarId,
                                         final SlotStatus status,
                                         final LocalDateTime from,
                                         final LocalDateTime to) {
        final String statusStr = status != null ? status.name() : null;
        return timeSlotRepository.findByCalendarIdWithFilters(calendarId, statusStr, from, to);
    }

    public Flux<AvailabilityResponse> getAvailability(@NonNull final UUID userId,
                                                      @NonNull final LocalDateTime from,
                                                      @NonNull final LocalDateTime to) {
        return timeSlotRepository.findByUserIdAndTimeRange(userId, from, to)
                .map(slot -> new AvailabilityResponse(slot.id(), slot.startTime(), slot.endTime(), slot.status()));
    }
}

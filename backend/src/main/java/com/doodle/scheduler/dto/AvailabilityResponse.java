package com.doodle.scheduler.dto;

import com.doodle.scheduler.domain.SlotStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record AvailabilityResponse(
        UUID slotId,
        LocalDateTime startTime,
        LocalDateTime endTime,
        SlotStatus status
) {}

package com.doodle.scheduler.dto;

import com.doodle.scheduler.domain.SlotStatus;

import java.time.LocalDateTime;

public record UpdateSlotRequest(
        LocalDateTime startTime,
        LocalDateTime endTime,
        SlotStatus status
) {}

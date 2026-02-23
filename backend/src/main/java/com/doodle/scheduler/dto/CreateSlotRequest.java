package com.doodle.scheduler.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record CreateSlotRequest(
        @NotNull LocalDateTime startTime,
        @NotNull LocalDateTime endTime
) {}

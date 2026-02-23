package com.doodle.scheduler.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateCalendarRequest(
        @NotNull UUID userId,
        @NotBlank String name
) {}

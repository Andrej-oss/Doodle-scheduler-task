package com.doodle.scheduler.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record CreateMeetingRequest(
        @NotNull UUID slotId,
        @NotNull UUID organizerId,
        @NotBlank String title,
        String description,
        List<UUID> participantIds
) {}

package com.doodle.scheduler.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record MeetingResponse(
        UUID id,
        String title,
        String description,
        UUID organizerId,
        UUID slotId,
        LocalDateTime startTime,
        LocalDateTime endTime,
        List<UUID> participantIds,
        LocalDateTime createdAt
) {}

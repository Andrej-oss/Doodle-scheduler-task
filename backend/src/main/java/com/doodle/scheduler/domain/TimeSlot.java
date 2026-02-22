package com.doodle.scheduler.domain;

import lombok.Builder;
import lombok.NonNull;
import lombok.With;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Table("time_slots")
@Builder
@With
public record TimeSlot(
        @Id UUID id,
        @NonNull UUID calendarId,
        @NonNull LocalDateTime startTime,
        @NonNull LocalDateTime endTime,
        @NonNull SlotStatus status,
        UUID meetingId,
        @CreatedDate LocalDateTime createdAt
) {}

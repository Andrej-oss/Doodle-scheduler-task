package com.doodle.scheduler.domain;

import lombok.Builder;
import lombok.NonNull;
import lombok.With;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Table("meetings")
@Builder
@With
public record Meeting(
        @Id UUID id,
        @NonNull String title,
        String description,
        @NonNull UUID organizerId,
        @NonNull UUID slotId,
        @CreatedDate LocalDateTime createdAt
) {}

package com.doodle.scheduler.domain;

import lombok.Builder;
import lombok.NonNull;
import lombok.With;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Table("calendars")
@Builder
@With
public record Calendar(
        @Id UUID id,
        @NonNull UUID userId,
        @NonNull String name,
        @CreatedDate LocalDateTime createdAt
) {}

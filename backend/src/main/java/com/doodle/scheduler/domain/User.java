package com.doodle.scheduler.domain;

import lombok.Builder;
import lombok.NonNull;
import lombok.With;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Table("users")
@Builder
@With
public record User(
        @Id UUID id,
        @NonNull String username,
        @NonNull String email,
        @CreatedDate LocalDateTime createdAt
) {}

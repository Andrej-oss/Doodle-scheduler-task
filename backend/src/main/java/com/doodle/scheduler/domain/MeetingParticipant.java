package com.doodle.scheduler.domain;

import lombok.Builder;
import lombok.NonNull;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Table("meeting_participants")
@Builder
public record MeetingParticipant(
        @Id UUID id,
        @NonNull UUID meetingId,
        @NonNull UUID userId
) {}

package com.doodle.minidoodle.domain.model;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record Meeting(
        UUID id,
        UUID timeSlotId,
        Instant slotStartTime,
        Instant slotEndTime,
        String title,
        String description,
        Set<UUID> participantIds,
        Instant createdAt,
        Instant updatedAt
) {}

package com.doodle.minidoodle.domain.command;

import java.util.Set;
import java.util.UUID;

public record ScheduleMeetingCommand(
        UUID userId,
        UUID slotId,
        String title,
        String description,
        Set<UUID> participantIds
) {}

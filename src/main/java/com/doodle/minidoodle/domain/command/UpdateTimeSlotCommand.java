package com.doodle.minidoodle.domain.command;

import java.time.Instant;
import java.util.UUID;

public record UpdateTimeSlotCommand(UUID userId, UUID slotId, Instant startTime, Instant endTime) {}

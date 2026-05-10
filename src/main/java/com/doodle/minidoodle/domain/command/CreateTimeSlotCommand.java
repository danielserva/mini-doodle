package com.doodle.minidoodle.domain.command;

import java.time.Instant;
import java.util.UUID;

public record CreateTimeSlotCommand(UUID userId, Instant startTime, Instant endTime) {}

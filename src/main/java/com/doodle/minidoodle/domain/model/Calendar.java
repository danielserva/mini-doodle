package com.doodle.minidoodle.domain.model;

import java.time.Instant;
import java.util.UUID;

public record Calendar(
        UUID id,
        UUID userId,
        Instant createdAt
) {}

package com.doodle.minidoodle.domain.model;

import java.time.Instant;
import java.util.UUID;

public record User(
        UUID id,
        String email,
        String name,
        Instant createdAt
) {}

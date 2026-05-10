package com.doodle.minidoodle.adapter.in.rest.request;

import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record UpdateTimeSlotRequest(
        @NotNull Instant startTime,
        @NotNull Instant endTime
) {}

package com.doodle.minidoodle.adapter.in.rest.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Set;
import java.util.UUID;

public record ScheduleMeetingRequest(
        @NotBlank @Size(max = 255) String title,
        String description,
        @NotNull Set<UUID> participantIds
) {}

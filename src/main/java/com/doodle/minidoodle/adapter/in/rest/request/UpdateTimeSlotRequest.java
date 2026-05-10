package com.doodle.minidoodle.adapter.in.rest.request;

import com.doodle.minidoodle.adapter.in.rest.deserializer.MinutePrecisionInstantDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public record UpdateTimeSlotRequest(
        @NotNull @JsonDeserialize(using = MinutePrecisionInstantDeserializer.class) Instant startTime,
        @NotNull @JsonDeserialize(using = MinutePrecisionInstantDeserializer.class) Instant endTime
) {}

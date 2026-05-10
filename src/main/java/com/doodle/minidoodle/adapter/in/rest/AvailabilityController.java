package com.doodle.minidoodle.adapter.in.rest;

import com.doodle.minidoodle.adapter.in.rest.response.AvailabilityResponse;
import com.doodle.minidoodle.domain.model.TimeSlot;
import com.doodle.minidoodle.domain.port.in.TimeSlotUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users/{userId}/availability")
@RequiredArgsConstructor
@Tag(name = "Availability", description = "Aggregated availability view")
public class AvailabilityController {

    private final TimeSlotUseCase timeSlotUseCase;

    @GetMapping
    @Operation(summary = "Get aggregated availability for a user in a time range. " +
            "If omitted, defaults to now through 7 days from now. Both or neither must be provided.")
    public AvailabilityResponse getAvailability(
            @PathVariable UUID userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to) {
        if ((from == null) != (to == null)) {
            throw new IllegalArgumentException("Both 'from' and 'to' must be provided together, or neither.");
        }
        if (from == null) {
            from = Instant.now();
            to = from.plus(7, ChronoUnit.DAYS);
        }
        List<TimeSlot> slots = timeSlotUseCase.queryAvailability(userId, from, to);
        return AvailabilityResponse.of(userId, from, to, slots);
    }
}

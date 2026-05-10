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
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users/{userId}/availability")
@RequiredArgsConstructor
@Tag(name = "Availability", description = "Aggregated availability view")
public class AvailabilityController {

    private final TimeSlotUseCase timeSlotUseCase;

    @GetMapping
    @Operation(summary = "Get aggregated availability for a user in a time range")
    public AvailabilityResponse getAvailability(
            @PathVariable UUID userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to) {
        List<TimeSlot> slots = timeSlotUseCase.queryAvailability(userId, from, to);
        return AvailabilityResponse.of(userId, from, to, slots);
    }
}

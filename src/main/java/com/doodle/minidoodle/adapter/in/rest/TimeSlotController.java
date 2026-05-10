package com.doodle.minidoodle.adapter.in.rest;

import com.doodle.minidoodle.adapter.in.rest.request.CreateTimeSlotRequest;
import com.doodle.minidoodle.adapter.in.rest.request.UpdateSlotStatusRequest;
import com.doodle.minidoodle.adapter.in.rest.request.UpdateTimeSlotRequest;
import com.doodle.minidoodle.adapter.in.rest.response.PagedResponse;
import com.doodle.minidoodle.adapter.in.rest.response.TimeSlotResponse;
import com.doodle.minidoodle.domain.command.CreateTimeSlotCommand;
import com.doodle.minidoodle.domain.command.UpdateTimeSlotCommand;
import com.doodle.minidoodle.domain.model.SlotStatus;
import com.doodle.minidoodle.domain.port.in.TimeSlotUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users/{userId}/slots")
@RequiredArgsConstructor
@Tag(name = "Time Slots", description = "Time slot management")
public class TimeSlotController {

    private final TimeSlotUseCase timeSlotUseCase;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new time slot")
    public TimeSlotResponse createTimeSlot(
            @PathVariable UUID userId,
            @Valid @RequestBody CreateTimeSlotRequest request) {
        return TimeSlotResponse.from(timeSlotUseCase.createTimeSlot(
                new CreateTimeSlotCommand(userId, request.startTime(), request.endTime())));
    }

    @GetMapping
    @Operation(summary = "List time slots with optional filters")
    public PagedResponse<TimeSlotResponse> listTimeSlots(
            @PathVariable UUID userId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(required = false) SlotStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("startTime").ascending());
        return PagedResponse.from(
                timeSlotUseCase.listTimeSlots(userId, from, to, status, pageable),
                TimeSlotResponse::from);
    }

    @GetMapping("/{slotId}")
    @Operation(summary = "Get a time slot by ID")
    public TimeSlotResponse getTimeSlot(@PathVariable UUID userId, @PathVariable UUID slotId) {
        return TimeSlotResponse.from(timeSlotUseCase.getTimeSlot(userId, slotId));
    }

    @PutMapping("/{slotId}")
    @Operation(summary = "Update a time slot")
    public TimeSlotResponse updateTimeSlot(
            @PathVariable UUID userId,
            @PathVariable UUID slotId,
            @Valid @RequestBody UpdateTimeSlotRequest request) {
        return TimeSlotResponse.from(timeSlotUseCase.updateTimeSlot(
                new UpdateTimeSlotCommand(userId, slotId, request.startTime(), request.endTime())));
    }

    @DeleteMapping("/{slotId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a time slot")
    public void deleteTimeSlot(@PathVariable UUID userId, @PathVariable UUID slotId) {
        timeSlotUseCase.deleteTimeSlot(userId, slotId);
    }

    @PatchMapping("/{slotId}/status")
    @Operation(summary = "Update the status of a time slot (FREE or BUSY)")
    public TimeSlotResponse updateSlotStatus(
            @PathVariable UUID userId,
            @PathVariable UUID slotId,
            @Valid @RequestBody UpdateSlotStatusRequest request) {
        return TimeSlotResponse.from(timeSlotUseCase.updateSlotStatus(userId, slotId, request.status()));
    }
}

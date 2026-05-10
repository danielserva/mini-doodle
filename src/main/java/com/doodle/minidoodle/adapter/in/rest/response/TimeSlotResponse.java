package com.doodle.minidoodle.adapter.in.rest.response;

import com.doodle.minidoodle.domain.model.SlotStatus;
import com.doodle.minidoodle.domain.model.TimeSlot;

import java.time.Instant;
import java.util.UUID;

public record TimeSlotResponse(
        UUID id,
        Instant startTime,
        Instant endTime,
        SlotStatus status,
        UUID meetingId,
        Instant createdAt,
        Instant updatedAt
) {
    public static TimeSlotResponse from(TimeSlot slot) {
        return new TimeSlotResponse(
                slot.id(),
                slot.startTime(),
                slot.endTime(),
                slot.status(),
                slot.meetingId(),
                slot.createdAt(),
                slot.updatedAt()
        );
    }
}

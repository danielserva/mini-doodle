package com.doodle.minidoodle.domain.model;

import java.time.Instant;
import java.util.UUID;

public record TimeSlot(
        UUID id,
        UUID calendarId,
        Instant startTime,
        Instant endTime,
        SlotStatus status,
        UUID meetingId,
        Instant createdAt,
        Instant updatedAt
) {
    public boolean isFree() {
        return status == SlotStatus.FREE;
    }

    public boolean isBusy() {
        return status == SlotStatus.BUSY;
    }

    public TimeSlot withStatus(SlotStatus newStatus) {
        return new TimeSlot(id, calendarId, startTime, endTime, newStatus, meetingId, createdAt, Instant.now());
    }

    public TimeSlot withMeeting(UUID newMeetingId) {
        return new TimeSlot(id, calendarId, startTime, endTime, SlotStatus.BUSY, newMeetingId, createdAt, Instant.now());
    }
}

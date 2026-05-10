package com.doodle.minidoodle.adapter.in.rest.response;

import com.doodle.minidoodle.domain.model.Meeting;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record MeetingResponse(
        UUID id,
        UUID timeSlotId,
        Instant startTime,
        Instant endTime,
        String title,
        String description,
        Set<UUID> participantIds,
        Instant createdAt,
        Instant updatedAt
) {
    public static MeetingResponse from(Meeting meeting) {
        return new MeetingResponse(
                meeting.id(),
                meeting.timeSlotId(),
                meeting.slotStartTime(),
                meeting.slotEndTime(),
                meeting.title(),
                meeting.description(),
                meeting.participantIds(),
                meeting.createdAt(),
                meeting.updatedAt()
        );
    }
}

package com.doodle.minidoodle.domain.exception;

import java.util.UUID;

public class MeetingNotFoundException extends RuntimeException {
    public MeetingNotFoundException(UUID meetingId) {
        super("Meeting not found: " + meetingId);
    }
}

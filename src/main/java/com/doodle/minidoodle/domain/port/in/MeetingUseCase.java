package com.doodle.minidoodle.domain.port.in;

import com.doodle.minidoodle.domain.command.ScheduleMeetingCommand;
import com.doodle.minidoodle.domain.model.Meeting;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface MeetingUseCase {
    Meeting scheduleMeeting(ScheduleMeetingCommand command);
    Meeting getMeeting(UUID userId, UUID meetingId);
    Page<Meeting> listMeetings(UUID userId, Pageable pageable);
}

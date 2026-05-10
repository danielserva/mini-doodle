package com.doodle.minidoodle.domain.service;

import com.doodle.minidoodle.domain.command.ScheduleMeetingCommand;
import com.doodle.minidoodle.domain.exception.MeetingNotFoundException;
import com.doodle.minidoodle.domain.exception.SlotAlreadyBookedException;
import com.doodle.minidoodle.domain.exception.TimeSlotNotFoundException;
import com.doodle.minidoodle.domain.exception.UserNotFoundException;
import com.doodle.minidoodle.domain.model.Calendar;
import com.doodle.minidoodle.domain.model.Meeting;
import com.doodle.minidoodle.domain.model.TimeSlot;
import com.doodle.minidoodle.domain.port.in.MeetingUseCase;
import com.doodle.minidoodle.domain.port.out.CalendarRepositoryPort;
import com.doodle.minidoodle.domain.port.out.MeetingRepositoryPort;
import com.doodle.minidoodle.domain.port.out.TimeSlotRepositoryPort;
import com.doodle.minidoodle.domain.port.out.UserRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MeetingService implements MeetingUseCase {

    private final UserRepositoryPort userRepository;
    private final CalendarRepositoryPort calendarRepository;
    private final TimeSlotRepositoryPort timeSlotRepository;
    private final MeetingRepositoryPort meetingRepository;

    @Override
    @Transactional
    public Meeting scheduleMeeting(ScheduleMeetingCommand command) {
        Calendar calendar = requireCalendar(command.userId());
        TimeSlot slot = timeSlotRepository.findByIdAndCalendarId(command.slotId(), calendar.id())
                .orElseThrow(() -> new TimeSlotNotFoundException(command.slotId()));

        if (slot.isBusy()) {
            throw new SlotAlreadyBookedException(command.slotId());
        }

        for (UUID participantId : command.participantIds()) {
            if (!userRepository.existsById(participantId)) {
                throw new UserNotFoundException(participantId);
            }
        }

        Instant now = Instant.now();
        UUID meetingId = UUID.randomUUID();

        Meeting meeting = new Meeting(
                meetingId,
                slot.id(),
                slot.startTime(),
                slot.endTime(),
                command.title(),
                command.description(),
                command.participantIds(),
                now,
                now
        );
        Meeting savedMeeting = meetingRepository.save(meeting);

        timeSlotRepository.save(slot.withMeeting(savedMeeting.id()));

        return savedMeeting;
    }

    @Override
    @Transactional(readOnly = true)
    public Meeting getMeeting(UUID userId, UUID meetingId) {
        Calendar calendar = requireCalendar(userId);
        Meeting meeting = meetingRepository.findById(meetingId)
                .orElseThrow(() -> new MeetingNotFoundException(meetingId));
        TimeSlot slot = timeSlotRepository.findByIdAndCalendarId(meeting.timeSlotId(), calendar.id())
                .orElseThrow(() -> new MeetingNotFoundException(meetingId));
        return meeting;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Meeting> listMeetings(UUID userId, Pageable pageable) {
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException(userId);
        }
        return meetingRepository.findByUserId(userId, pageable);
    }

    private Calendar requireCalendar(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException(userId);
        }
        return calendarRepository.findByUserId(userId)
                .orElseGet(() -> calendarRepository.save(
                        new Calendar(UUID.randomUUID(), userId, Instant.now())
                ));
    }
}

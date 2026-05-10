package com.doodle.minidoodle.domain.service;

import com.doodle.minidoodle.domain.command.ScheduleMeetingCommand;
import com.doodle.minidoodle.domain.exception.SlotAlreadyBookedException;
import com.doodle.minidoodle.domain.exception.TimeSlotNotFoundException;
import com.doodle.minidoodle.domain.model.Calendar;
import com.doodle.minidoodle.domain.model.Meeting;
import com.doodle.minidoodle.domain.model.SlotStatus;
import com.doodle.minidoodle.domain.model.TimeSlot;
import com.doodle.minidoodle.domain.port.out.CalendarRepositoryPort;
import com.doodle.minidoodle.domain.port.out.MeetingRepositoryPort;
import com.doodle.minidoodle.domain.port.out.TimeSlotRepositoryPort;
import com.doodle.minidoodle.domain.port.out.UserRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MeetingServiceTest {

    @Mock UserRepositoryPort userRepository;
    @Mock CalendarRepositoryPort calendarRepository;
    @Mock TimeSlotRepositoryPort timeSlotRepository;
    @Mock MeetingRepositoryPort meetingRepository;
    @InjectMocks MeetingService meetingService;

    private final UUID userId = UUID.randomUUID();
    private final UUID calendarId = UUID.randomUUID();
    private final UUID slotId = UUID.randomUUID();
    private final Instant start = Instant.parse("2026-05-20T09:00:00Z");
    private final Instant end = Instant.parse("2026-05-20T10:00:00Z");

    @Test
    void scheduleMeeting_successfullyConvertsFreSlot() {
        Calendar calendar = new Calendar(calendarId, userId, Instant.now());
        TimeSlot freeSlot = new TimeSlot(slotId, calendarId, start, end, SlotStatus.FREE, null, Instant.now(), Instant.now());
        Meeting savedMeeting = new Meeting(UUID.randomUUID(), slotId, start, end, "Standup", null, Set.of(), Instant.now(), Instant.now());

        when(userRepository.existsById(userId)).thenReturn(true);
        when(calendarRepository.findByUserId(userId)).thenReturn(Optional.of(calendar));
        when(timeSlotRepository.findByIdAndCalendarId(slotId, calendarId)).thenReturn(Optional.of(freeSlot));
        when(meetingRepository.save(any())).thenReturn(savedMeeting);
        when(timeSlotRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Meeting result = meetingService.scheduleMeeting(
                new ScheduleMeetingCommand(userId, slotId, "Standup", null, Set.of()));

        assertThat(result.title()).isEqualTo("Standup");
        assertThat(result.timeSlotId()).isEqualTo(slotId);
        verify(timeSlotRepository).save(any());
    }

    @Test
    void scheduleMeeting_throwsWhenSlotAlreadyBusy() {
        Calendar calendar = new Calendar(calendarId, userId, Instant.now());
        TimeSlot busySlot = new TimeSlot(slotId, calendarId, start, end, SlotStatus.BUSY, UUID.randomUUID(), Instant.now(), Instant.now());

        when(userRepository.existsById(userId)).thenReturn(true);
        when(calendarRepository.findByUserId(userId)).thenReturn(Optional.of(calendar));
        when(timeSlotRepository.findByIdAndCalendarId(slotId, calendarId)).thenReturn(Optional.of(busySlot));

        assertThatThrownBy(() -> meetingService.scheduleMeeting(
                new ScheduleMeetingCommand(userId, slotId, "Meeting", null, Set.of())))
                .isInstanceOf(SlotAlreadyBookedException.class);
    }

    @Test
    void scheduleMeeting_throwsWhenSlotNotFound() {
        Calendar calendar = new Calendar(calendarId, userId, Instant.now());

        when(userRepository.existsById(userId)).thenReturn(true);
        when(calendarRepository.findByUserId(userId)).thenReturn(Optional.of(calendar));
        when(timeSlotRepository.findByIdAndCalendarId(slotId, calendarId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> meetingService.scheduleMeeting(
                new ScheduleMeetingCommand(userId, slotId, "Meeting", null, Set.of())))
                .isInstanceOf(TimeSlotNotFoundException.class);
    }
}

package com.doodle.minidoodle.domain.service;

import com.doodle.minidoodle.domain.command.CreateTimeSlotCommand;
import com.doodle.minidoodle.domain.command.UpdateTimeSlotCommand;
import com.doodle.minidoodle.domain.exception.TimeSlotNotFoundException;
import com.doodle.minidoodle.domain.exception.UserNotFoundException;
import com.doodle.minidoodle.domain.model.Calendar;
import com.doodle.minidoodle.domain.model.SlotStatus;
import com.doodle.minidoodle.domain.model.TimeSlot;
import com.doodle.minidoodle.domain.port.out.CalendarRepositoryPort;
import com.doodle.minidoodle.domain.port.out.TimeSlotRepositoryPort;
import com.doodle.minidoodle.domain.port.out.UserRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TimeSlotServiceTest {

    @Mock UserRepositoryPort userRepository;
    @Mock CalendarRepositoryPort calendarRepository;
    @Mock TimeSlotRepositoryPort timeSlotRepository;
    @InjectMocks TimeSlotService timeSlotService;

    private final UUID userId = UUID.randomUUID();
    private final UUID calendarId = UUID.randomUUID();
    private final Instant start = Instant.parse("2026-05-20T09:00:00Z");
    private final Instant end = Instant.parse("2026-05-20T10:00:00Z");

    @Test
    void createTimeSlot_createsSlotForExistingUser() {
        Calendar calendar = new Calendar(calendarId, userId, Instant.now());
        when(userRepository.existsById(userId)).thenReturn(true);
        when(calendarRepository.findByUserId(userId)).thenReturn(Optional.of(calendar));
        when(timeSlotRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        TimeSlot result = timeSlotService.createTimeSlot(new CreateTimeSlotCommand(userId, start, end));

        assertThat(result.calendarId()).isEqualTo(calendarId);
        assertThat(result.startTime()).isEqualTo(start);
        assertThat(result.endTime()).isEqualTo(end);
        assertThat(result.status()).isEqualTo(SlotStatus.FREE);
        assertThat(result.meetingId()).isNull();
    }

    @Test
    void createTimeSlot_throwsWhenUserNotFound() {
        when(userRepository.existsById(userId)).thenReturn(false);

        assertThatThrownBy(() -> timeSlotService.createTimeSlot(new CreateTimeSlotCommand(userId, start, end)))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void createTimeSlot_createsCalendarIfNotExists() {
        Calendar newCalendar = new Calendar(calendarId, userId, Instant.now());
        when(userRepository.existsById(userId)).thenReturn(true);
        when(calendarRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(calendarRepository.save(any())).thenReturn(newCalendar);
        when(timeSlotRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        TimeSlot result = timeSlotService.createTimeSlot(new CreateTimeSlotCommand(userId, start, end));

        assertThat(result.calendarId()).isEqualTo(calendarId);
        verify(calendarRepository).save(any());
    }

    @Test
    void createTimeSlot_truncatesTimesToMinutePrecision() {
        Instant startWithSeconds = Instant.parse("2026-05-20T09:00:45Z");
        Instant endWithSeconds   = Instant.parse("2026-05-20T10:00:30Z");
        Calendar calendar = new Calendar(calendarId, userId, Instant.now());
        when(userRepository.existsById(userId)).thenReturn(true);
        when(calendarRepository.findByUserId(userId)).thenReturn(Optional.of(calendar));
        when(timeSlotRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        TimeSlot result = timeSlotService.createTimeSlot(new CreateTimeSlotCommand(userId, startWithSeconds, endWithSeconds));

        assertThat(result.startTime()).isEqualTo(startWithSeconds.truncatedTo(ChronoUnit.MINUTES));
        assertThat(result.endTime()).isEqualTo(endWithSeconds.truncatedTo(ChronoUnit.MINUTES));
    }

    @Test
    void updateTimeSlot_truncatesTimesToMinutePrecision() {
        UUID slotId = UUID.randomUUID();
        Instant startWithSeconds = Instant.parse("2026-05-20T11:00:45Z");
        Instant endWithSeconds   = Instant.parse("2026-05-20T12:00:30Z");
        Calendar calendar = new Calendar(calendarId, userId, Instant.now());
        TimeSlot existing = new TimeSlot(slotId, calendarId, start, end, SlotStatus.FREE, null, Instant.now(), Instant.now());
        when(userRepository.existsById(userId)).thenReturn(true);
        when(calendarRepository.findByUserId(userId)).thenReturn(Optional.of(calendar));
        when(timeSlotRepository.findByIdAndCalendarId(slotId, calendarId)).thenReturn(Optional.of(existing));
        when(timeSlotRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        TimeSlot result = timeSlotService.updateTimeSlot(new UpdateTimeSlotCommand(userId, slotId, startWithSeconds, endWithSeconds));

        assertThat(result.startTime()).isEqualTo(startWithSeconds.truncatedTo(ChronoUnit.MINUTES));
        assertThat(result.endTime()).isEqualTo(endWithSeconds.truncatedTo(ChronoUnit.MINUTES));
    }

    @Test
    void deleteTimeSlot_throwsWhenSlotNotInCalendar() {
        UUID slotId = UUID.randomUUID();
        Calendar calendar = new Calendar(calendarId, userId, Instant.now());
        when(userRepository.existsById(userId)).thenReturn(true);
        when(calendarRepository.findByUserId(userId)).thenReturn(Optional.of(calendar));
        when(timeSlotRepository.findByIdAndCalendarId(slotId, calendarId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> timeSlotService.deleteTimeSlot(userId, slotId))
                .isInstanceOf(TimeSlotNotFoundException.class);
    }
}

package com.doodle.minidoodle.domain.service;

import com.doodle.minidoodle.domain.command.CreateTimeSlotCommand;
import com.doodle.minidoodle.domain.command.UpdateTimeSlotCommand;
import com.doodle.minidoodle.domain.exception.SlotAlreadyBookedException;
import com.doodle.minidoodle.domain.exception.TimeSlotNotFoundException;
import com.doodle.minidoodle.domain.exception.UserNotFoundException;
import com.doodle.minidoodle.domain.model.Calendar;
import com.doodle.minidoodle.domain.model.SlotStatus;
import com.doodle.minidoodle.domain.model.TimeSlot;
import com.doodle.minidoodle.domain.port.in.TimeSlotUseCase;
import com.doodle.minidoodle.domain.port.out.CalendarRepositoryPort;
import com.doodle.minidoodle.domain.port.out.TimeSlotRepositoryPort;
import com.doodle.minidoodle.domain.port.out.UserRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TimeSlotService implements TimeSlotUseCase {

    private final UserRepositoryPort userRepository;
    private final CalendarRepositoryPort calendarRepository;
    private final TimeSlotRepositoryPort timeSlotRepository;

    @Override
    @Transactional
    public TimeSlot createTimeSlot(CreateTimeSlotCommand command) {
        if (!userRepository.existsById(command.userId())) {
            throw new UserNotFoundException(command.userId());
        }
        Calendar calendar = getOrCreateCalendar(command.userId());
        TimeSlot slot = new TimeSlot(
                UUID.randomUUID(),
                calendar.id(),
                command.startTime(),
                command.endTime(),
                SlotStatus.FREE,
                null,
                Instant.now(),
                Instant.now()
        );
        return timeSlotRepository.save(slot);
    }

    @Override
    @Transactional
    public TimeSlot updateTimeSlot(UpdateTimeSlotCommand command) {
        Calendar calendar = requireCalendar(command.userId());
        TimeSlot slot = requireSlotInCalendar(command.slotId(), calendar.id());
        if (slot.isBusy()) {
            throw new SlotAlreadyBookedException(command.slotId());
        }
        TimeSlot updated = new TimeSlot(
                slot.id(),
                slot.calendarId(),
                command.startTime(),
                command.endTime(),
                slot.status(),
                slot.meetingId(),
                slot.createdAt(),
                Instant.now()
        );
        return timeSlotRepository.save(updated);
    }

    @Override
    @Transactional
    public void deleteTimeSlot(UUID userId, UUID slotId) {
        Calendar calendar = requireCalendar(userId);
        requireSlotInCalendar(slotId, calendar.id());
        timeSlotRepository.deleteById(slotId);
    }

    @Override
    @Transactional
    public TimeSlot updateSlotStatus(UUID userId, UUID slotId, SlotStatus status) {
        Calendar calendar = requireCalendar(userId);
        TimeSlot slot = requireSlotInCalendar(slotId, calendar.id());
        return timeSlotRepository.save(slot.withStatus(status));
    }

    @Override
    @Transactional(readOnly = true)
    public TimeSlot getTimeSlot(UUID userId, UUID slotId) {
        Calendar calendar = requireCalendar(userId);
        return requireSlotInCalendar(slotId, calendar.id());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TimeSlot> listTimeSlots(UUID userId, Instant from, Instant to, SlotStatus status, Pageable pageable) {
        Calendar calendar = requireCalendar(userId);
        return timeSlotRepository.findByCalendarId(calendar.id(), from, to, status, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TimeSlot> queryAvailability(UUID userId, Instant from, Instant to) {
        Calendar calendar = requireCalendar(userId);
        return timeSlotRepository.findByCalendarIdInRange(calendar.id(), from, to);
    }

    private Calendar getOrCreateCalendar(UUID userId) {
        return calendarRepository.findByUserId(userId)
                .orElseGet(() -> calendarRepository.save(
                        new Calendar(UUID.randomUUID(), userId, Instant.now())
                ));
    }

    private Calendar requireCalendar(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new UserNotFoundException(userId);
        }
        return getOrCreateCalendar(userId);
    }

    private TimeSlot requireSlotInCalendar(UUID slotId, UUID calendarId) {
        return timeSlotRepository.findByIdAndCalendarId(slotId, calendarId)
                .orElseThrow(() -> new TimeSlotNotFoundException(slotId));
    }
}

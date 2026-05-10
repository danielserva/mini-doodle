package com.doodle.minidoodle.domain.port.in;

import com.doodle.minidoodle.domain.command.CreateTimeSlotCommand;
import com.doodle.minidoodle.domain.command.UpdateTimeSlotCommand;
import com.doodle.minidoodle.domain.model.SlotStatus;
import com.doodle.minidoodle.domain.model.TimeSlot;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface TimeSlotUseCase {
    TimeSlot createTimeSlot(CreateTimeSlotCommand command);
    TimeSlot updateTimeSlot(UpdateTimeSlotCommand command);
    void deleteTimeSlot(UUID userId, UUID slotId);
    TimeSlot updateSlotStatus(UUID userId, UUID slotId, SlotStatus status);
    TimeSlot getTimeSlot(UUID userId, UUID slotId);
    Page<TimeSlot> listTimeSlots(UUID userId, Instant from, Instant to, SlotStatus status, Pageable pageable);
    List<TimeSlot> queryAvailability(UUID userId, Instant from, Instant to);
}

package com.doodle.minidoodle.domain.port.out;

import com.doodle.minidoodle.domain.model.SlotStatus;
import com.doodle.minidoodle.domain.model.TimeSlot;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TimeSlotRepositoryPort {
    TimeSlot save(TimeSlot timeSlot);
    Optional<TimeSlot> findById(UUID id);
    Optional<TimeSlot> findByIdAndCalendarId(UUID id, UUID calendarId);
    void deleteById(UUID id);
    Page<TimeSlot> findByCalendarId(UUID calendarId, Instant from, Instant to, SlotStatus status, Pageable pageable);
    List<TimeSlot> findByCalendarIdInRange(UUID calendarId, Instant from, Instant to);
}

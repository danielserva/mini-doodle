package com.doodle.minidoodle.domain.port.out;

import com.doodle.minidoodle.domain.model.Meeting;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface MeetingRepositoryPort {
    Meeting save(Meeting meeting);
    Optional<Meeting> findById(UUID id);
    Optional<Meeting> findByTimeSlotId(UUID timeSlotId);
    Page<Meeting> findByUserId(UUID userId, Pageable pageable);
    void deleteById(UUID id);
}

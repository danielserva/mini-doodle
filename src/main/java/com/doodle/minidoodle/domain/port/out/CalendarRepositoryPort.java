package com.doodle.minidoodle.domain.port.out;

import com.doodle.minidoodle.domain.model.Calendar;

import java.util.Optional;
import java.util.UUID;

public interface CalendarRepositoryPort {
    Calendar save(Calendar calendar);
    Optional<Calendar> findByUserId(UUID userId);
}

package com.doodle.minidoodle.adapter.out.persistence.repository;

import com.doodle.minidoodle.adapter.out.persistence.entity.CalendarEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CalendarJpaRepository extends JpaRepository<CalendarEntity, UUID> {
    Optional<CalendarEntity> findByUserId(UUID userId);
}

package com.doodle.minidoodle.adapter.out.persistence.repository;

import com.doodle.minidoodle.adapter.out.persistence.entity.TimeSlotEntity;
import com.doodle.minidoodle.domain.model.SlotStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TimeSlotJpaRepository extends JpaRepository<TimeSlotEntity, UUID> {

    Optional<TimeSlotEntity> findByIdAndCalendarId(UUID id, UUID calendarId);

    @Query(value = """
            SELECT ts FROM TimeSlotEntity ts
            WHERE ts.calendar.id = :calendarId
            AND (:from IS NULL OR ts.startTime >= :from)
            AND (:to IS NULL OR ts.endTime <= :to)
            AND (:status IS NULL OR ts.status = :status)
            ORDER BY ts.startTime ASC
            """,
            countQuery = """
            SELECT COUNT(ts) FROM TimeSlotEntity ts
            WHERE ts.calendar.id = :calendarId
            AND (:from IS NULL OR ts.startTime >= :from)
            AND (:to IS NULL OR ts.endTime <= :to)
            AND (:status IS NULL OR ts.status = :status)
            """)
    Page<TimeSlotEntity> findByFilters(
            @Param("calendarId") UUID calendarId,
            @Param("from") Instant from,
            @Param("to") Instant to,
            @Param("status") SlotStatus status,
            Pageable pageable
    );

    @Query("""
            SELECT ts FROM TimeSlotEntity ts
            WHERE ts.calendar.id = :calendarId
            AND ts.startTime >= :from
            AND ts.endTime <= :to
            ORDER BY ts.startTime ASC
            """)
    List<TimeSlotEntity> findByCalendarIdInRange(
            @Param("calendarId") UUID calendarId,
            @Param("from") Instant from,
            @Param("to") Instant to
    );
}

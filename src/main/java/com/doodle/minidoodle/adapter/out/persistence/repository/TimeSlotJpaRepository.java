package com.doodle.minidoodle.adapter.out.persistence.repository;

import com.doodle.minidoodle.adapter.out.persistence.entity.TimeSlotEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TimeSlotJpaRepository extends JpaRepository<TimeSlotEntity, UUID>, JpaSpecificationExecutor<TimeSlotEntity> {

    Optional<TimeSlotEntity> findByIdAndCalendarId(UUID id, UUID calendarId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT ts FROM TimeSlotEntity ts WHERE ts.id = :id AND ts.calendar.id = :calendarId")
    Optional<TimeSlotEntity> findByIdAndCalendarIdForUpdate(@Param("id") UUID id, @Param("calendarId") UUID calendarId);

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

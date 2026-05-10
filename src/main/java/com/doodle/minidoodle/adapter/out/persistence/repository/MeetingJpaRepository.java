package com.doodle.minidoodle.adapter.out.persistence.repository;

import com.doodle.minidoodle.adapter.out.persistence.entity.MeetingEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface MeetingJpaRepository extends JpaRepository<MeetingEntity, UUID> {

    Optional<MeetingEntity> findByTimeSlotId(UUID timeSlotId);

    @Query(value = """
            SELECT m FROM MeetingEntity m
            JOIN FETCH m.timeSlot ts
            JOIN ts.calendar c
            WHERE c.userId = :userId
            ORDER BY ts.startTime DESC
            """,
            countQuery = """
            SELECT COUNT(m) FROM MeetingEntity m
            JOIN m.timeSlot ts
            JOIN ts.calendar c
            WHERE c.userId = :userId
            """)
    Page<MeetingEntity> findByUserId(@Param("userId") UUID userId, Pageable pageable);
}

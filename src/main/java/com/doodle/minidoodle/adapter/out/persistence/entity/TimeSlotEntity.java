package com.doodle.minidoodle.adapter.out.persistence.entity;

import com.doodle.minidoodle.domain.model.SlotStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "time_slots", indexes = {
        @Index(name = "idx_time_slots_calendar_id", columnList = "calendar_id"),
        @Index(name = "idx_time_slots_calendar_start", columnList = "calendar_id, start_time"),
        @Index(name = "idx_time_slots_status", columnList = "status")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimeSlotEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "calendar_id", nullable = false)
    private CalendarEntity calendar;

    @Column(nullable = false)
    private Instant startTime;

    @Column(nullable = false)
    private Instant endTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private SlotStatus status;

    @Column(name = "meeting_id")
    private UUID meetingId;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;
}

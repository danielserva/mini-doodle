package com.doodle.minidoodle.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "meetings", indexes = {
        @Index(name = "idx_meetings_time_slot_id", columnList = "time_slot_id")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MeetingEntity {

    @Id
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "time_slot_id", nullable = false, unique = true)
    private TimeSlotEntity timeSlot;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "meeting_participants",
            joinColumns = @JoinColumn(name = "meeting_id"),
            indexes = @Index(name = "idx_meeting_participants_user_id", columnList = "user_id"))
    @Column(name = "user_id")
    @Builder.Default
    private Set<UUID> participantIds = new HashSet<>();

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;
}

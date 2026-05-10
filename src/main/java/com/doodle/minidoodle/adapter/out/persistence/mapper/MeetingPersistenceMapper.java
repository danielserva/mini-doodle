package com.doodle.minidoodle.adapter.out.persistence.mapper;

import com.doodle.minidoodle.adapter.out.persistence.entity.MeetingEntity;
import com.doodle.minidoodle.adapter.out.persistence.entity.TimeSlotEntity;
import com.doodle.minidoodle.domain.model.Meeting;
import org.springframework.stereotype.Component;

@Component
public class MeetingPersistenceMapper {

    public Meeting toDomain(MeetingEntity entity) {
        TimeSlotEntity slot = entity.getTimeSlot();
        return new Meeting(
                entity.getId(),
                slot.getId(),
                slot.getStartTime(),
                slot.getEndTime(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getParticipantIds(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public MeetingEntity toEntity(Meeting meeting, TimeSlotEntity timeSlot) {
        return MeetingEntity.builder()
                .id(meeting.id())
                .timeSlot(timeSlot)
                .title(meeting.title())
                .description(meeting.description())
                .participantIds(meeting.participantIds())
                .createdAt(meeting.createdAt())
                .updatedAt(meeting.updatedAt())
                .build();
    }
}

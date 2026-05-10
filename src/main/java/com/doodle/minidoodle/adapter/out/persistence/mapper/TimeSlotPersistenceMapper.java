package com.doodle.minidoodle.adapter.out.persistence.mapper;

import com.doodle.minidoodle.adapter.out.persistence.entity.CalendarEntity;
import com.doodle.minidoodle.adapter.out.persistence.entity.TimeSlotEntity;
import com.doodle.minidoodle.domain.model.TimeSlot;
import org.springframework.stereotype.Component;

@Component
public class TimeSlotPersistenceMapper {

    public TimeSlot toDomain(TimeSlotEntity entity) {
        return new TimeSlot(
                entity.getId(),
                entity.getCalendar().getId(),
                entity.getStartTime(),
                entity.getEndTime(),
                entity.getStatus(),
                entity.getMeetingId(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public TimeSlotEntity toEntity(TimeSlot slot, CalendarEntity calendar) {
        return TimeSlotEntity.builder()
                .id(slot.id())
                .calendar(calendar)
                .startTime(slot.startTime())
                .endTime(slot.endTime())
                .status(slot.status())
                .meetingId(slot.meetingId())
                .createdAt(slot.createdAt())
                .updatedAt(slot.updatedAt())
                .build();
    }
}

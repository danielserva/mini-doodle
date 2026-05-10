package com.doodle.minidoodle.adapter.out.persistence.mapper;

import com.doodle.minidoodle.adapter.out.persistence.entity.CalendarEntity;
import com.doodle.minidoodle.domain.model.Calendar;
import org.springframework.stereotype.Component;

@Component
public class CalendarPersistenceMapper {

    public Calendar toDomain(CalendarEntity entity) {
        return new Calendar(entity.getId(), entity.getUserId(), entity.getCreatedAt());
    }

    public CalendarEntity toEntity(Calendar calendar) {
        return CalendarEntity.builder()
                .id(calendar.id())
                .userId(calendar.userId())
                .createdAt(calendar.createdAt())
                .build();
    }
}

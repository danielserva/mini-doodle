package com.doodle.minidoodle.adapter.out.persistence.adapter;

import com.doodle.minidoodle.adapter.out.persistence.mapper.CalendarPersistenceMapper;
import com.doodle.minidoodle.adapter.out.persistence.repository.CalendarJpaRepository;
import com.doodle.minidoodle.domain.model.Calendar;
import com.doodle.minidoodle.domain.port.out.CalendarRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CalendarPersistenceAdapter implements CalendarRepositoryPort {

    private final CalendarJpaRepository repository;
    private final CalendarPersistenceMapper mapper;

    @Override
    public Calendar save(Calendar calendar) {
        return mapper.toDomain(repository.save(mapper.toEntity(calendar)));
    }

    @Override
    public Optional<Calendar> findByUserId(UUID userId) {
        return repository.findByUserId(userId).map(mapper::toDomain);
    }
}

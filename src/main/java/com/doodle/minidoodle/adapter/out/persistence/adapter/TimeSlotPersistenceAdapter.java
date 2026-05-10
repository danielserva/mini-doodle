package com.doodle.minidoodle.adapter.out.persistence.adapter;

import com.doodle.minidoodle.adapter.out.persistence.entity.CalendarEntity;
import com.doodle.minidoodle.adapter.out.persistence.entity.TimeSlotEntity;
import com.doodle.minidoodle.adapter.out.persistence.mapper.TimeSlotPersistenceMapper;
import com.doodle.minidoodle.adapter.out.persistence.repository.CalendarJpaRepository;
import com.doodle.minidoodle.adapter.out.persistence.repository.TimeSlotJpaRepository;
import com.doodle.minidoodle.domain.model.SlotStatus;
import com.doodle.minidoodle.domain.model.TimeSlot;
import com.doodle.minidoodle.domain.port.out.TimeSlotRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TimeSlotPersistenceAdapter implements TimeSlotRepositoryPort {

    private final TimeSlotJpaRepository timeSlotRepository;
    private final CalendarJpaRepository calendarRepository;
    private final TimeSlotPersistenceMapper mapper;

    @Override
    public TimeSlot save(TimeSlot timeSlot) {
        CalendarEntity calendar = calendarRepository.getReferenceById(timeSlot.calendarId());
        TimeSlotEntity entity = timeSlotRepository.findById(timeSlot.id())
                .map(existing -> {
                    existing.setStartTime(timeSlot.startTime());
                    existing.setEndTime(timeSlot.endTime());
                    existing.setStatus(timeSlot.status());
                    existing.setMeetingId(timeSlot.meetingId());
                    existing.setUpdatedAt(timeSlot.updatedAt());
                    return existing;
                })
                .orElseGet(() -> mapper.toEntity(timeSlot, calendar));
        return mapper.toDomain(timeSlotRepository.save(entity));
    }

    @Override
    public Optional<TimeSlot> findById(UUID id) {
        return timeSlotRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<TimeSlot> findByIdAndCalendarId(UUID id, UUID calendarId) {
        return timeSlotRepository.findByIdAndCalendarId(id, calendarId).map(mapper::toDomain);
    }

    @Override
    public Optional<TimeSlot> findByIdAndCalendarIdForUpdate(UUID id, UUID calendarId) {
        return timeSlotRepository.findByIdAndCalendarIdForUpdate(id, calendarId).map(mapper::toDomain);
    }

    @Override
    public void deleteById(UUID id) {
        timeSlotRepository.deleteById(id);
    }

    @Override
    public Page<TimeSlot> findByCalendarId(UUID calendarId, Instant from, Instant to, SlotStatus status, Pageable pageable) {
        Specification<TimeSlotEntity> spec = buildSpec(calendarId, from, to, status);
        Pageable sorted = pageable.isPaged()
                ? PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by("startTime").ascending())
                : pageable;
        return timeSlotRepository.findAll(spec, sorted).map(mapper::toDomain);
    }

    private Specification<TimeSlotEntity> buildSpec(UUID calendarId, Instant from, Instant to, SlotStatus status) {
        List<Specification<TimeSlotEntity>> predicates = new ArrayList<>();
        predicates.add((root, q, cb) -> cb.equal(root.get("calendar").get("id"), calendarId));
        if (from != null)   predicates.add((root, q, cb) -> cb.greaterThanOrEqualTo(root.get("startTime"), from));
        if (to != null)     predicates.add((root, q, cb) -> cb.lessThanOrEqualTo(root.get("endTime"), to));
        if (status != null) predicates.add((root, q, cb) -> cb.equal(root.get("status"), status));
        return predicates.stream().reduce(Specification.where(null), Specification::and);
    }

    @Override
    public List<TimeSlot> findByCalendarIdInRange(UUID calendarId, Instant from, Instant to) {
        return timeSlotRepository.findByCalendarIdInRange(calendarId, from, to)
                .stream()
                .map(mapper::toDomain)
                .toList();
    }
}

package com.doodle.minidoodle.adapter.out.persistence.adapter;

import com.doodle.minidoodle.adapter.out.persistence.entity.TimeSlotEntity;
import com.doodle.minidoodle.adapter.out.persistence.mapper.MeetingPersistenceMapper;
import com.doodle.minidoodle.adapter.out.persistence.repository.MeetingJpaRepository;
import com.doodle.minidoodle.adapter.out.persistence.repository.TimeSlotJpaRepository;
import com.doodle.minidoodle.domain.model.Meeting;
import com.doodle.minidoodle.domain.port.out.MeetingRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class MeetingPersistenceAdapter implements MeetingRepositoryPort {

    private final MeetingJpaRepository meetingRepository;
    private final TimeSlotJpaRepository timeSlotRepository;
    private final MeetingPersistenceMapper mapper;

    @Override
    public Meeting save(Meeting meeting) {
        TimeSlotEntity timeSlot = timeSlotRepository.getReferenceById(meeting.timeSlotId());
        return mapper.toDomain(meetingRepository.save(mapper.toEntity(meeting, timeSlot)));
    }

    @Override
    public Optional<Meeting> findById(UUID id) {
        return meetingRepository.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<Meeting> findByTimeSlotId(UUID timeSlotId) {
        return meetingRepository.findByTimeSlotId(timeSlotId).map(mapper::toDomain);
    }

    @Override
    public Page<Meeting> findByUserId(UUID userId, Pageable pageable) {
        return meetingRepository.findByUserId(userId, pageable).map(mapper::toDomain);
    }

    @Override
    public void deleteById(UUID id) {
        meetingRepository.deleteById(id);
    }
}

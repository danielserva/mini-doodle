package com.doodle.minidoodle.adapter.in.rest.response;

import com.doodle.minidoodle.domain.model.SlotStatus;
import com.doodle.minidoodle.domain.model.TimeSlot;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record AvailabilityResponse(
        UUID userId,
        Instant from,
        Instant to,
        long totalSlots,
        long freeSlots,
        long busySlots,
        List<SlotSummary> slots
) {
    public record SlotSummary(UUID id, Instant startTime, Instant endTime, SlotStatus status, UUID meetingId) {
        public static SlotSummary from(TimeSlot slot) {
            return new SlotSummary(slot.id(), slot.startTime(), slot.endTime(), slot.status(), slot.meetingId());
        }
    }

    public static AvailabilityResponse of(UUID userId, Instant from, Instant to, List<TimeSlot> slots) {
        List<SlotSummary> summaries = slots.stream().map(SlotSummary::from).toList();
        long free = slots.stream().filter(TimeSlot::isFree).count();
        long busy = slots.stream().filter(TimeSlot::isBusy).count();
        return new AvailabilityResponse(userId, from, to, slots.size(), free, busy, summaries);
    }
}

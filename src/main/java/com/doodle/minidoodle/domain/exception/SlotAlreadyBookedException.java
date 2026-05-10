package com.doodle.minidoodle.domain.exception;

import java.util.UUID;

public class SlotAlreadyBookedException extends RuntimeException {
    public SlotAlreadyBookedException(UUID slotId) {
        super("Time slot is already booked: " + slotId);
    }
}

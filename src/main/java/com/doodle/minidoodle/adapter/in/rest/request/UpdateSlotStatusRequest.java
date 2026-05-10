package com.doodle.minidoodle.adapter.in.rest.request;

import com.doodle.minidoodle.domain.model.SlotStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateSlotStatusRequest(@NotNull SlotStatus status) {}

package com.doodle.minidoodle.adapter.in.rest.response;

import com.doodle.minidoodle.domain.model.User;

import java.time.Instant;
import java.util.UUID;

public record UserResponse(UUID id, String email, String name, Instant createdAt) {

    public static UserResponse from(User user) {
        return new UserResponse(user.id(), user.email(), user.name(), user.createdAt());
    }
}

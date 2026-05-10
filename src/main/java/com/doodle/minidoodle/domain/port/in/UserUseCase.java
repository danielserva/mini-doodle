package com.doodle.minidoodle.domain.port.in;

import com.doodle.minidoodle.domain.command.CreateUserCommand;
import com.doodle.minidoodle.domain.model.User;

import java.util.UUID;

public interface UserUseCase {
    User createUser(CreateUserCommand command);
    User getUser(UUID userId);
}

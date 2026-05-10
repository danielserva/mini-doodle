package com.doodle.minidoodle.domain.service;

import com.doodle.minidoodle.domain.command.CreateUserCommand;
import com.doodle.minidoodle.domain.exception.DuplicateEmailException;
import com.doodle.minidoodle.domain.exception.UserNotFoundException;
import com.doodle.minidoodle.domain.model.User;
import com.doodle.minidoodle.domain.port.in.UserUseCase;
import com.doodle.minidoodle.domain.port.out.UserRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService implements UserUseCase {

    private final UserRepositoryPort userRepository;

    @Override
    @Transactional
    public User createUser(CreateUserCommand command) {
        if (userRepository.findByEmail(command.email()).isPresent()) {
            throw new DuplicateEmailException(command.email());
        }
        User user = new User(UUID.randomUUID(), command.email(), command.name(), Instant.now());
        return userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public User getUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }
}

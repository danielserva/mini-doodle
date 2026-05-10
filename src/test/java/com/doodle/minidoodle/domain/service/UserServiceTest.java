package com.doodle.minidoodle.domain.service;

import com.doodle.minidoodle.domain.command.CreateUserCommand;
import com.doodle.minidoodle.domain.exception.DuplicateEmailException;
import com.doodle.minidoodle.domain.exception.UserNotFoundException;
import com.doodle.minidoodle.domain.model.User;
import com.doodle.minidoodle.domain.port.out.UserRepositoryPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock UserRepositoryPort userRepository;
    @InjectMocks UserService userService;

    private final UUID userId = UUID.randomUUID();
    private final User user = new User(userId, "alice@example.com", "Alice", Instant.now());

    @Test
    void createUser_savesAndReturnsUser() {
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(any())).thenReturn(user);

        User result = userService.createUser(new CreateUserCommand("alice@example.com", "Alice"));

        assertThat(result.email()).isEqualTo("alice@example.com");
        assertThat(result.name()).isEqualTo("Alice");
    }

    @Test
    void createUser_throwsOnDuplicateEmail() {
        when(userRepository.findByEmail("alice@example.com")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userService.createUser(new CreateUserCommand("alice@example.com", "Alice")))
                .isInstanceOf(DuplicateEmailException.class);
    }

    @Test
    void getUser_returnsUserWhenFound() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        User result = userService.getUser(userId);

        assertThat(result.id()).isEqualTo(userId);
    }

    @Test
    void getUser_throwsWhenNotFound() {
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUser(userId))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    void listUsers_returnsPageFromRepository() {
        Pageable pageable = PageRequest.of(0, 20);
        User second = new User(UUID.randomUUID(), "bob@example.com", "Bob", Instant.now());
        when(userRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(user, second)));

        var result = userService.listUsers(pageable);

        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent()).extracting(User::email)
                .containsExactly("alice@example.com", "bob@example.com");
    }

    @Test
    void listUsers_returnsEmptyPageWhenNoUsers() {
        Pageable pageable = PageRequest.of(0, 20);
        when(userRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of()));

        var result = userService.listUsers(pageable);

        assertThat(result.getTotalElements()).isZero();
        assertThat(result.getContent()).isEmpty();
    }
}

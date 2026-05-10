package com.doodle.minidoodle.adapter.in.rest;

import com.doodle.minidoodle.adapter.in.rest.request.CreateUserRequest;
import com.doodle.minidoodle.adapter.in.rest.response.PagedResponse;
import com.doodle.minidoodle.adapter.in.rest.response.UserResponse;
import com.doodle.minidoodle.domain.command.CreateUserCommand;
import com.doodle.minidoodle.domain.port.in.UserUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User management")
public class UserController {

    private final UserUseCase userUseCase;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new user")
    public UserResponse createUser(@Valid @RequestBody CreateUserRequest request) {
        return UserResponse.from(userUseCase.createUser(new CreateUserCommand(request.email(), request.name())));
    }

    @GetMapping
    @Operation(summary = "List all users")
    public PagedResponse<UserResponse> listUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return PagedResponse.from(
                userUseCase.listUsers(PageRequest.of(page, size, Sort.by("createdAt").descending())),
                UserResponse::from);
    }

    @GetMapping("/{userId}")
    @Operation(summary = "Get a user by ID")
    public UserResponse getUser(@PathVariable UUID userId) {
        return UserResponse.from(userUseCase.getUser(userId));
    }
}

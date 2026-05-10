package com.doodle.minidoodle.adapter.in.rest;

import com.doodle.minidoodle.adapter.in.rest.request.ScheduleMeetingRequest;
import com.doodle.minidoodle.adapter.in.rest.response.MeetingResponse;
import com.doodle.minidoodle.adapter.in.rest.response.PagedResponse;
import com.doodle.minidoodle.domain.command.ScheduleMeetingCommand;
import com.doodle.minidoodle.domain.port.in.MeetingUseCase;
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
@RequiredArgsConstructor
@Tag(name = "Meetings", description = "Meeting scheduling")
public class MeetingController {

    private final MeetingUseCase meetingUseCase;

    @PostMapping("/api/v1/users/{userId}/slots/{slotId}/meeting")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Schedule a meeting by converting a free time slot")
    public MeetingResponse scheduleMeeting(
            @PathVariable UUID userId,
            @PathVariable UUID slotId,
            @Valid @RequestBody ScheduleMeetingRequest request) {
        return MeetingResponse.from(meetingUseCase.scheduleMeeting(new ScheduleMeetingCommand(
                userId, slotId, request.title(), request.description(), request.participantIds())));
    }

    @GetMapping("/api/v1/users/{userId}/meetings")
    @Operation(summary = "List meetings for a user")
    public PagedResponse<MeetingResponse> listMeetings(
            @PathVariable UUID userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("startTime").descending());
        return PagedResponse.from(meetingUseCase.listMeetings(userId, pageable), MeetingResponse::from);
    }

    @GetMapping("/api/v1/users/{userId}/meetings/{meetingId}")
    @Operation(summary = "Get a meeting by ID")
    public MeetingResponse getMeeting(@PathVariable UUID userId, @PathVariable UUID meetingId) {
        return MeetingResponse.from(meetingUseCase.getMeeting(userId, meetingId));
    }
}

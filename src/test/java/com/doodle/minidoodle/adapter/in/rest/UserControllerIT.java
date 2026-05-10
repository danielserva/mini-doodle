package com.doodle.minidoodle.adapter.in.rest;

import com.doodle.minidoodle.adapter.in.rest.request.CreateTimeSlotRequest;
import com.doodle.minidoodle.adapter.in.rest.request.CreateUserRequest;
import com.doodle.minidoodle.adapter.in.rest.request.ScheduleMeetingRequest;
import com.doodle.minidoodle.adapter.in.rest.response.MeetingResponse;
import com.doodle.minidoodle.adapter.in.rest.response.TimeSlotResponse;
import com.doodle.minidoodle.adapter.in.rest.response.UserResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class UserControllerIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @Test
    void fullMeetingSchedulingFlow() throws Exception {
        // Create organizer
        UserResponse organizer = createUser("organizer@test.com", "Organizer");
        assertThat(organizer.email()).isEqualTo("organizer@test.com");

        // Create participant
        UserResponse participant = createUser("participant@test.com", "Participant");

        // Create a time slot
        Instant start = Instant.parse("2026-06-01T09:00:00Z");
        Instant end = Instant.parse("2026-06-01T10:00:00Z");
        TimeSlotResponse slot = createTimeSlot(organizer.id().toString(), start, end);
        assertThat(slot.status().name()).isEqualTo("FREE");
        assertThat(slot.meetingId()).isNull();

        // Schedule a meeting on the slot
        MeetingResponse meeting = scheduleMeeting(
                organizer.id().toString(),
                slot.id().toString(),
                "Team Standup",
                "Daily standup meeting",
                Set.of(participant.id()));
        assertThat(meeting.title()).isEqualTo("Team Standup");
        assertThat(meeting.participantIds()).contains(participant.id());

        // Slot should now be BUSY
        mockMvc.perform(get("/api/v1/users/{userId}/slots/{slotId}", organizer.id(), slot.id()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("BUSY"))
                .andExpect(jsonPath("$.meetingId").value(meeting.id().toString()));

        // Scheduling on a busy slot should fail
        mockMvc.perform(post("/api/v1/users/{userId}/slots/{slotId}/meeting", organizer.id(), slot.id())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new ScheduleMeetingRequest("Another", null, Set.of()))))
                .andExpect(status().isConflict());
    }

    @Test
    void createUser_rejectsDuplicateEmail() throws Exception {
        createUser("unique@test.com", "User One");

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateUserRequest("unique@test.com", "User Two"))))
                .andExpect(status().isConflict());
    }

    @Test
    void getUser_returnsNotFoundForUnknownId() throws Exception {
        mockMvc.perform(get("/api/v1/users/00000000-0000-0000-0000-000000000000"))
                .andExpect(status().isNotFound());
    }

    private UserResponse createUser(String email, String name) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateUserRequest(email, name))))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readValue(result.getResponse().getContentAsString(), UserResponse.class);
    }

    private TimeSlotResponse createTimeSlot(String userId, Instant start, Instant end) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/users/{userId}/slots", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateTimeSlotRequest(start, end))))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readValue(result.getResponse().getContentAsString(), TimeSlotResponse.class);
    }

    private MeetingResponse scheduleMeeting(String userId, String slotId, String title, String description, Set<?> participants) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/users/{userId}/slots/{slotId}/meeting", userId, slotId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ScheduleMeetingRequest(title, description, (Set<java.util.UUID>) participants))))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readValue(result.getResponse().getContentAsString(), MeetingResponse.class);
    }
}

package com.doodle.minidoodle.adapter.in.rest;

import com.doodle.minidoodle.adapter.in.rest.request.CreateTimeSlotRequest;
import com.doodle.minidoodle.adapter.in.rest.request.CreateUserRequest;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class AvailabilityControllerIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @Test
    void getAvailability_withExplicitRange_returnsMatchingSlots() throws Exception {
        UserResponse user = createUser("avail-explicit@test.com", "Explicit Range User");

        Instant start = Instant.parse("2030-01-01T09:00:00Z");
        Instant end   = Instant.parse("2030-01-01T10:00:00Z");
        createTimeSlot(user.id().toString(), start, end);

        mockMvc.perform(get("/api/v1/users/{userId}/availability", user.id())
                        .param("from", "2030-01-01T00:00:00Z")
                        .param("to",   "2030-01-02T00:00:00Z"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(user.id().toString()))
                .andExpect(jsonPath("$.totalSlots").value(1))
                .andExpect(jsonPath("$.freeSlots").value(1))
                .andExpect(jsonPath("$.busySlots").value(0))
                .andExpect(jsonPath("$.slots.length()").value(1));
    }

    @Test
    void getAvailability_withoutRange_defaultsToSevenDayWindow() throws Exception {
        UserResponse user = createUser("avail-default@test.com", "Default Window User");

        mockMvc.perform(get("/api/v1/users/{userId}/availability", user.id()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(user.id().toString()))
                .andExpect(jsonPath("$.from").exists())
                .andExpect(jsonPath("$.to").exists())
                .andExpect(jsonPath("$.totalSlots").value(0));
    }

    @Test
    void getAvailability_withOnlyFrom_returnsBadRequest() throws Exception {
        UserResponse user = createUser("avail-from-only@test.com", "From Only User");

        mockMvc.perform(get("/api/v1/users/{userId}/availability", user.id())
                        .param("from", "2030-01-01T00:00:00Z"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAvailability_withOnlyTo_returnsBadRequest() throws Exception {
        UserResponse user = createUser("avail-to-only@test.com", "To Only User");

        mockMvc.perform(get("/api/v1/users/{userId}/availability", user.id())
                        .param("to", "2030-01-08T00:00:00Z"))
                .andExpect(status().isBadRequest());
    }

    private UserResponse createUser(String email, String name) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateUserRequest(email, name))))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readValue(result.getResponse().getContentAsString(), UserResponse.class);
    }

    private void createTimeSlot(String userId, Instant start, Instant end) throws Exception {
        mockMvc.perform(post("/api/v1/users/{userId}/slots", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateTimeSlotRequest(start, end))))
                .andExpect(status().isCreated());
    }
}

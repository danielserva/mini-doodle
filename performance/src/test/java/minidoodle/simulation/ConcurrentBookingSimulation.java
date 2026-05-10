package minidoodle.simulation;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;
import minidoodle.scenario.SlotScenario;
import minidoodle.scenario.UserScenario;

import java.util.concurrent.atomic.AtomicReference;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

/**
 * Race-condition simulation: 50 users simultaneously attempt to book the same FREE slot.
 * Expected result: exactly one 201 Created, the rest 409 Conflict.
 *
 * Uses .andThen() to guarantee the setup scenario completes (and the shared slot is
 * recorded) before the concurrent booking wave starts.
 */
public class ConcurrentBookingSimulation extends Simulation {

    private static final String BASE_URL = System.getProperty("baseUrl", "http://localhost:8080");

    private static final AtomicReference<String> SHARED_USER_ID = new AtomicReference<>();
    private static final AtomicReference<String> SHARED_SLOT_ID = new AtomicReference<>();

    HttpProtocolBuilder httpProtocol = http
        .baseUrl(BASE_URL)
        .acceptHeader("application/json")
        .contentTypeHeader("application/json");

    ScenarioBuilder setup = scenario("Setup")
        .exec(UserScenario.create())
        .exec(SlotScenario.createSlot(1))
        .exec(session -> {
            SHARED_USER_ID.set(session.getString("userId"));
            SHARED_SLOT_ID.set(session.getString("slotId"));
            return session;
        });

    ScenarioBuilder concurrentBooking = scenario("Concurrent Booking")
        .exec(session -> session
            .set("userId", SHARED_USER_ID.get())
            .set("slotId", SHARED_SLOT_ID.get())
        )
        .exec(
            http("Book Slot")
                .post("/api/v1/users/#{userId}/slots/#{slotId}/meeting")
                .body(StringBody("{\"title\":\"Concurrent Meeting\",\"participantIds\":[]}"))
                // Both 201 (winner) and 409 (losers) are correct outcomes
                .check(status().in(201, 409))
        );

    {
        setUp(
            setup.injectOpen(atOnceUsers(1))
                .andThen(
                    concurrentBooking.injectOpen(atOnceUsers(50))
                )
        ).protocols(httpProtocol)
         .assertions(
             // All requests must get a valid response (201 or 409) — no 500s
             global().successfulRequests().percent().is(100.0)
         );
    }
}

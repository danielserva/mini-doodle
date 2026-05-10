package minidoodle.simulation;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;
import minidoodle.scenario.MeetingScenario;
import minidoodle.scenario.SlotScenario;
import minidoodle.scenario.UserScenario;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

/**
 * Simulates the full scheduling journey:
 *   create user → create 5 slots → schedule meeting → query availability
 *
 * Load profile: ramp to 50 users over 30s, hold for 60s.
 */
public class SchedulingSimulation extends Simulation {

    private static final String BASE_URL = System.getProperty("baseUrl", "http://localhost:8080");

    HttpProtocolBuilder httpProtocol = http
        .baseUrl(BASE_URL)
        .acceptHeader("application/json")
        .contentTypeHeader("application/json");

    ScenarioBuilder scn = scenario("Full Scheduling Journey")
        .exec(UserScenario.create())
        .exec(SlotScenario.createSlot(1))    // slotId saved — this is the one we'll book
        .exec(SlotScenario.createExtra(2))
        .exec(SlotScenario.createExtra(3))
        .exec(SlotScenario.createExtra(4))
        .exec(SlotScenario.createExtra(5))
        .exec(MeetingScenario.schedule())
        .exec(
            http("Get Availability")
                .get("/api/v1/users/#{userId}/availability")
                .queryParam("from", SlotScenario.fromTime())
                .queryParam("to",   SlotScenario.toTime())
                .check(status().is(200))
                .check(jsonPath("$.totalSlots").ofInt().gt(0))
        );

    {
        setUp(
            scn.injectOpen(
                rampUsers(50).during(30)
            )
        ).protocols(httpProtocol)
         .assertions(
             global().responseTime().percentile(99).lt(2000),
             global().successfulRequests().percent().gt(95.0)
         );
    }
}

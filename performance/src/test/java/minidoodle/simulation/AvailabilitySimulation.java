package minidoodle.simulation;

import io.gatling.javaapi.core.*;
import io.gatling.javaapi.http.*;
import minidoodle.scenario.SlotScenario;
import minidoodle.scenario.UserScenario;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

/**
 * Read-heavy simulation targeting the availability aggregation endpoint.
 * Each virtual user creates their own user + 10 slots, then fires
 * 30 availability queries over a 48-hour window.
 *
 * Load profile: ramp to 100 users over 30s.
 */
public class AvailabilitySimulation extends Simulation {

    private static final String BASE_URL = System.getProperty("baseUrl", "http://localhost:8080");

    HttpProtocolBuilder httpProtocol = http
        .baseUrl(BASE_URL)
        .acceptHeader("application/json")
        .contentTypeHeader("application/json");

    ScenarioBuilder scn = scenario("Availability Read Load")
        .group("Setup").on(
            exec(UserScenario.create())
                .exec(SlotScenario.createSlot(1))
                .exec(SlotScenario.createExtra(2))
                .exec(SlotScenario.createExtra(3))
                .exec(SlotScenario.createExtra(4))
                .exec(SlotScenario.createExtra(5))
                .exec(SlotScenario.createExtra(6))
                .exec(SlotScenario.createExtra(7))
                .exec(SlotScenario.createExtra(8))
                .exec(SlotScenario.createExtra(9))
                .exec(SlotScenario.createExtra(10))
        )
        .group("Read").on(
            repeat(30).on(
                exec(
                    http("Get Availability")
                        .get("/api/v1/users/#{userId}/availability")
                        .queryParam("from", SlotScenario.fromTime())
                        .queryParam("to",   SlotScenario.toTime())
                        .check(status().is(200))
                        .check(jsonPath("$.totalSlots").ofInt().is(10))
                )
            )
        );

    {
        setUp(
            scn.injectOpen(
                rampUsers(100).during(30)
            )
        ).protocols(httpProtocol)
         .assertions(
             details("Read", "Get Availability").responseTime().percentile(95).lt(500),
             global().successfulRequests().percent().gt(99.0)
         );
    }
}

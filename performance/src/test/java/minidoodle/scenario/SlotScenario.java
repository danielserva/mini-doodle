package minidoodle.scenario;

import io.gatling.javaapi.core.ChainBuilder;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

public class SlotScenario {

    // Fixed base: start of the next day (UTC), computed once at class load
    private static final Instant BASE = Instant.now().truncatedTo(ChronoUnit.DAYS).plusSeconds(86400);

    public static String fromTime() {
        return BASE.toString();
    }

    public static String toTime() {
        return BASE.plusSeconds(48 * 3600L).toString();
    }

    /** Creates a slot and saves its id as {@code slotId} in the session. */
    public static ChainBuilder createSlot(int hourOffset) {
        String start = BASE.plusSeconds(hourOffset * 3600L).toString();
        String end   = BASE.plusSeconds(hourOffset * 3600L + 3600).toString();
        return exec(
            http("Create Slot +%dh".formatted(hourOffset))
                .post("/api/v1/users/#{userId}/slots")
                .body(StringBody("{\"startTime\":\"%s\",\"endTime\":\"%s\"}".formatted(start, end)))
                .check(status().is(201))
                .check(jsonPath("$.id").saveAs("slotId"))
        );
    }

    /** Creates a slot without saving its id — used to build up slot volume. */
    public static ChainBuilder createExtra(int hourOffset) {
        String start = BASE.plusSeconds(hourOffset * 3600L).toString();
        String end   = BASE.plusSeconds(hourOffset * 3600L + 3600).toString();
        return exec(
            http("Create Slot +%dh".formatted(hourOffset))
                .post("/api/v1/users/#{userId}/slots")
                .body(StringBody("{\"startTime\":\"%s\",\"endTime\":\"%s\"}".formatted(start, end)))
                .check(status().is(201))
        );
    }
}

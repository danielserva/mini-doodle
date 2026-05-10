package minidoodle.scenario;

import io.gatling.javaapi.core.ChainBuilder;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

public class MeetingScenario {

    public static ChainBuilder schedule() {
        return exec(
            http("Schedule Meeting")
                .post("/api/v1/users/#{userId}/slots/#{slotId}/meeting")
                .body(StringBody("{\"title\":\"Performance Meeting\",\"participantIds\":[]}"))
                .check(status().is(201))
                .check(jsonPath("$.id").saveAs("meetingId"))
        );
    }
}

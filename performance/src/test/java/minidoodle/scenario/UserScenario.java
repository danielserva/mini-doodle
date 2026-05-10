package minidoodle.scenario;

import io.gatling.javaapi.core.ChainBuilder;

import java.util.UUID;

import static io.gatling.javaapi.core.CoreDsl.*;
import static io.gatling.javaapi.http.HttpDsl.*;

public class UserScenario {

    public static ChainBuilder create() {
        return exec(
            http("Create User")
                .post("/api/v1/users")
                .body(StringBody(session ->
                    "{\"email\":\"user-%s@perf.test\",\"name\":\"Perf User\"}".formatted(UUID.randomUUID())
                ))
                .check(status().is(201))
                .check(jsonPath("$.id").saveAs("userId"))
        );
    }
}

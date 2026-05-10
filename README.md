# Mini Doodle

Meeting scheduling platform built with Spring Boot 3, Java 21, PostgreSQL, and hexagonal architecture.

Users create time slots, convert them into meetings, and query their availability over a time range. Concurrent booking attempts on the same slot are safe — a pessimistic lock (`SELECT FOR UPDATE`) ensures exactly one winner and clean `409 Conflict` responses for the rest.

## Architecture

Hexagonal (Ports and Adapters):

```
src/main/java/com/doodle/minidoodle/
├── domain/
│   ├── model/          ← immutable Java records (User, Calendar, TimeSlot, Meeting)
│   ├── command/        ← input commands
│   ├── exception/      ← domain exceptions
│   ├── port/
│   │   ├── in/         ← use case interfaces (UserUseCase, TimeSlotUseCase, MeetingUseCase)
│   │   └── out/        ← repository port interfaces
│   └── service/        ← business logic, zero infrastructure dependencies
├── adapter/
│   ├── in/rest/        ← Spring MVC controllers and DTOs
│   └── out/persistence/← JPA entities, Spring Data repos, mappers, adapters
└── config/             ← OpenAPI configuration
```

Diagrams (PlantUML) are in [docs/](docs/):
- `class-diagram.puml` — full class diagram
- `database-diagram.puml` — ER diagram
- `uc-*.puml` — sequence diagram per use case

## Running locally

**Prerequisite:** Docker

```bash
docker compose up --build
```

The service starts on `http://localhost:8080`.

| Endpoint | URL |
|---|---|
| Swagger UI | http://localhost:8080/swagger-ui.html |
| Health | http://localhost:8080/actuator/health |
| Prometheus metrics | http://localhost:8080/actuator/prometheus |

## API

### Users

| Method | Path | Description |
|---|---|---|
| `POST` | `/api/v1/users` | Create a user |
| `GET` | `/api/v1/users` | List all users (`?page=&size=`) |
| `GET` | `/api/v1/users/{userId}` | Get a user |

```json
POST /api/v1/users
{ "email": "alice@example.com", "name": "Alice" }
```

### Time Slots

| Method | Path | Description |
|---|---|---|
| `POST` | `/api/v1/users/{userId}/slots` | Create a time slot |
| `GET` | `/api/v1/users/{userId}/slots` | List slots (`?from=&to=&status=&page=&size=`) |
| `GET` | `/api/v1/users/{userId}/slots/{slotId}` | Get a slot |
| `PUT` | `/api/v1/users/{userId}/slots/{slotId}` | Update a slot's time range |
| `DELETE` | `/api/v1/users/{userId}/slots/{slotId}` | Delete a slot |
| `PATCH` | `/api/v1/users/{userId}/slots/{slotId}/status` | Set status (`FREE` or `BUSY`) |

```json
POST /api/v1/users/{userId}/slots
{ "startTime": "2026-06-01T09:00Z", "endTime": "2026-06-01T10:00Z" }
```

`startTime` and `endTime` accept ISO 8601 date-time with optional timezone (defaults to UTC). Values are truncated to minute precision — seconds are ignored. The same applies to `PUT /slots/{slotId}`.

### Meetings

Scheduling a meeting atomically converts the slot from `FREE` to `BUSY`. Concurrent requests on the same slot are serialized via a database-level row lock — only the first succeeds with `201 Created`, the rest receive `409 Conflict`.

| Method | Path | Description |
|---|---|---|
| `POST` | `/api/v1/users/{userId}/slots/{slotId}/meeting` | Schedule a meeting |
| `GET` | `/api/v1/users/{userId}/meetings` | List meetings (`?page=&size=`) |
| `GET` | `/api/v1/users/{userId}/meetings/{meetingId}` | Get a meeting |

```json
POST /api/v1/users/{userId}/slots/{slotId}/meeting
{
  "title": "Team Standup",
  "description": "Daily sync",
  "participantIds": ["<uuid>", "<uuid>"]
}
```

### Availability

Returns all slots in a time range with free/busy counts.

`from` and `to` are optional. If both are omitted, the window defaults to now through 7 days from now. Providing exactly one of them returns `400 Bad Request`.

```
GET /api/v1/users/{userId}/availability?from=2026-06-01T00:00:00Z&to=2026-06-07T00:00:00Z
GET /api/v1/users/{userId}/availability
```

```json
{
  "userId": "...",
  "from": "2026-06-01T00:00:00Z",
  "to": "2026-06-07T00:00:00Z",
  "totalSlots": 10,
  "freeSlots": 7,
  "busySlots": 3,
  "slots": [
    { "id": "...", "startTime": "...", "endTime": "...", "status": "FREE", "meetingId": null },
    { "id": "...", "startTime": "...", "endTime": "...", "status": "BUSY", "meetingId": "..." }
  ]
}
```

## Tests

```bash
# Unit tests — no Docker needed
./mvnw test -Dtest="*ServiceTest"

# All tests including integration (requires Docker)
./mvnw test
```

Integration tests spin up a real PostgreSQL instance via Testcontainers.

## Performance tests

Gatling simulations live in [performance/](performance/). They require the service to be running.

```bash
# Start the service
docker compose up --build -d

# Run all simulations
cd performance && mvn gatling:test

# Run a single simulation
mvn gatling:test -Dgatling.simulationClass=minidoodle.simulation.ConcurrentBookingSimulation

# Target a different host
mvn gatling:test -DbaseUrl=http://staging.example.com
```

HTML reports are written to `performance/target/gatling/<simulation>-<timestamp>/index.html`.

| Simulation | Load profile | Assertion |
|---|---|---|
| `SchedulingSimulation` | Ramp 0→50 users over 30 s | p99 < 2 s, success > 95% |
| `AvailabilitySimulation` | Ramp 0→100 users over 30 s | p95 reads < 500 ms, success > 99% |
| `ConcurrentBookingSimulation` | 50 users at once on one slot | 100% valid responses (201 or 409, no 500s) |

See [performance/README.md](performance/README.md) for full details.

## Tech stack

| | |
|---|---|
| Runtime | Java 21, Spring Boot 3.3.5 |
| Database | PostgreSQL 16, Spring Data JPA, Liquibase |
| API docs | SpringDoc OpenAPI 2.6 (Swagger UI) |
| Metrics | Micrometer + Prometheus |
| Testing | JUnit 5, Mockito, Testcontainers |
| Performance | Gatling 3.13 (Java DSL) |

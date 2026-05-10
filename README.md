# Mini Doodle

Meeting scheduling platform built with Spring Boot 3, Java 21, PostgreSQL, and hexagonal architecture.

Users create time slots, convert them into meetings, and query their availability over a time range.

## Running locally

**Prerequisite:** Docker

```bash
docker compose up --build
```

The service starts on `http://localhost:8080`.

| Endpoint | URL |
|----------|-----|
| Swagger UI | http://localhost:8080/swagger-ui.html |
| Health | http://localhost:8080/actuator/health |
| Prometheus metrics | http://localhost:8080/actuator/prometheus |

## API

### Users

```
POST /api/v1/users
GET  /api/v1/users/{userId}
```

```json
POST /api/v1/users
{ "email": "alice@example.com", "name": "Alice" }
```

### Time Slots

```
POST   /api/v1/users/{userId}/slots
GET    /api/v1/users/{userId}/slots?from=&to=&status=&page=&size=
GET    /api/v1/users/{userId}/slots/{slotId}
PUT    /api/v1/users/{userId}/slots/{slotId}
DELETE /api/v1/users/{userId}/slots/{slotId}
PATCH  /api/v1/users/{userId}/slots/{slotId}/status
```

```json
POST /api/v1/users/{userId}/slots
{ "startTime": "2026-06-01T09:00:00Z", "endTime": "2026-06-01T10:00:00Z" }
```

### Meetings

Scheduling a meeting converts the slot from `FREE` to `BUSY`.

```
POST /api/v1/users/{userId}/slots/{slotId}/meeting
GET  /api/v1/users/{userId}/meetings
GET  /api/v1/users/{userId}/meetings/{meetingId}
```

```json
POST /api/v1/users/{userId}/slots/{slotId}/meeting
{
  "title": "Team Standup",
  "description": "Daily sync",
  "participantIds": ["<uuid>", "<uuid>"]
}
```

### Availability

```
GET /api/v1/users/{userId}/availability?from=2026-06-01T00:00:00Z&to=2026-06-07T00:00:00Z
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
# Unit tests (no Docker needed)
./mvnw test -Dtest="*ServiceTest"

# All tests including integration
./mvnw test
```

Integration tests spin up a real PostgreSQL instance via Testcontainers.

## Performance tests

Gatling simulations live in [performance/](performance/). They require the service to be running.

```bash
docker compose up --build -d
cd performance && mvn gatling:test
```

See [performance/README.md](performance/README.md) for details on each simulation.

## Tech stack

| | |
|---|---|
| Runtime | Java 21, Spring Boot 3.3 |
| Database | PostgreSQL 16, Spring Data JPA, Liquibase |
| API docs | SpringDoc OpenAPI (Swagger UI) |
| Metrics | Micrometer + Prometheus |
| Testing | JUnit 5, Mockito, Testcontainers |

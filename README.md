# Mini Doodle — Meeting Scheduling Platform

A high-performance meeting scheduling service built with Spring Boot 3, Java 21, and hexagonal architecture.

## Architecture

The project follows **Hexagonal Architecture (Ports and Adapters)**:

```
domain/
  model/        ← Pure Java records: User, Calendar, TimeSlot, Meeting, SlotStatus
  command/      ← Input value objects for use cases
  exception/    ← Domain exceptions
  port/in/      ← Inbound ports (use case interfaces)
  port/out/     ← Outbound ports (repository interfaces)
  service/      ← Domain services implementing use cases

adapter/
  in/rest/      ← REST controllers, DTOs, global exception handler
  out/
    persistence/  ← JPA entities, Spring Data repos, mappers, adapters
```

**Key design decisions:**
- `Calendar` is a domain-only concept — never exposed via the API. Users interact only with their slots and meetings.
- Domain objects are immutable Java records; JPA entities are separate classes in the persistence adapter.
- Port interfaces live in the domain — adapters implement/call them, keeping the dependency direction inward.
- Flyway manages DB schema; Hibernate validates only (no DDL auto-generation in prod).
- HikariCP connection pool tuned for concurrent access (20 max connections).
- Indexed columns: `calendar_id + start_time`, `status`, ensuring efficient range queries at scale.

## Running Locally

### Prerequisites
- Docker and Docker Compose

### Start everything

```bash
docker compose up --build
```

The app starts on **http://localhost:8080**.

### Swagger UI (API docs)
```
http://localhost:8080/swagger-ui.html
```

### Prometheus metrics
```
http://localhost:8080/actuator/prometheus
```

### Health check
```
http://localhost:8080/actuator/health
```

---

## API Reference

### Users

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/api/v1/users` | Create a user |
| `GET`  | `/api/v1/users/{userId}` | Get a user |

**Create user:**
```json
POST /api/v1/users
{
  "email": "alice@example.com",
  "name": "Alice"
}
```

---

### Time Slots

| Method | Path | Description |
|--------|------|-------------|
| `POST`  | `/api/v1/users/{userId}/slots` | Create a time slot |
| `GET`   | `/api/v1/users/{userId}/slots` | List slots (filterable) |
| `GET`   | `/api/v1/users/{userId}/slots/{slotId}` | Get a slot |
| `PUT`   | `/api/v1/users/{userId}/slots/{slotId}` | Update a slot |
| `DELETE`| `/api/v1/users/{userId}/slots/{slotId}` | Delete a slot |
| `PATCH` | `/api/v1/users/{userId}/slots/{slotId}/status` | Change FREE/BUSY status |

**Create time slot:**
```json
POST /api/v1/users/{userId}/slots
{
  "startTime": "2026-06-01T09:00:00Z",
  "endTime":   "2026-06-01T10:00:00Z"
}
```

**List slots (with filters):**
```
GET /api/v1/users/{userId}/slots?from=2026-06-01T00:00:00Z&to=2026-06-02T00:00:00Z&status=FREE&page=0&size=20
```

---

### Meetings

| Method | Path | Description |
|--------|------|-------------|
| `POST` | `/api/v1/users/{userId}/slots/{slotId}/meeting` | Schedule a meeting |
| `GET`  | `/api/v1/users/{userId}/meetings` | List meetings |
| `GET`  | `/api/v1/users/{userId}/meetings/{meetingId}` | Get a meeting |

**Schedule a meeting (converts FREE slot → BUSY):**
```json
POST /api/v1/users/{userId}/slots/{slotId}/meeting
{
  "title": "Team Standup",
  "description": "Daily 15-minute sync",
  "participantIds": ["<user-uuid-1>", "<user-uuid-2>"]
}
```

---

### Availability

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/api/v1/users/{userId}/availability` | Aggregated free/busy view |

```
GET /api/v1/users/{userId}/availability?from=2026-06-01T00:00:00Z&to=2026-06-07T00:00:00Z
```

**Response:**
```json
{
  "userId": "...",
  "from": "2026-06-01T00:00:00Z",
  "to":   "2026-06-07T00:00:00Z",
  "totalSlots": 10,
  "freeSlots": 7,
  "busySlots": 3,
  "slots": [
    { "id": "...", "startTime": "...", "endTime": "...", "status": "FREE", "meetingId": null },
    { "id": "...", "startTime": "...", "endTime": "...", "status": "BUSY", "meetingId": "..." }
  ]
}
```

---

## Running Tests

```bash
# Unit tests only (no DB required)
./mvnw test -Dtest="*ServiceTest"

# All tests including integration (requires Docker for Testcontainers)
./mvnw test
```

Integration tests use **Testcontainers** to spin up a real PostgreSQL instance automatically.

## Tech Stack

| Layer | Technology |
|-------|------------|
| Framework | Spring Boot 3.3, Java 21 |
| Persistence | PostgreSQL 16, Spring Data JPA, Flyway |
| API docs | SpringDoc OpenAPI 2 (Swagger UI) |
| Metrics | Micrometer + Prometheus |
| Testing | JUnit 5, Mockito, Testcontainers |
| Container | Docker Compose |

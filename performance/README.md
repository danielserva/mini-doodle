# Mini Doodle — Performance Tests

Gatling (Java DSL) load tests targeting a running instance of the service.

## Prerequisites

- Java 21
- Maven
- The service running on `http://localhost:8080` (default)

```bash
# From the project root
docker compose up --build -d
```

## Running

```bash
cd performance

# Run all simulations
mvn gatling:test

# Run a single simulation
mvn gatling:test -Dgatling.simulationClass=minidoodle.simulation.SchedulingSimulation

# Target a different host
mvn gatling:test -DbaseUrl=http://staging.example.com
```

HTML reports are written to `target/gatling/<simulation-name>-<timestamp>/index.html`.

## Simulations

### `SchedulingSimulation` — full user journey

Models the end-to-end scheduling flow for each virtual user:
1. `POST /api/v1/users` — create a unique user
2. `POST /api/v1/users/{id}/slots` × 5 — create five time slots
3. `POST /api/v1/users/{id}/slots/{slotId}/meeting` — schedule a meeting (marks slot BUSY)
4. `GET /api/v1/users/{id}/availability` — query the 48-hour availability window

**Load profile:** ramp from 0 to 50 users over 30 seconds  
**Assertions:** p99 latency < 2 s, success rate > 95%

---

### `AvailabilitySimulation` — read-heavy load

Exercises the date-range aggregation query, which is the most expensive read path.
Each virtual user creates their own user and 10 slots, then fires 30 availability
queries in a tight loop.

**Load profile:** ramp from 0 to 100 users over 30 seconds (≈ 3 000 availability reads total)  
**Assertions:** p95 latency on availability reads < 500 ms, success rate > 99%

---

### `ConcurrentBookingSimulation` — race condition

Verifies the slot-booking mutex under maximum concurrency. A single FREE slot is
created up front; then 50 virtual users all attempt to book it simultaneously.

**Expected outcome:** exactly one `201 Created`, the remaining 49 return `409 Conflict`  
**Load profile:** all 50 users injected at once (`atOnceUsers(50)`) after setup  
**Assertion:** 100% of requests return a valid HTTP status (no 500s)

> To confirm the 1-vs-49 split, check the `Book Slot` request breakdown in the HTML report.

## Structure

```
src/test/java/minidoodle/
├── Engine.java                          ← IDE launcher
├── scenario/
│   ├── UserScenario.java               ← create user chain
│   ├── SlotScenario.java               ← create slot chain (with/without id save)
│   └── MeetingScenario.java            ← schedule meeting chain
└── simulation/
    ├── SchedulingSimulation.java
    ├── AvailabilitySimulation.java
    └── ConcurrentBookingSimulation.java
```

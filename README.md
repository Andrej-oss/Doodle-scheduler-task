# Doodle Scheduler

Mini meeting scheduling platform — users create calendars, mark free time slots, and book them as meetings with participants.

## How to run

```bash
docker-compose up -d
```

That's it. Postgres, backend, frontend, Prometheus and Grafana all start together.

## Where to find things

| What | URL |
|---|---|
| Frontend | http://localhost:3000 |
| Backend API | http://localhost:8080 |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| Prometheus | http://localhost:9090 |
| Grafana | http://localhost:3001 (admin / admin) |

> Grafana: add Prometheus as a data source (`http://prometheus:9090`) and build dashboards from there.

## Stack

- Java 21, Spring Boot 3, WebFlux + R2DBC (non-blocking all the way)
- PostgreSQL 16 with Flyway migrations
- Next.js 15 frontend
- Prometheus + Grafana for metrics

Went with reactive stack because the task mentioned hundreds of users and thousands of slots — blocking thread-per-request wouldn't scale well here. DB indexes on `calendar_id + start_time + end_time` and a covering index make time-range queries fast.

## API quick reference

Full interactive docs at Swagger, but here's the gist:

**Users**
```
POST /api/v1/users          { "username": "alice", "email": "alice@example.com" }
GET  /api/v1/users/{id}
```

**Calendars**
```
POST /api/v1/calendars                    { "userId": "...", "name": "Work" }
GET  /api/v1/users/{userId}/calendars
```

**Time Slots**
```
POST   /api/v1/calendars/{calendarId}/slots    { "startTime": "2025-06-01T10:00", "endTime": "2025-06-01T11:00" }
PUT    /api/v1/slots/{slotId}                  { "status": "BUSY" }
DELETE /api/v1/slots/{slotId}
GET    /api/v1/calendars/{calendarId}/slots?status=FREE&from=2025-06-01T00:00&to=2025-06-30T00:00
GET    /api/v1/users/{userId}/availability?from=2025-06-01T00:00&to=2025-06-07T00:00
```

**Meetings**
```
POST /api/v1/meetings    { "slotId": "...", "organizerId": "...", "title": "Team Sync", "participantIds": ["..."] }
GET  /api/v1/meetings/{id}
GET  /api/v1/users/{userId}/meetings
```

## Getting started (UI)

Open http://localhost:3000. On the first visit you'll see a signup screen — type a username and email, hit **Get Started**. That's it, you're in.

To add another user open the app in a **private / incognito window** (each browser tab remembers its own user in `localStorage`). Fill in a different username and email and click **Get Started**.

## Typical flow

1. Create a user
2. Create a calendar for that user
3. Add free time slots to the calendar
4. Book a slot as a meeting — slot status automatically becomes `BUSY`
5. Trying to book the same slot twice returns `409 Conflict`

## Metrics

Custom counters exposed at `/actuator/prometheus`:
- `slots_created_total`
- `meetings_scheduled_total`

## Tests

Integration tests cover the main flows using Testcontainers (spins up a real Postgres):

```bash
cd backend && ./mvnw test
```

# Doodle Scheduler

Mini meeting scheduling platform. Users create calendars, define available time slots, and convert them into meetings with participants.

## Stack

- Java 21 + Spring Boot 3.4 + WebFlux + R2DBC
- PostgreSQL 16 + Flyway
- Docker Compose

## Run

```bash
docker-compose up -d
```

API: http://localhost:8080

## API

**Users**
- `POST /api/v1/users`
- `GET /api/v1/users/{userId}`

**Calendars**
- `POST /api/v1/calendars`
- `GET /api/v1/users/{userId}/calendars`

**Time Slots**
- `POST /api/v1/calendars/{calendarId}/slots`
- `PUT /api/v1/slots/{slotId}`
- `DELETE /api/v1/slots/{slotId}`
- `GET /api/v1/calendars/{calendarId}/slots?status=FREE&from=...&to=...`
- `GET /api/v1/users/{userId}/availability?from=...&to=...`

**Meetings**
- `POST /api/v1/meetings`
- `GET /api/v1/meetings/{meetingId}`
- `GET /api/v1/users/{userId}/meetings`

## Metrics

Prometheus endpoint: `GET /actuator/prometheus`

Custom metrics:
- `slots_created_total` — total slots created
- `meetings_scheduled_total` — total meetings scheduled

CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE users (
    id         UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    username   VARCHAR(100) NOT NULL UNIQUE,
    email      VARCHAR(255) NOT NULL UNIQUE,
    created_at TIMESTAMP   NOT NULL DEFAULT now()
);

CREATE TABLE calendars (
    id         UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id    UUID        NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name       VARCHAR(255) NOT NULL,
    created_at TIMESTAMP   NOT NULL DEFAULT now()
);

CREATE TABLE meetings (
    id           UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    title        VARCHAR(255) NOT NULL,
    description  TEXT,
    organizer_id UUID        NOT NULL REFERENCES users(id),
    slot_id      UUID        NOT NULL UNIQUE,
    created_at   TIMESTAMP   NOT NULL DEFAULT now()
);

CREATE TABLE time_slots (
    id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    calendar_id UUID        NOT NULL REFERENCES calendars(id) ON DELETE CASCADE,
    start_time  TIMESTAMP   NOT NULL,
    end_time    TIMESTAMP   NOT NULL,
    status      VARCHAR(10) NOT NULL DEFAULT 'FREE',
    meeting_id  UUID        REFERENCES meetings(id) ON DELETE SET NULL,
    created_at  TIMESTAMP   NOT NULL DEFAULT now(),
    CONSTRAINT chk_slot_time CHECK (end_time > start_time)
);

CREATE TABLE meeting_participants (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    meeting_id UUID NOT NULL REFERENCES meetings(id) ON DELETE CASCADE,
    user_id    UUID NOT NULL REFERENCES users(id),
    UNIQUE (meeting_id, user_id)
);

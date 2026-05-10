--liquibase formatted sql

--changeset mini-doodle:0001
CREATE TABLE users (
    id          UUID         PRIMARY KEY,
    email       VARCHAR(255) NOT NULL UNIQUE,
    name        VARCHAR(255) NOT NULL,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE TABLE calendars (
    id          UUID        PRIMARY KEY,
    user_id     UUID        NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE time_slots (
    id          UUID        PRIMARY KEY,
    calendar_id UUID        NOT NULL REFERENCES calendars(id) ON DELETE CASCADE,
    start_time  TIMESTAMPTZ NOT NULL,
    end_time    TIMESTAMPTZ NOT NULL,
    status      VARCHAR(10) NOT NULL DEFAULT 'FREE',
    meeting_id  UUID,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_time_slot_valid_range CHECK (end_time > start_time)
);

CREATE INDEX idx_time_slots_calendar_id    ON time_slots(calendar_id);
CREATE INDEX idx_time_slots_calendar_start ON time_slots(calendar_id, start_time);
CREATE INDEX idx_time_slots_status         ON time_slots(status);
CREATE INDEX idx_time_slots_start_end      ON time_slots(start_time, end_time);

CREATE TABLE meetings (
    id           UUID         PRIMARY KEY,
    time_slot_id UUID         NOT NULL UNIQUE REFERENCES time_slots(id) ON DELETE CASCADE,
    title        VARCHAR(255) NOT NULL,
    description  TEXT,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_meetings_time_slot_id ON meetings(time_slot_id);

CREATE TABLE meeting_participants (
    meeting_id UUID NOT NULL REFERENCES meetings(id) ON DELETE CASCADE,
    user_id    UUID NOT NULL REFERENCES users(id)    ON DELETE CASCADE,
    PRIMARY KEY (meeting_id, user_id)
);

CREATE INDEX idx_meeting_participants_user_id ON meeting_participants(user_id);
--rollback DROP TABLE meeting_participants; DROP TABLE meetings; DROP TABLE time_slots; DROP TABLE calendars; DROP TABLE users;

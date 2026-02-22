-- Time-range queries (most frequent)
CREATE INDEX idx_timeslot_calendar_time
    ON time_slots(calendar_id, start_time, end_time);

-- Status filter queries
CREATE INDEX idx_timeslot_status
    ON time_slots(status, calendar_id);

-- Covering index for availability aggregation
CREATE INDEX idx_timeslot_covering
    ON time_slots(calendar_id) INCLUDE (start_time, end_time, status);

-- Meetings by organizer
CREATE INDEX idx_meeting_organizer
    ON meetings(organizer_id);

-- Participants by user
CREATE INDEX idx_participant_user
    ON meeting_participants(user_id);

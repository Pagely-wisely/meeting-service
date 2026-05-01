-- =========================
-- TABLE: p_processed_event
-- =========================
CREATE TABLE p_processed_event (
    id UUID PRIMARY KEY,
    consumer_name VARCHAR(100) NOT NULL,
    event_id VARCHAR(100) NOT NULL,
    processed_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- =========================
-- UNIQUE CONSTRAINT
-- =========================
ALTER TABLE p_processed_event
    ADD CONSTRAINT uk_p_processed_event_consumer_event UNIQUE (consumer_name, event_id);

-- =========================
-- INDEX
-- =========================
CREATE INDEX idx_p_processed_event_consumer_name ON p_processed_event(consumer_name);
CREATE INDEX idx_p_processed_event_event_id ON p_processed_event(event_id);
CREATE INDEX idx_p_processed_event_processed_at ON p_processed_event(processed_at);

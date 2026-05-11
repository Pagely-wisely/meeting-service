CREATE TABLE p_outbox (
                          id UUID PRIMARY KEY,
                          aggregate_type VARCHAR(50) NOT NULL,
                          aggregate_id UUID NOT NULL,
                          event_type VARCHAR(100) NOT NULL,
                          topic VARCHAR(100) NOT NULL,
                          payload JSONB NOT NULL,

                          published BOOLEAN NOT NULL DEFAULT FALSE,
                          published_at TIMESTAMP,

                          publishing BOOLEAN NOT NULL DEFAULT FALSE,
                          publishing_started_at TIMESTAMP,

                          failure_count INT NOT NULL DEFAULT 0,
                          last_failure_at TIMESTAMP,
                          last_failure_message TEXT,
                          created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_p_outbox_unpublished
    ON p_outbox (created_at)
    WHERE published = FALSE
      AND publishing = FALSE;

CREATE INDEX idx_p_outbox_aggregate
    ON p_outbox (aggregate_type, aggregate_id);

CREATE INDEX idx_p_outbox_failure_retry
    ON p_outbox (failure_count, last_failure_at)
    WHERE published = FALSE;

CREATE INDEX idx_p_outbox_publishing
    ON p_outbox (publishing, publishing_started_at)
    WHERE published = FALSE;

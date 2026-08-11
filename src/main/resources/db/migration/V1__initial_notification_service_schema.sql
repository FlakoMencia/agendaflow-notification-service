CREATE SCHEMA IF NOT EXISTS notification_service;
SET search_path TO notification_service, public;

CREATE TABLE notification_inbox (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    event_id BIGINT NOT NULL,
    organization_id BIGINT NOT NULL,
    appointment_id BIGINT NOT NULL,
    event_type VARCHAR(40) NOT NULL,
    channel VARCHAR(20) NOT NULL,
    recipient VARCHAR(254) NOT NULL,
    locale VARCHAR(35) NOT NULL,
    correlation_id VARCHAR(100),
    payload JSONB NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    attempt_count INTEGER NOT NULL DEFAULT 0,
    next_attempt_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    processing_started_at TIMESTAMPTZ,
    processed_at TIMESTAMPTZ,
    last_error VARCHAR(1000),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_notification_inbox_event_id UNIQUE (event_id),
    CONSTRAINT chk_notification_inbox_status
        CHECK (status IN ('PENDING', 'PROCESSING', 'PROCESSED', 'EXHAUSTED')),
    CONSTRAINT chk_notification_inbox_attempt_count CHECK (attempt_count >= 0),
    CONSTRAINT chk_notification_inbox_channel CHECK (channel = 'EMAIL')
);

CREATE INDEX idx_notification_inbox_claim
    ON notification_inbox (status, next_attempt_at);
CREATE INDEX idx_notification_inbox_created_at
    ON notification_inbox (created_at);

CREATE TABLE notification_deliveries (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    inbox_id BIGINT NOT NULL,
    channel VARCHAR(20) NOT NULL,
    template_code VARCHAR(80) NOT NULL,
    attempt INTEGER NOT NULL,
    provider_type VARCHAR(40) NOT NULL,
    status VARCHAR(20) NOT NULL,
    started_at TIMESTAMPTZ NOT NULL,
    finished_at TIMESTAMPTZ,
    error_code VARCHAR(80),
    error_message VARCHAR(500),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_notification_delivery_inbox
        FOREIGN KEY (inbox_id) REFERENCES notification_inbox (id),
    CONSTRAINT uq_notification_delivery_attempt UNIQUE (inbox_id, attempt),
    CONSTRAINT chk_notification_delivery_status
        CHECK (status IN ('PROCESSING', 'DISPATCHED', 'FAILED')),
    CONSTRAINT chk_notification_delivery_attempt CHECK (attempt > 0)
);

CREATE INDEX idx_notification_deliveries_inbox
    ON notification_deliveries (inbox_id, created_at);

package com.flakomencia.agendaflow.notification.infrastructure.persistence;

import java.time.OffsetDateTime;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "notification_inbox", schema = "notification_service")
public class NotificationInboxEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name = "event_id", nullable = false, updatable = false) private Long eventId;
    @Column(name = "organization_id", nullable = false, updatable = false) private Long organizationId;
    @Column(name = "appointment_id", nullable = false, updatable = false) private Long appointmentId;
    @Column(name = "event_type", nullable = false, updatable = false, length = 40) private String eventType;
    @Column(nullable = false, updatable = false, length = 20) private String channel;
    @Column(nullable = false, updatable = false, length = 254) private String recipient;
    @Column(nullable = false, updatable = false, length = 35) private String locale;
    @Column(name = "correlation_id", updatable = false, length = 100) private String correlationId;
    @JdbcTypeCode(SqlTypes.JSON) @Column(nullable = false, updatable = false, columnDefinition = "jsonb") private String payload;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private InboxStatus status;
    @Column(name = "attempt_count", nullable = false) private int attemptCount;
    @Column(name = "next_attempt_at", nullable = false) private OffsetDateTime nextAttemptAt;
    @Column(name = "processing_started_at") private OffsetDateTime processingStartedAt;
    @Column(name = "processed_at") private OffsetDateTime processedAt;
    @Column(name = "last_error", length = 1000) private String lastError;
    @Column(name = "created_at", nullable = false, insertable = false, updatable = false) private OffsetDateTime createdAt;
    @Column(name = "updated_at", nullable = false) private OffsetDateTime updatedAt;

    protected NotificationInboxEntity() {}
    public Long getId() { return id; }
    public Long getEventId() { return eventId; }
    public Long getOrganizationId() { return organizationId; }
    public Long getAppointmentId() { return appointmentId; }
    public String getEventType() { return eventType; }
    public String getChannel() { return channel; }
    public String getRecipient() { return recipient; }
    public String getLocale() { return locale; }
    public String getCorrelationId() { return correlationId; }
    public String getPayload() { return payload; }
    public InboxStatus getStatus() { return status; }
    public int getAttemptCount() { return attemptCount; }
    public OffsetDateTime getNextAttemptAt() { return nextAttemptAt; }
    public OffsetDateTime getProcessingStartedAt() { return processingStartedAt; }
    public OffsetDateTime getProcessedAt() { return processedAt; }
    public String getLastError() { return lastError; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }

    public void claim(OffsetDateTime now) {
        status = InboxStatus.PROCESSING; attemptCount++; processingStartedAt = now; lastError = null; updatedAt = now;
    }
    public void processed(OffsetDateTime now) {
        status = InboxStatus.PROCESSED; processedAt = now; processingStartedAt = null; lastError = null; updatedAt = now;
    }
    public void failed(OffsetDateTime now, OffsetDateTime retryAt, String error, int maxAttempts) {
        status = attemptCount >= maxAttempts ? InboxStatus.EXHAUSTED : InboxStatus.PENDING;
        nextAttemptAt = retryAt; processingStartedAt = null; lastError = error; updatedAt = now;
    }
    public void recover(OffsetDateTime now) {
        status = InboxStatus.PENDING; nextAttemptAt = now; processingStartedAt = null;
        lastError = "Recovered stale processing claim"; updatedAt = now;
    }
}

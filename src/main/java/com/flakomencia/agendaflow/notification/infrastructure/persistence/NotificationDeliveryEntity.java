package com.flakomencia.agendaflow.notification.infrastructure.persistence;

import java.time.OffsetDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "notification_deliveries", schema = "notification_service")
public class NotificationDeliveryEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "inbox_id", nullable = false, updatable = false)
    private NotificationInboxEntity inbox;
    @Column(nullable = false, updatable = false, length = 20) private String channel;
    @Column(name = "template_code", nullable = false, updatable = false, length = 80) private String templateCode;
    @Column(nullable = false, updatable = false) private int attempt;
    @Column(name = "provider_type", nullable = false, updatable = false, length = 40) private String providerType;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) private DeliveryStatus status;
    @Column(name = "started_at", nullable = false, updatable = false) private OffsetDateTime startedAt;
    @Column(name = "finished_at") private OffsetDateTime finishedAt;
    @Column(name = "error_code", length = 80) private String errorCode;
    @Column(name = "error_message", length = 500) private String errorMessage;
    @Column(name = "created_at", nullable = false, insertable = false, updatable = false) private OffsetDateTime createdAt;

    protected NotificationDeliveryEntity() {}
    public NotificationDeliveryEntity(NotificationInboxEntity inbox, String channel, String templateCode,
            int attempt, OffsetDateTime startedAt) {
        this.inbox = inbox; this.channel = channel; this.templateCode = templateCode; this.attempt = attempt;
        this.providerType = "QUARKUS_MAILER"; this.status = DeliveryStatus.PROCESSING; this.startedAt = startedAt;
    }
    public Long getId() { return id; }
    public DeliveryStatus getStatus() { return status; }
    public String getTemplateCode() { return templateCode; }
    public int getAttempt() { return attempt; }
    public void dispatched(OffsetDateTime now) { status = DeliveryStatus.DISPATCHED; finishedAt = now; }
    public void failed(OffsetDateTime now, String code, String message) {
        status = DeliveryStatus.FAILED; finishedAt = now; errorCode = code; errorMessage = message;
    }
}

package com.flakomencia.agendaflow.notification.application;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class DurableNotificationIntakeService {
    private final EntityManager entityManager;
    private final ObjectMapper objectMapper;

    @Inject
    public DurableNotificationIntakeService(EntityManager entityManager, ObjectMapper objectMapper) {
        this.entityManager = entityManager;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public NotificationIntakeResult accept(AppointmentNotificationMessage message, String correlationId) {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        @SuppressWarnings("unchecked")
        List<Number> inserted = entityManager.createNativeQuery("""
                INSERT INTO notification_service.notification_inbox
                    (event_id, organization_id, appointment_id, event_type, channel, recipient, locale,
                     correlation_id, payload, status, attempt_count, next_attempt_at, created_at, updated_at)
                VALUES
                    (:eventId, :organizationId, :appointmentId, :eventType, 'EMAIL', :recipient, :locale,
                     :correlationId, CAST(:payload AS jsonb), 'PENDING', 0, :now, :now, :now)
                ON CONFLICT (event_id) DO NOTHING
                RETURNING id
                """)
                .setParameter("eventId", message.eventId())
                .setParameter("organizationId", message.organizationId())
                .setParameter("appointmentId", message.appointmentId())
                .setParameter("eventType", message.type().name())
                .setParameter("recipient", message.recipient().trim().toLowerCase(java.util.Locale.ROOT))
                .setParameter("locale", message.locale())
                .setParameter("correlationId", safeCorrelationId(correlationId))
                .setParameter("payload", serialize(message))
                .setParameter("now", now)
                .getResultList();
        entityManager.flush();
        return new NotificationIntakeResult(message.eventId(), inserted.isEmpty());
    }

    private String serialize(AppointmentNotificationMessage message) {
        try {
            return objectMapper.writeValueAsString(message);
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("notification request cannot be serialized", exception);
        }
    }

    private String safeCorrelationId(String value) {
        if (value == null || value.isBlank()) return null;
        String normalized = value.replaceAll("[\\r\\n]", "");
        return normalized.length() <= 100 ? normalized : normalized.substring(0, 100);
    }
}

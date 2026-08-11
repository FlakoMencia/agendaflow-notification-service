package com.flakomencia.agendaflow.notification.domain;

import java.time.Instant;
import java.util.Objects;

public record AppointmentNotificationEvent(
        Long organizationId,
        Long appointmentId,
        AppointmentNotificationEventType type,
        String recipient,
        String locale,
        Instant occurredAt,
        NotificationVariables variables) {

    public AppointmentNotificationEvent {
        if (organizationId == null || organizationId <= 0) {
            throw new IllegalArgumentException("organizationId must be positive");
        }
        if (appointmentId == null || appointmentId <= 0) {
            throw new IllegalArgumentException("appointmentId must be positive");
        }
        Objects.requireNonNull(type, "type must not be null");
        if (recipient == null || recipient.isBlank()) {
            throw new IllegalArgumentException("recipient must not be blank");
        }
        Objects.requireNonNull(occurredAt, "occurredAt must not be null");
        variables = variables == null ? NotificationVariables.empty() : variables;
    }
}

package com.flakomencia.agendaflow.notification.domain;

import java.time.Instant;
import java.util.Objects;

public record NotificationRequest(
        Long organizationId,
        Long appointmentId,
        NotificationChannel channel,
        NotificationTemplateCode templateCode,
        NotificationRecipient recipient,
        String locale,
        Instant scheduledAt,
        NotificationVariables variables) {

    public NotificationRequest {
        if (organizationId == null || organizationId <= 0) {
            throw new IllegalArgumentException("organizationId must be positive");
        }
        if (appointmentId != null && appointmentId <= 0) {
            throw new IllegalArgumentException("appointmentId must be positive when present");
        }
        Objects.requireNonNull(channel, "channel must not be null");
        Objects.requireNonNull(templateCode, "templateCode must not be null");
        Objects.requireNonNull(recipient, "recipient must not be null");
        variables = variables == null ? NotificationVariables.empty() : variables;
    }
}

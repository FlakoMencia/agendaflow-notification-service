package com.flakomencia.agendaflow.notification.domain;

import java.util.Objects;
import java.util.Set;

public record NotificationTemplate(
        AppointmentNotificationTemplateCode code,
        NotificationChannel channel,
        String subject,
        String plainTextBody,
        Set<String> requiredVariables) {

    public NotificationTemplate {
        Objects.requireNonNull(code, "code must not be null");
        Objects.requireNonNull(channel, "channel must not be null");
        if (subject == null || subject.isBlank()) {
            throw new IllegalArgumentException("subject must not be blank");
        }
        if (plainTextBody == null || plainTextBody.isBlank()) {
            throw new IllegalArgumentException("plainTextBody must not be blank");
        }
        requiredVariables = Set.copyOf(Objects.requireNonNull(
                requiredVariables,
                "requiredVariables must not be null"));
    }
}

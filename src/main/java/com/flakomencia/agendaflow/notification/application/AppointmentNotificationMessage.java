package com.flakomencia.agendaflow.notification.application;

import java.time.Instant;
import java.util.Map;

import com.flakomencia.agendaflow.notification.domain.AppointmentNotificationEvent;
import com.flakomencia.agendaflow.notification.domain.AppointmentNotificationEventType;
import com.flakomencia.agendaflow.notification.domain.NotificationVariables;

public record AppointmentNotificationMessage(
        Long eventId,
        Long organizationId,
        Long appointmentId,
        AppointmentNotificationEventType type,
        String recipient,
        String locale,
        Instant occurredAt,
        Map<String, String> variables) {

    public AppointmentNotificationEvent toDomainEvent() {
        return new AppointmentNotificationEvent(organizationId, appointmentId, type, recipient, locale, occurredAt,
                new NotificationVariables(variables));
    }
}

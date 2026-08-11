package com.flakomencia.agendaflow.notification.api.model;

public record NotificationIntakeResponse(Long eventId, String status, boolean duplicate) {
    public static NotificationIntakeResponse accepted(Long eventId, boolean duplicate) {
        return new NotificationIntakeResponse(eventId, "ACCEPTED", duplicate);
    }
}

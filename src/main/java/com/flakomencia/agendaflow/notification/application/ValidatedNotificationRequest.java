package com.flakomencia.agendaflow.notification.application;

import java.util.Objects;

import com.flakomencia.agendaflow.notification.domain.NotificationRequest;

public record ValidatedNotificationRequest(NotificationRequest request) {

    public ValidatedNotificationRequest {
        Objects.requireNonNull(request, "request must not be null");
    }
}

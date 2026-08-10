package com.flakomencia.agendaflow.notification.domain;

import java.util.Objects;

public record NotificationRecipient(String value) {

    public NotificationRecipient {
        Objects.requireNonNull(value, "value must not be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException("value must not be blank");
        }
    }
}

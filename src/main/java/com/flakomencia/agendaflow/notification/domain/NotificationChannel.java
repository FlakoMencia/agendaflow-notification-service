package com.flakomencia.agendaflow.notification.domain;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "Supported notification channel. Phase 2 accepts EMAIL only.")
public enum NotificationChannel {
    EMAIL
}

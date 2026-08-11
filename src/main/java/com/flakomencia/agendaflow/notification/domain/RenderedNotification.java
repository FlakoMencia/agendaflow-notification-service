package com.flakomencia.agendaflow.notification.domain;

import java.time.Instant;
import java.util.Objects;

public record RenderedNotification(
        Long organizationId,
        Long appointmentId,
        NotificationChannel channel,
        NotificationTemplateCode templateCode,
        NotificationRecipient recipient,
        String locale,
        Instant scheduledAt,
        String subject,
        String content) {

    public RenderedNotification {
        if (organizationId == null || organizationId <= 0) {
            throw new IllegalArgumentException("organizationId must be positive");
        }
        Objects.requireNonNull(channel, "channel must not be null");
        Objects.requireNonNull(templateCode, "templateCode must not be null");
        Objects.requireNonNull(recipient, "recipient must not be null");
        Objects.requireNonNull(content, "content must not be null");
        if (content.length() > NotificationLimits.MAX_RENDERED_CONTENT_LENGTH) {
            throw new IllegalArgumentException("content exceeds the rendered notification limit");
        }
    }

    public RenderedNotification(
            Long organizationId,
            Long appointmentId,
            NotificationChannel channel,
            NotificationTemplateCode templateCode,
            NotificationRecipient recipient,
            String locale,
            Instant scheduledAt,
            String content) {
        this(organizationId, appointmentId, channel, templateCode, recipient, locale, scheduledAt, null, content);
    }
}

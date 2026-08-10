package com.flakomencia.agendaflow.notification.application;

import com.flakomencia.agendaflow.notification.application.rendering.NotificationTemplateRenderer;
import com.flakomencia.agendaflow.notification.domain.NotificationRequest;
import com.flakomencia.agendaflow.notification.domain.RenderedNotification;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class NotificationPreparationService {

    private final NotificationTemplateRenderer renderer;

    @Inject
    public NotificationPreparationService(NotificationTemplateRenderer renderer) {
        this.renderer = renderer;
    }

    public RenderedNotification prepare(
            ValidatedNotificationRequest validatedRequest,
            String templateText) {
        NotificationRequest request = validatedRequest.request();
        String content = renderer.render(templateText, request.variables());

        return new RenderedNotification(
                request.organizationId(),
                request.appointmentId(),
                request.channel(),
                request.templateCode(),
                request.recipient(),
                request.locale(),
                request.scheduledAt(),
                content);
    }
}

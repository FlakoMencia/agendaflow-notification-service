package com.flakomencia.agendaflow.notification.application;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.flakomencia.agendaflow.notification.application.rendering.PlainTextTemplateRenderer;
import com.flakomencia.agendaflow.notification.domain.NotificationChannel;
import com.flakomencia.agendaflow.notification.domain.NotificationRecipient;
import com.flakomencia.agendaflow.notification.domain.NotificationRequest;
import com.flakomencia.agendaflow.notification.domain.NotificationTemplateCode;
import com.flakomencia.agendaflow.notification.domain.NotificationVariables;
import com.flakomencia.agendaflow.notification.domain.RenderedNotification;

class NotificationPreparationServiceTest {

    private final NotificationPreparationService service = new NotificationPreparationService(
            new PlainTextTemplateRenderer());

    @Test
    void preparesRenderedContentWithoutDeliveringOrPersisting() {
        NotificationRequest request = new NotificationRequest(
                10L,
                25L,
                NotificationChannel.EMAIL,
                new NotificationTemplateCode("SYNTHETIC_NOTICE"),
                new NotificationRecipient("recipient@example.com"),
                "en-US",
                null,
                new NotificationVariables(Map.of("name", "Laura", "time", "10:00 AM")));

        RenderedNotification rendered = service.prepare(
                new ValidatedNotificationRequest(request),
                "Hello {{name}}, scheduled at {{time}}.");

        assertEquals(10L, rendered.organizationId());
        assertEquals(25L, rendered.appointmentId());
        assertEquals(NotificationChannel.EMAIL, rendered.channel());
        assertEquals("SYNTHETIC_NOTICE", rendered.templateCode().value());
        assertEquals("recipient@example.com", rendered.recipient().value());
        assertEquals("Hello Laura, scheduled at 10:00 AM.", rendered.content());
    }
}

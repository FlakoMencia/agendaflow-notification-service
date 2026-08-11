package com.flakomencia.agendaflow.notification.application;

import static com.flakomencia.agendaflow.notification.domain.AppointmentNotificationEventType.CANCELLED;
import static com.flakomencia.agendaflow.notification.domain.AppointmentNotificationEventType.CREATED;
import static com.flakomencia.agendaflow.notification.domain.AppointmentNotificationEventType.RESCHEDULED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.Arrays;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.flakomencia.agendaflow.notification.application.port.NotificationDeliveryPort;
import com.flakomencia.agendaflow.notification.application.rendering.PlainTextTemplateRenderer;
import com.flakomencia.agendaflow.notification.application.template.AppointmentNotificationTemplateMapper;
import com.flakomencia.agendaflow.notification.application.template.TemplateVariablesValidator;
import com.flakomencia.agendaflow.notification.domain.AppointmentNotificationEvent;
import com.flakomencia.agendaflow.notification.domain.AppointmentNotificationEventType;
import com.flakomencia.agendaflow.notification.domain.NotificationVariables;
import com.flakomencia.agendaflow.notification.domain.RenderedNotification;
import com.flakomencia.agendaflow.notification.infrastructure.template.BuiltInNotificationTemplateCatalog;

class AppointmentNotificationPreparationServiceTest {

    private final AppointmentNotificationPreparationService service = new AppointmentNotificationPreparationService(
            new BuiltInNotificationTemplateCatalog(),
            new AppointmentNotificationTemplateMapper(),
            new TemplateVariablesValidator(),
            new PlainTextTemplateRenderer(),
            new NotificationRecipientNormalizer());

    @Test
    void rendersConfirmation() {
        RenderedNotification rendered = service.prepare(event(CREATED, "en-US", "customer@example.com"));

        assertEquals("APPOINTMENT_CONFIRMATION", rendered.templateCode().value());
        assertEquals("Appointment confirmed: Consultation", rendered.subject());
        assertTrue(rendered.content().contains("is scheduled for August 10 at 10:00 AM"));
    }

    @Test
    void rendersRescheduledNotification() {
        RenderedNotification rendered = service.prepare(event(RESCHEDULED, "en-US", "customer@example.com"));

        assertEquals("APPOINTMENT_RESCHEDULED", rendered.templateCode().value());
        assertTrue(rendered.content().contains("has been rescheduled to August 10 at 10:00 AM"));
    }

    @Test
    void rendersCancelledNotification() {
        RenderedNotification rendered = service.prepare(event(CANCELLED, "en-US", "customer@example.com"));

        assertEquals("APPOINTMENT_CANCELLED", rendered.templateCode().value());
        assertTrue(rendered.content().contains("has been cancelled"));
        assertFalse(rendered.content().contains("Example Specialist"));
    }

    @Test
    void normalizesRecipientWithoutLoggingOrSendingIt() {
        RenderedNotification rendered = service.prepare(event(CREATED, "en-US", "  Customer@EXAMPLE.COM  "));

        assertEquals("Customer@example.com", rendered.recipient().value());
    }

    @Test
    void fallsBackToBaseLocale() {
        RenderedNotification rendered = service.prepare(event(CREATED, "es-SV", "customer@example.com"));

        assertEquals("en-US", rendered.locale());
    }

    @Test
    void hasNoDeliveryPortCollaborator() {
        boolean hasDeliveryPort = Arrays.stream(AppointmentNotificationPreparationService.class.getDeclaredFields())
                .map(Field::getType)
                .anyMatch(NotificationDeliveryPort.class::isAssignableFrom);

        assertFalse(hasDeliveryPort);
    }

    private AppointmentNotificationEvent event(
            AppointmentNotificationEventType type,
            String locale,
            String recipient) {
        return new AppointmentNotificationEvent(
                10L,
                25L,
                type,
                recipient,
                locale,
                Instant.parse("2026-08-10T15:00:00Z"),
                new NotificationVariables(Map.of(
                        "customerName", "Example Customer",
                        "serviceName", "Consultation",
                        "specialistName", "Example Specialist",
                        "appointmentDateTime", "August 10 at 10:00 AM",
                        "branchName", "Main Branch",
                        "extraContext", "Ignored safely")));
    }
}

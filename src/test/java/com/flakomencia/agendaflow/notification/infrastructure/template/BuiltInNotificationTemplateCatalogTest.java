package com.flakomencia.agendaflow.notification.infrastructure.template;

import static com.flakomencia.agendaflow.notification.domain.AppointmentNotificationTemplateCode.APPOINTMENT_CANCELLED;
import static com.flakomencia.agendaflow.notification.domain.AppointmentNotificationTemplateCode.APPOINTMENT_CONFIRMATION;
import static com.flakomencia.agendaflow.notification.domain.AppointmentNotificationTemplateCode.APPOINTMENT_RESCHEDULED;
import static com.flakomencia.agendaflow.notification.domain.NotificationChannel.EMAIL;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import com.flakomencia.agendaflow.notification.domain.AppointmentNotificationTemplateCode;
import com.flakomencia.agendaflow.notification.domain.UnknownNotificationTemplateException;

class BuiltInNotificationTemplateCatalogTest {

    private final BuiltInNotificationTemplateCatalog catalog = new BuiltInNotificationTemplateCatalog();

    @Test
    void containsConfirmationTemplate() {
        assertEquals(APPOINTMENT_CONFIRMATION, catalog.get(APPOINTMENT_CONFIRMATION, EMAIL, "en-US").code());
    }

    @Test
    void containsRescheduledTemplate() {
        assertEquals(APPOINTMENT_RESCHEDULED, catalog.get(APPOINTMENT_RESCHEDULED, EMAIL, "en-US").code());
    }

    @Test
    void containsCancelledTemplate() {
        assertEquals(APPOINTMENT_CANCELLED, catalog.get(APPOINTMENT_CANCELLED, EMAIL, "en-US").code());
    }

    @Test
    void rejectsUnknownAppointmentTemplateCode() {
        assertThrows(
                UnknownNotificationTemplateException.class,
                () -> AppointmentNotificationTemplateCode.from("APPOINTMENT_REMINDER"));
    }

    @Test
    void rejectsAnUnsupportedChannel() {
        assertThrows(
                NotificationTemplateNotFoundException.class,
                () -> catalog.get(APPOINTMENT_CONFIRMATION, null, "en-US"));
    }

    @Test
    void fallsBackToTheBaseLocale() {
        assertEquals("en-US", catalog.resolveLocale("es-SV"));
        assertEquals(APPOINTMENT_CONFIRMATION, catalog.get(APPOINTMENT_CONFIRMATION, EMAIL, "es-SV").code());
    }
}

package com.flakomencia.agendaflow.notification.application.template;

import static com.flakomencia.agendaflow.notification.domain.AppointmentNotificationEventType.CANCELLED;
import static com.flakomencia.agendaflow.notification.domain.AppointmentNotificationEventType.CREATED;
import static com.flakomencia.agendaflow.notification.domain.AppointmentNotificationEventType.RESCHEDULED;
import static com.flakomencia.agendaflow.notification.domain.AppointmentNotificationTemplateCode.APPOINTMENT_CANCELLED;
import static com.flakomencia.agendaflow.notification.domain.AppointmentNotificationTemplateCode.APPOINTMENT_CONFIRMATION;
import static com.flakomencia.agendaflow.notification.domain.AppointmentNotificationTemplateCode.APPOINTMENT_RESCHEDULED;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class AppointmentNotificationTemplateMapperTest {

    private final AppointmentNotificationTemplateMapper mapper = new AppointmentNotificationTemplateMapper();

    @Test
    void mapsCreatedExplicitly() {
        assertEquals(APPOINTMENT_CONFIRMATION, mapper.toTemplateCode(CREATED));
    }

    @Test
    void mapsRescheduledExplicitly() {
        assertEquals(APPOINTMENT_RESCHEDULED, mapper.toTemplateCode(RESCHEDULED));
    }

    @Test
    void mapsCancelledExplicitly() {
        assertEquals(APPOINTMENT_CANCELLED, mapper.toTemplateCode(CANCELLED));
    }
}

package com.flakomencia.agendaflow.notification.application.template;

import com.flakomencia.agendaflow.notification.domain.AppointmentNotificationEventType;
import com.flakomencia.agendaflow.notification.domain.AppointmentNotificationTemplateCode;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class AppointmentNotificationTemplateMapper {

    public AppointmentNotificationTemplateCode toTemplateCode(AppointmentNotificationEventType eventType) {
        return switch (eventType) {
            case CREATED -> AppointmentNotificationTemplateCode.APPOINTMENT_CONFIRMATION;
            case RESCHEDULED -> AppointmentNotificationTemplateCode.APPOINTMENT_RESCHEDULED;
            case CANCELLED -> AppointmentNotificationTemplateCode.APPOINTMENT_CANCELLED;
        };
    }
}

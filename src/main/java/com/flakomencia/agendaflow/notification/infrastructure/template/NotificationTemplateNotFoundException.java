package com.flakomencia.agendaflow.notification.infrastructure.template;

import com.flakomencia.agendaflow.notification.domain.AppointmentNotificationTemplateCode;
import com.flakomencia.agendaflow.notification.domain.NotificationChannel;

public final class NotificationTemplateNotFoundException extends IllegalArgumentException {

    public NotificationTemplateNotFoundException(
            AppointmentNotificationTemplateCode code,
            NotificationChannel channel) {
        super("No built-in template for code=" + code + " and channel=" + channel);
    }
}

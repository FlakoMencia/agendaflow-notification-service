package com.flakomencia.agendaflow.notification.application.port;

import com.flakomencia.agendaflow.notification.domain.AppointmentNotificationTemplateCode;
import com.flakomencia.agendaflow.notification.domain.NotificationChannel;
import com.flakomencia.agendaflow.notification.domain.NotificationTemplate;

public interface NotificationTemplateCatalog {

    NotificationTemplate get(
            AppointmentNotificationTemplateCode code,
            NotificationChannel channel,
            String requestedLocale);

    String resolveLocale(String requestedLocale);
}

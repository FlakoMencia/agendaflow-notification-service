package com.flakomencia.agendaflow.notification.application.port;

import com.flakomencia.agendaflow.notification.domain.AppointmentNotificationEvent;
import com.flakomencia.agendaflow.notification.domain.RenderedNotification;

/** Internal entry boundary; no durable transport adapter is connected in this phase. */
public interface AppointmentNotificationIntake {

    RenderedNotification prepare(AppointmentNotificationEvent event);
}

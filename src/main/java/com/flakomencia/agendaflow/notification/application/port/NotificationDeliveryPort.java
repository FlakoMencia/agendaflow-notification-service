package com.flakomencia.agendaflow.notification.application.port;

import com.flakomencia.agendaflow.notification.domain.RenderedNotification;

public interface NotificationDeliveryPort {

    void deliver(RenderedNotification notification);
}

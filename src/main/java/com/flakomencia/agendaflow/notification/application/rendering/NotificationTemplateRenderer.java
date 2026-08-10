package com.flakomencia.agendaflow.notification.application.rendering;

import com.flakomencia.agendaflow.notification.domain.NotificationVariables;

public interface NotificationTemplateRenderer {

    String render(String templateText, NotificationVariables variables);
}

package com.flakomencia.agendaflow.notification.domain;

public final class UnknownNotificationTemplateException extends IllegalArgumentException {

    public UnknownNotificationTemplateException(String code) {
        super("Unknown appointment notification template code: " + code);
    }
}

package com.flakomencia.agendaflow.notification.domain;

public final class NotificationLimits {

    public static final int MAX_VARIABLES = 20;
    public static final int MAX_VARIABLE_KEY_LENGTH = 64;
    public static final int MAX_VARIABLE_VALUE_LENGTH = 500;
    public static final int MAX_TEMPLATE_LENGTH = 10_000;
    public static final int MAX_RENDERED_CONTENT_LENGTH = 20_000;

    private NotificationLimits() {
    }
}

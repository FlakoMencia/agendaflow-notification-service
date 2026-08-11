package com.flakomencia.agendaflow.notification.domain;

import java.util.Arrays;

public enum AppointmentNotificationTemplateCode {
    APPOINTMENT_CONFIRMATION,
    APPOINTMENT_RESCHEDULED,
    APPOINTMENT_CANCELLED;

    public NotificationTemplateCode toTemplateCode() {
        return new NotificationTemplateCode(name());
    }

    public static AppointmentNotificationTemplateCode from(String value) {
        return Arrays.stream(values())
                .filter(candidate -> candidate.name().equals(value))
                .findFirst()
                .orElseThrow(() -> new UnknownNotificationTemplateException(value));
    }
}

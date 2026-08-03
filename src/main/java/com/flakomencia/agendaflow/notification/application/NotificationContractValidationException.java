package com.flakomencia.agendaflow.notification.application;

public class NotificationContractValidationException extends RuntimeException {

    private final String code;
    private final String field;

    public NotificationContractValidationException(String code, String message, String field) {
        super(message);
        this.code = code;
        this.field = field;
    }

    public String code() {
        return code;
    }

    public String field() {
        return field;
    }
}

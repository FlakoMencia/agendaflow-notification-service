package com.flakomencia.agendaflow.notification.infrastructure.http;

import jakarta.enterprise.context.RequestScoped;

@RequestScoped
public class CorrelationIdContext {

    private String correlationId;

    public String get() {
        return correlationId;
    }

    void set(String correlationId) {
        this.correlationId = correlationId;
    }

    void clear() {
        correlationId = null;
    }
}

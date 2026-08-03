package com.flakomencia.agendaflow.notification.api.model;

import java.time.Instant;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(name = "ApiError", description = "Uniform technical error response")
public record ApiErrorResponse(
        Instant timestamp,
        int status,
        String code,
        String message,
        String path,
        String correlationId) {
}

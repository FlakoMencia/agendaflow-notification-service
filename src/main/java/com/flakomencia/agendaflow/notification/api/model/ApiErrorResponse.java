package com.flakomencia.agendaflow.notification.api.model;

import java.time.Instant;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(name = "ApiError", description = "Uniform technical error response")
public record ApiErrorResponse(
        Instant timestamp,
        int status,
        String code,
        String message,
        String path,
        String correlationId,
        @JsonInclude(JsonInclude.Include.NON_EMPTY)
        @Schema(description = "Validation errors keyed by public request field")
        Map<String, String> fieldErrors) {
}

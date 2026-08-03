package com.flakomencia.agendaflow.notification.api.model;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.flakomencia.agendaflow.notification.domain.NotificationChannel;

@Schema(
        name = "NotificationRequestValidationResponse",
        description = "Contract validation result; it is not a delivery or processing acknowledgement")
public record NotificationRequestValidationResponse(
        @Schema(description = "Whether the submitted contract passed validation", examples = "true")
        boolean valid,
        @Schema(description = "Validated channel", examples = "EMAIL")
        NotificationChannel channel,
        @Schema(description = "Validated template code", examples = "APPOINTMENT_CONFIRMATION")
        String templateCode,
        @Schema(description = "Safely normalized email recipient", examples = "customer@example.com")
        String normalizedRecipient,
        @Schema(description = "Whether a future scheduling instant was supplied", examples = "true")
        boolean scheduled,
        @Schema(description = "Request correlation identifier")
        String correlationId) {
}

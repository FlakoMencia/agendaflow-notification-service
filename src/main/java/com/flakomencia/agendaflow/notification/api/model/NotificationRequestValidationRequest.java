package com.flakomencia.agendaflow.notification.api.model;

import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

import com.flakomencia.agendaflow.notification.domain.NotificationChannel;
import com.flakomencia.agendaflow.notification.domain.NotificationLimits;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

@Schema(
        name = "NotificationRequestValidationRequest",
        description = "Candidate notification request validated without sending, storing or enqueueing it")
public record NotificationRequestValidationRequest(
        @NotNull(message = "organizationId is required")
        @Positive(message = "organizationId must be positive")
        @Schema(description = "Owning organization identifier", examples = "10")
        Long organizationId,

        @Positive(message = "appointmentId must be positive")
        @Schema(description = "Optional related appointment identifier", examples = "25")
        Long appointmentId,

        @NotNull(message = "channel is required")
        @Schema(description = "Notification channel; EMAIL is the only supported value", examples = "EMAIL")
        NotificationChannel channel,

        @NotBlank(message = "templateCode is required")
        @Size(max = 80, message = "templateCode must contain at most 80 characters")
        @Pattern(
                regexp = "^[A-Z][A-Z0-9]*(?:_[A-Z0-9]+)*$",
                message = "templateCode must use uppercase letters, numbers and single underscores")
        @Schema(description = "Controlled template identifier", examples = "APPOINTMENT_CONFIRMATION")
        String templateCode,

        @NotBlank(message = "recipient is required")
        @Email(message = "recipient must be a valid email address")
        @Size(max = 254, message = "recipient must contain at most 254 characters")
        @Schema(description = "Email recipient; mailbox existence is not checked", examples = "customer@example.com")
        String recipient,

        @Size(max = 35, message = "locale must contain at most 35 characters")
        @Pattern(
                regexp = "^[A-Za-z]{2,3}(?:-[A-Za-z0-9]{2,8})*$",
                message = "locale must be a valid language tag such as en-US")
        @Schema(description = "Optional BCP 47 language tag", examples = "en-US")
        String locale,

        @Schema(description = "Optional UTC scheduling instant", examples = "2026-08-04T15:00:00Z")
        Instant scheduledAt,

        @Size(max = NotificationLimits.MAX_VARIABLES, message = "variables must contain at most 20 entries")
        @Schema(description = "Optional flat string variables; nested objects are not accepted")
        Map<
                @NotBlank(message = "variable keys must not be blank")
                @Size(
                        max = NotificationLimits.MAX_VARIABLE_KEY_LENGTH,
                        message = "variable keys must contain at most 64 characters")
                @Pattern(
                        regexp = "^[A-Za-z][A-Za-z0-9]*(?:[._-][A-Za-z0-9]+)*$",
                        message = "variable keys have an invalid format")
                String,
                @NotNull(message = "variable values must not be null")
                @Size(
                        max = NotificationLimits.MAX_VARIABLE_VALUE_LENGTH,
                        message = "variable values must contain at most 500 characters")
                String> variables) {

    public NotificationRequestValidationRequest {
        variables = variables == null
                ? Map.of()
                : Collections.unmodifiableMap(new LinkedHashMap<>(variables));
    }
}

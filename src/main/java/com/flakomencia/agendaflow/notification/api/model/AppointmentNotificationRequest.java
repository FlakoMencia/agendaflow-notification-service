package com.flakomencia.agendaflow.notification.api.model;

import java.time.Instant;
import java.util.Map;

import com.flakomencia.agendaflow.notification.domain.AppointmentNotificationEventType;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record AppointmentNotificationRequest(
        @NotNull @Positive Long eventId,
        @NotNull @Positive Long organizationId,
        @NotNull @Positive Long appointmentId,
        @NotNull AppointmentNotificationEventType type,
        @NotBlank @Email @Size(max = 254) String recipient,
        @NotBlank @Size(max = 35) String locale,
        @NotNull Instant occurredAt,
        @NotEmpty @Size(max = 30) Map<@NotBlank @Size(max = 80) String, @NotNull @Size(max = 1000) String> variables) {
}

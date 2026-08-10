package com.flakomencia.agendaflow.notification.application;

import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.jboss.logging.Logger;

import com.flakomencia.agendaflow.notification.domain.NotificationLimits;
import com.flakomencia.agendaflow.notification.domain.NotificationRequest;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class NotificationContractValidationService {

    private static final Logger LOG = Logger.getLogger(NotificationContractValidationService.class);
    private static final Set<String> SENSITIVE_VARIABLE_KEYS = Set.of(
            "password", "secret", "token", "apikey", "authorization", "credential", "credentials");

    public ValidatedNotificationRequest validate(
            NotificationRequest request,
            String correlationId) {
        validateSchedule(request.scheduledAt());
        validateVariables(request.variables().values());

        LOG.infof(
                "Notification contract validation correlationId=%s organizationId=%d channel=%s templateCode=%s result=VALID",
                correlationId,
                request.organizationId(),
                request.channel(),
                request.templateCode().value());

        return new ValidatedNotificationRequest(request);
    }

    private void validateSchedule(Instant scheduledAt) {
        if (scheduledAt != null && scheduledAt.isBefore(Instant.now())) {
            throw invalid(
                    "SCHEDULED_AT_IN_PAST",
                    "scheduledAt must be a future UTC instant",
                    "scheduledAt");
        }
    }

    private void validateVariables(Map<String, String> variables) {
        if (variables.size() > NotificationLimits.MAX_VARIABLES) {
            throw invalid("TOO_MANY_VARIABLES", "variables must contain at most 20 entries", "variables");
        }

        for (Map.Entry<String, String> variable : variables.entrySet()) {
            String key = variable.getKey();
            String value = variable.getValue();
            if (key == null || key.length() > NotificationLimits.MAX_VARIABLE_KEY_LENGTH) {
                throw invalid(
                        "INVALID_VARIABLE_KEY",
                        "variable keys must contain at most 64 characters",
                        "variables");
            }
            if (value == null || value.length() > NotificationLimits.MAX_VARIABLE_VALUE_LENGTH) {
                throw invalid(
                        "INVALID_VARIABLE_VALUE",
                        "variable values must contain at most 500 characters",
                        "variables");
            }

            String normalizedKey = key.replaceAll("[^A-Za-z0-9]", "").toLowerCase(Locale.ROOT);
            if (SENSITIVE_VARIABLE_KEYS.contains(normalizedKey)) {
                throw invalid(
                        "SECRET_VARIABLE_NOT_ALLOWED",
                        "variables must not contain secrets or credentials",
                        "variables");
            }

            String normalizedValue = value.stripLeading().toLowerCase(Locale.ROOT);
            if (normalizedValue.startsWith("<!doctype html") || normalizedValue.startsWith("<html")) {
                throw invalid(
                        "HTML_DOCUMENT_NOT_ALLOWED",
                        "variables must not contain complete HTML documents",
                        "variables");
            }
        }
    }

    private NotificationContractValidationException invalid(String code, String message, String field) {
        return new NotificationContractValidationException(code, message, field);
    }
}

package com.flakomencia.agendaflow.notification.application;

import java.time.Instant;
import java.util.IllformedLocaleException;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.jboss.logging.Logger;

import com.flakomencia.agendaflow.notification.api.model.NotificationRequestValidationRequest;
import com.flakomencia.agendaflow.notification.api.model.NotificationRequestValidationResponse;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class NotificationContractValidationService {

    private static final Logger LOG = Logger.getLogger(NotificationContractValidationService.class);
    private static final int MAX_VARIABLES = 20;
    private static final int MAX_VARIABLE_KEY_LENGTH = 64;
    private static final int MAX_VARIABLE_VALUE_LENGTH = 500;
    private static final Set<String> SENSITIVE_VARIABLE_KEYS = Set.of(
            "password", "secret", "token", "apikey", "authorization", "credential", "credentials");

    public NotificationRequestValidationResponse validate(
            NotificationRequestValidationRequest request,
            String correlationId) {
        validateSchedule(request.scheduledAt());
        normalizeLocale(request.locale());
        validateVariables(request.variables());

        String normalizedRecipient = normalizeRecipient(request.recipient());
        String normalizedTemplateCode = request.templateCode().trim();

        LOG.infof(
                "Notification contract validation correlationId=%s organizationId=%d channel=%s templateCode=%s result=VALID",
                correlationId,
                request.organizationId(),
                request.channel(),
                normalizedTemplateCode);

        return new NotificationRequestValidationResponse(
                true,
                request.channel(),
                normalizedTemplateCode,
                normalizedRecipient,
                request.scheduledAt() != null,
                correlationId);
    }

    private void validateSchedule(Instant scheduledAt) {
        if (scheduledAt != null && scheduledAt.isBefore(Instant.now())) {
            throw invalid(
                    "SCHEDULED_AT_IN_PAST",
                    "scheduledAt must be a future UTC instant",
                    "scheduledAt");
        }
    }

    private String normalizeLocale(String locale) {
        if (locale == null) {
            return null;
        }

        try {
            Locale parsed = new Locale.Builder().setLanguageTag(locale.trim()).build();
            String normalized = parsed.toLanguageTag();
            if (normalized.equals("und")) {
                throw new IllformedLocaleException("Undefined language tag");
            }
            return normalized;
        } catch (IllformedLocaleException exception) {
            throw invalid("INVALID_LOCALE", "locale must be a valid BCP 47 language tag", "locale");
        }
    }

    private void validateVariables(Map<String, String> variables) {
        if (variables.size() > MAX_VARIABLES) {
            throw invalid("TOO_MANY_VARIABLES", "variables must contain at most 20 entries", "variables");
        }

        for (Map.Entry<String, String> variable : variables.entrySet()) {
            String key = variable.getKey();
            String value = variable.getValue();
            if (key == null || key.length() > MAX_VARIABLE_KEY_LENGTH) {
                throw invalid(
                        "INVALID_VARIABLE_KEY",
                        "variable keys must contain at most 64 characters",
                        "variables");
            }
            if (value == null || value.length() > MAX_VARIABLE_VALUE_LENGTH) {
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

    private String normalizeRecipient(String recipient) {
        String trimmed = recipient.trim();
        int separator = trimmed.lastIndexOf('@');
        if (separator <= 0 || separator == trimmed.length() - 1) {
            throw invalid("INVALID_RECIPIENT", "recipient must be a valid email address", "recipient");
        }
        return trimmed.substring(0, separator + 1)
                + trimmed.substring(separator + 1).toLowerCase(Locale.ROOT);
    }

    private NotificationContractValidationException invalid(String code, String message, String field) {
        return new NotificationContractValidationException(code, message, field);
    }
}

package com.flakomencia.agendaflow.notification.application;

import java.util.IllformedLocaleException;
import java.util.Locale;

import com.flakomencia.agendaflow.notification.api.model.NotificationRequestValidationRequest;
import com.flakomencia.agendaflow.notification.api.model.NotificationRequestValidationResponse;
import com.flakomencia.agendaflow.notification.domain.NotificationRecipient;
import com.flakomencia.agendaflow.notification.domain.NotificationRequest;
import com.flakomencia.agendaflow.notification.domain.NotificationTemplateCode;
import com.flakomencia.agendaflow.notification.domain.NotificationVariables;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class NotificationRequestMapper {

    public NotificationRequest toDomain(NotificationRequestValidationRequest request) {
        return new NotificationRequest(
                request.organizationId(),
                request.appointmentId(),
                request.channel(),
                new NotificationTemplateCode(request.templateCode().trim()),
                new NotificationRecipient(normalizeRecipient(request.recipient())),
                normalizeLocale(request.locale()),
                request.scheduledAt(),
                new NotificationVariables(request.variables()));
    }

    public NotificationRequestValidationResponse toResponse(
            ValidatedNotificationRequest validatedRequest,
            String correlationId) {
        NotificationRequest request = validatedRequest.request();
        return new NotificationRequestValidationResponse(
                true,
                request.channel(),
                request.templateCode().value(),
                request.recipient().value(),
                request.scheduledAt() != null,
                correlationId);
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

    private NotificationContractValidationException invalid(String code, String message, String field) {
        return new NotificationContractValidationException(code, message, field);
    }
}

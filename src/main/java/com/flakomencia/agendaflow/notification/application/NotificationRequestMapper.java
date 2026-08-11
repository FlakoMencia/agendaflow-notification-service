package com.flakomencia.agendaflow.notification.application;

import java.util.IllformedLocaleException;
import java.util.Locale;

import com.flakomencia.agendaflow.notification.api.model.NotificationRequestValidationRequest;
import com.flakomencia.agendaflow.notification.api.model.NotificationRequestValidationResponse;
import com.flakomencia.agendaflow.notification.domain.NotificationRequest;
import com.flakomencia.agendaflow.notification.domain.NotificationTemplateCode;
import com.flakomencia.agendaflow.notification.domain.NotificationVariables;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class NotificationRequestMapper {

    private final NotificationRecipientNormalizer recipientNormalizer;

    public NotificationRequestMapper(NotificationRecipientNormalizer recipientNormalizer) {
        this.recipientNormalizer = recipientNormalizer;
    }

    public NotificationRequest toDomain(NotificationRequestValidationRequest request) {
        return new NotificationRequest(
                request.organizationId(),
                request.appointmentId(),
                request.channel(),
                new NotificationTemplateCode(request.templateCode().trim()),
                recipientNormalizer.normalize(request.recipient()),
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

package com.flakomencia.agendaflow.notification.application;

import java.util.Locale;

import com.flakomencia.agendaflow.notification.domain.NotificationRecipient;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class NotificationRecipientNormalizer {

    public NotificationRecipient normalize(String recipient) {
        String trimmed = recipient.trim();
        int separator = trimmed.lastIndexOf('@');
        if (separator <= 0 || separator == trimmed.length() - 1) {
            throw new NotificationContractValidationException(
                    "INVALID_RECIPIENT",
                    "recipient must be a valid email address",
                    "recipient");
        }
        return new NotificationRecipient(
                trimmed.substring(0, separator + 1)
                        + trimmed.substring(separator + 1).toLowerCase(Locale.ROOT));
    }
}

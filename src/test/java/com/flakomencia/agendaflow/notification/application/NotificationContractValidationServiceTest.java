package com.flakomencia.agendaflow.notification.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.Modifier;
import java.time.Instant;
import java.util.Arrays;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.flakomencia.agendaflow.notification.domain.NotificationChannel;
import com.flakomencia.agendaflow.notification.domain.NotificationRecipient;
import com.flakomencia.agendaflow.notification.domain.NotificationRequest;
import com.flakomencia.agendaflow.notification.domain.NotificationTemplateCode;
import com.flakomencia.agendaflow.notification.domain.NotificationVariables;

class NotificationContractValidationServiceTest {

    private final NotificationContractValidationService service = new NotificationContractValidationService();

    @Test
    void returnsTheValidatedInternalRequest() {
        NotificationRequest request = request(Map.of());

        ValidatedNotificationRequest validated = service.validate(request, "unit-correlation");

        assertEquals(request, validated.request());
    }

    @Test
    void rejectsSensitiveVariables() {
        NotificationContractValidationException exception = assertThrows(
                NotificationContractValidationException.class,
                () -> service.validate(request(Map.of("apiKey", "secret-value")), "unit-correlation"));

        assertEquals("SECRET_VARIABLE_NOT_ALLOWED", exception.code());
    }

    @Test
    void rejectsCompleteHtmlDocumentsInVariableValues() {
        NotificationContractValidationException exception = assertThrows(
                NotificationContractValidationException.class,
                () -> service.validate(
                        request(Map.of("content", "<!doctype html><html><body>content</body></html>")),
                        "unit-correlation"));

        assertEquals("HTML_DOCUMENT_NOT_ALLOWED", exception.code());
    }

    @Test
    void rejectsPastSchedules() {
        NotificationRequest request = new NotificationRequest(
                10L,
                25L,
                NotificationChannel.EMAIL,
                new NotificationTemplateCode("APPOINTMENT_CONFIRMATION"),
                new NotificationRecipient("customer@example.com"),
                "en-US",
                Instant.now().minusSeconds(60),
                NotificationVariables.empty());

        NotificationContractValidationException exception = assertThrows(
                NotificationContractValidationException.class,
                () -> service.validate(request, "unit-correlation"));

        assertEquals("SCHEDULED_AT_IN_PAST", exception.code());
    }

    @Test
    void hasNoPersistenceMessagingOrProviderCollaborators() {
        long instanceFields = Arrays.stream(NotificationContractValidationService.class.getDeclaredFields())
                .filter(field -> !Modifier.isStatic(field.getModifiers()))
                .count();

        assertEquals(0, instanceFields);
    }

    private NotificationRequest request(Map<String, String> variables) {
        return new NotificationRequest(
                10L,
                25L,
                NotificationChannel.EMAIL,
                new NotificationTemplateCode("APPOINTMENT_CONFIRMATION"),
                new NotificationRecipient("customer@example.com"),
                "en-US",
                null,
                new NotificationVariables(variables));
    }
}

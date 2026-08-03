package com.flakomencia.agendaflow.notification.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Modifier;
import java.time.Instant;
import java.util.Arrays;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.flakomencia.agendaflow.notification.api.model.NotificationRequestValidationRequest;
import com.flakomencia.agendaflow.notification.api.model.NotificationRequestValidationResponse;
import com.flakomencia.agendaflow.notification.domain.NotificationChannel;

class NotificationContractValidationServiceTest {

    private final NotificationContractValidationService service = new NotificationContractValidationService();

    @Test
    void normalizesOnlyTheEmailDomainSafely() {
        NotificationRequestValidationResponse response = service.validate(
                request("Customer@EXAMPLE.COM", "en-us", null, Map.of()),
                "unit-correlation");

        assertEquals("Customer@example.com", response.normalizedRecipient());
        assertEquals("unit-correlation", response.correlationId());
        assertTrue(response.valid());
    }

    @Test
    void rejectsSensitiveVariables() {
        NotificationContractValidationException exception = assertThrows(
                NotificationContractValidationException.class,
                () -> service.validate(
                        request("customer@example.com", "en-US", null, Map.of("apiKey", "secret-value")),
                        "unit-correlation"));

        assertEquals("SECRET_VARIABLE_NOT_ALLOWED", exception.code());
    }

    @Test
    void rejectsCompleteHtmlDocuments() {
        NotificationContractValidationException exception = assertThrows(
                NotificationContractValidationException.class,
                () -> service.validate(
                        request(
                                "customer@example.com",
                                "en-US",
                                null,
                                Map.of("content", "<!doctype html><html><body>content</body></html>")),
                        "unit-correlation"));

        assertEquals("HTML_DOCUMENT_NOT_ALLOWED", exception.code());
    }

    @Test
    void hasNoPersistenceMessagingOrProviderCollaborators() {
        long instanceFields = Arrays.stream(NotificationContractValidationService.class.getDeclaredFields())
                .filter(field -> !Modifier.isStatic(field.getModifiers()))
                .count();

        assertEquals(0, instanceFields);
    }

    private NotificationRequestValidationRequest request(
            String recipient,
            String locale,
            Instant scheduledAt,
            Map<String, String> variables) {
        return new NotificationRequestValidationRequest(
                10L,
                25L,
                NotificationChannel.EMAIL,
                "APPOINTMENT_CONFIRMATION",
                recipient,
                locale,
                scheduledAt,
                variables);
    }
}

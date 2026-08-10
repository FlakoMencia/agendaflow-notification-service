package com.flakomencia.agendaflow.notification.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.flakomencia.agendaflow.notification.api.model.NotificationRequestValidationRequest;
import com.flakomencia.agendaflow.notification.api.model.NotificationRequestValidationResponse;
import com.flakomencia.agendaflow.notification.domain.NotificationChannel;
import com.flakomencia.agendaflow.notification.domain.NotificationRequest;

class NotificationRequestMapperTest {

    private final NotificationRequestMapper mapper = new NotificationRequestMapper();

    @Test
    void mapsAndNormalizesTheHttpDtoIntoTheInternalModel() {
        Instant scheduledAt = Instant.parse("2030-08-08T16:00:00Z");
        NotificationRequest request = mapper.toDomain(new NotificationRequestValidationRequest(
                10L,
                25L,
                NotificationChannel.EMAIL,
                "APPOINTMENT_CONFIRMATION",
                "  Customer@EXAMPLE.COM  ",
                "en-us",
                scheduledAt,
                Map.of("customerName", "Synthetic")));

        assertEquals(10L, request.organizationId());
        assertEquals(25L, request.appointmentId());
        assertEquals(NotificationChannel.EMAIL, request.channel());
        assertEquals("APPOINTMENT_CONFIRMATION", request.templateCode().value());
        assertEquals("Customer@example.com", request.recipient().value());
        assertEquals("en-US", request.locale());
        assertEquals(scheduledAt, request.scheduledAt());
        assertEquals("Synthetic", request.variables().get("customerName"));
    }

    @Test
    void mapsNullOptionalFieldsWithoutInventingValues() {
        NotificationRequest request = mapper.toDomain(new NotificationRequestValidationRequest(
                10L,
                null,
                NotificationChannel.EMAIL,
                "GENERIC_NOTICE",
                "customer@example.com",
                null,
                null,
                null));

        assertNull(request.appointmentId());
        assertNull(request.locale());
        assertNull(request.scheduledAt());
        assertTrue(request.variables().values().isEmpty());
    }

    @Test
    void createsTheUnchangedExternalValidationResponse() {
        NotificationRequest request = mapper.toDomain(new NotificationRequestValidationRequest(
                10L,
                null,
                NotificationChannel.EMAIL,
                "GENERIC_NOTICE",
                "customer@EXAMPLE.COM",
                null,
                null,
                Map.of()));

        NotificationRequestValidationResponse response = mapper.toResponse(
                new ValidatedNotificationRequest(request),
                "correlation-id");

        assertTrue(response.valid());
        assertEquals(NotificationChannel.EMAIL, response.channel());
        assertEquals("GENERIC_NOTICE", response.templateCode());
        assertEquals("customer@example.com", response.normalizedRecipient());
        assertEquals("correlation-id", response.correlationId());
    }

    @Test
    void requestAndVariablesAreDefensivelyCopied() {
        LinkedHashMap<String, String> mutableVariables = new LinkedHashMap<>();
        mutableVariables.put("name", "Original");
        NotificationRequestValidationRequest dto = new NotificationRequestValidationRequest(
                10L,
                null,
                NotificationChannel.EMAIL,
                "GENERIC_NOTICE",
                "customer@example.com",
                null,
                null,
                mutableVariables);

        NotificationRequest request = mapper.toDomain(dto);
        mutableVariables.put("name", "Changed");

        assertEquals("Original", request.variables().get("name"));
        assertThrows(
                UnsupportedOperationException.class,
                () -> request.variables().values().put("another", "value"));
    }
}

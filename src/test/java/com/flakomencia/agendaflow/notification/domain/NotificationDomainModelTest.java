package com.flakomencia.agendaflow.notification.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashMap;

import org.junit.jupiter.api.Test;

class NotificationDomainModelTest {

    @Test
    void domainModelsAreRecordsWithoutHttpAnnotations() {
        assertTrue(NotificationRequest.class.isRecord());
        assertTrue(NotificationRecipient.class.isRecord());
        assertTrue(NotificationTemplateCode.class.isRecord());
        assertTrue(NotificationVariables.class.isRecord());
        assertTrue(RenderedNotification.class.isRecord());
        assertTrue(NotificationTemplate.class.isRecord());
        assertTrue(AppointmentNotificationEvent.class.isRecord());
        assertEquals(0, NotificationRequest.class.getAnnotations().length);
        assertEquals(0, RenderedNotification.class.getAnnotations().length);
    }

    @Test
    void variablesKeepAnImmutableInsertionOrderedCopy() {
        LinkedHashMap<String, String> source = new LinkedHashMap<>();
        source.put("first", "one");
        NotificationVariables variables = new NotificationVariables(source);
        source.put("second", "two");

        assertEquals(1, variables.values().size());
        assertEquals("one", variables.get("first"));
        assertThrows(UnsupportedOperationException.class, () -> variables.values().clear());
    }

    @Test
    void rejectsInvalidInternalModelValuesWithoutJakartaValidation() {
        assertThrows(IllegalArgumentException.class, () -> new NotificationRecipient(" "));
        assertThrows(
                IllegalArgumentException.class,
                () -> new NotificationRequest(
                        0L,
                        null,
                        NotificationChannel.EMAIL,
                        new NotificationTemplateCode("SYNTHETIC_NOTICE"),
                        new NotificationRecipient("recipient@example.com"),
                        null,
                        null,
                        NotificationVariables.empty()));
    }
}
